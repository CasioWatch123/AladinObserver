package com.github.casiowatch123.aladinobserver.model.offshop.impl.products;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions.AladinAPIException;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions.ProductUpdateException;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.casiowatch123.aladinobserver.model.ModelPolicies;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAladinProduct implements AladinProduct{
    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    
    protected static final Gson GSON_PARSER = new Gson();

    protected static final int TIMEOUT_SEC = ModelPolicies.TIMEOUT_SEC;
    
    protected final URI imageURI;
    protected final String itemId;
    protected final String itemName;
    protected static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    protected BufferedImage itemImage;
    
    protected final CheckResultDeque checkResultDeque;
    
    protected final TTBKeyService ttbKeyService;
    
    
    protected OffshopCheckResult previousOffshopCheckResult;
    
    protected AbstractAladinProduct(HistoryObjectDeque<OffshopCheckResult> historyObjectDeque,
                                    URI imageURI,
                                    String itemId,
                                    String itemName,
                                    BufferedImage defaultImage,
                                    TTBKeyService ttbKeyService) {        
        this.imageURI = imageURI;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImage = defaultImage;
        this.checkResultDeque = generateHistoryDeque(historyObjectDeque);
        this.ttbKeyService = ttbKeyService;
    }
    
    protected CheckResultDeque generateHistoryDeque(HistoryObjectDeque<OffshopCheckResult> historyObjectList) {
        CheckResultDeque historyDeque = new CheckResultDeque();
        //Initialize history deque based on execution history            
        
        historyObjectList
                .getDeque()
                .forEach(historyObject -> {
                    if (historyObject.isValidKey(itemId)) {
                        historyDeque.addHistoryLast(historyObject.get(itemId));
                    }
                });
        
        this.previousOffshopCheckResult = historyDeque.getDeque().peekFirst();
        return historyDeque;
    }
    
    @Override
    public CompletableFuture<OffshopCheckResult> updateAsync(Executor asyncExecutor) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getUpdateURI())
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .GET()
                    .build();

            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .orTimeout(5, TimeUnit.SECONDS)
                    .thenApplyAsync(response -> {
                        try {
                            JsonObject responseJson = GSON_PARSER.fromJson(response.body(), JsonObject.class);
                            //error response from aladin api.
                            if (responseJson.get("errorCode") != null) {
                                throw new AladinAPIException(itemId, responseJson.get("errorMessage").getAsString());
                            }

                            //generate valid off shop list
                            List<String> offShopList = responseJson.getAsJsonArray("itemOffStoreList").asList()
                                    .stream()
                                    .map(element -> element
                                            .getAsJsonObject()
                                            .get("offName")
                                            .getAsString())
                                    .toList();

                            OffshopCheckResult result;
                            
                            this.previousOffshopCheckResult = checkResultDeque.getDeque().peekFirst();
                            
                            if (offShopList.isEmpty()) {
                                result = OffshopCheckResult.getEmptyCheckResult(itemId);
                                checkResultDeque.addHistoryFirst(OffshopCheckResult.getEmptyCheckResult(itemId));
                            } else {
                                result = new OffshopCheckResult(itemId, offShopList, LocalDateTime.now().withNano(0));
                                checkResultDeque.addHistoryFirst(result);
                            }
                            
                            return result;
                        } catch (AladinAPIException | JsonSyntaxException e) {
                            throw new ProductUpdateException(e);
                        }
                    }, asyncExecutor)
                    .exceptionally(e -> {
                        Logger.getInstance().writeLog(e);
                        return OffshopCheckResult.getExceptionalCheckResult(itemId);
                    });
        } catch (RejectedExecutionException e) {
            return CompletableFuture.completedFuture(OffshopCheckResult.getExceptionalCheckResult(itemId));
        } catch (URISyntaxException e) {
            Logger.getInstance().writeLog(e);
            return CompletableFuture.completedFuture(OffshopCheckResult.getExceptionalCheckResult(itemId));
        }
    }
    
    @Override
    public AladinProductData getData() {
        return new AladinProductData() {
            private final BufferedImage image = itemImage;
            private final String id = itemId;
            private final String name = itemName;
            
            private final OffshopCheckResult previousCheckResult = previousOffshopCheckResult;
            private final List<OffshopCheckResult> historyList = List.copyOf(new ArrayDeque<>(checkResultDeque.getDeque()));
            
            @Override
            public OffshopCheckResult getPreviousCheckResult() {
                return this.previousCheckResult;
            }

            @Override
            public BufferedImage itemImage() {
                return this.image;
            }

            @Override
            public String itemId() {
                return this.id;
            }

            @Override
            public String itemName() {
                return this.name;
            }

            @Override
            public List<OffshopCheckResult> getHistoryList() {
                return this.historyList;
            }

            @Override
            public OffshopCheckResult getHistoryFirst() {
                return this.historyList.getFirst();
            }
        };
    }
    
    protected abstract URI getUpdateURI() throws URISyntaxException;
}
