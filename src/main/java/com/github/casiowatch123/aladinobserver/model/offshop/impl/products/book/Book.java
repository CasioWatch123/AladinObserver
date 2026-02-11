package com.github.casiowatch123.aladinobserver.model.offshop.impl.products.book;


import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AbstractAladinProduct;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProduct;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions.AladinAPIException;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions.ProductInitializeException;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyService;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Book  extends AbstractAladinProduct implements AladinProduct {
    private static final BufferedImage defaultImage;

    //Initializing default cover image
    static {
        try (InputStream in = Book.class
                .getClassLoader()
                .getResourceAsStream("ProxyBookCoverImage.png")) {
            defaultImage = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Book(HistoryObjectDeque<OffshopCheckResult> historyObjectDeque,
                 URI imageURI,
                 String itemId,
                 String itemName,
                 BufferedImage defaultImage,
                 TTBKeyService ttbKeyService) {
        super(historyObjectDeque, imageURI, itemId, itemName, defaultImage, ttbKeyService);
    }
    
    public static Book create(String itemId, HistoryObjectDeque<OffshopCheckResult> historyObjectDeque, TTBKeyService ttbKeyService) throws InterruptedException, ProductInitializeException {
        try {
            URI itemInfoRequestURI = new URI("http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx?ttbkey=" + 
                    ttbKeyService.getTTBKey() +
                    "&itemIdType=ItemId&ItemId=" + 
                    itemId +
                    "&output=JS&Version=20131101");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(itemInfoRequestURI)
                    .timeout(Duration.ofSeconds(AbstractAladinProduct.TIMEOUT_SEC))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT
                    .send(request, HttpResponse.BodyHandlers.ofString());
            
            JsonObject responseJson = GSON_PARSER.fromJson(response.body(), JsonObject.class);
            
            if (responseJson.get("errorCode") != null) {
                throw new AladinAPIException("(itemId)" + itemId, responseJson.get("errorMessage").getAsString());
            }

            URI imageURI;
            String itemName;
            BufferedImage cover;
            
            JsonObject itemJsonObject = responseJson.getAsJsonArray("item").get(0).getAsJsonObject();
            
            imageURI = new URI(itemJsonObject.get("cover").getAsString());
            itemName = itemJsonObject.get("title").getAsString();
            
            cover = loadImage(imageURI);
            
            return new Book(historyObjectDeque, imageURI, itemId, itemName, cover, ttbKeyService);
        } catch (AladinAPIException | URISyntaxException | IOException e) {
            throw new ProductInitializeException("Failed to load book", e);
        } catch (JsonSyntaxException e) {
            throw new ProductInitializeException("Failed to load book (server issue)", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
    
    private static BufferedImage loadImage(URI imageURI) throws InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(imageURI)
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }

            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("");

            if (!contentType.startsWith("image/")) {
                throw new IOException("Not an image: " + contentType);
            }

            byte[] body = response.body();

            if (body.length > MAX_IMAGE_BYTES) {
                throw new IOException("Image too large: " + body.length);
            }

            try (ByteArrayInputStream in = new ByteArrayInputStream(body)) {
                return ImageIO.read(in);
            }
        } catch (IOException e) {
            Logger.getInstance().writeLog(e);
            return defaultImage;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    protected URI getUpdateURI() throws URISyntaxException {
        return new URI("http://www.aladin.co.kr/ttb/api/ItemOffStoreList.aspx?ttbkey=" +
                ttbKeyService.getTTBKey() +
                "&itemIdType=ItemId&ItemId=" +
                itemId +
                "&output=JS");
    }
}