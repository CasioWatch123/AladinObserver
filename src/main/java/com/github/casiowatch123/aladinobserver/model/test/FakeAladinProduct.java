package com.github.casiowatch123.aladinobserver.model.test;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProduct;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.CheckResultDeque;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.book.Book;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FakeAladinProduct implements AladinProduct {
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

    private final String itemId;
    private final String itemName;

    private BufferedImage itemImage;

    private final CheckResultDeque checkResultDeque = new CheckResultDeque();

    private OffshopCheckResult previousOffshopCheckResult;
    
    public FakeAladinProduct(String itemId, HistoryObjectDeque<OffshopCheckResult> historyObjectDeque) {
        this.itemId = itemId;
        this.itemName = "Fake_" + itemId;
        this.itemImage = defaultImage;
    }
    @Override
    public CompletableFuture<OffshopCheckResult> updateAsync(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
                    this.previousOffshopCheckResult = checkResultDeque.getDeque().peekFirst();

                    Random random = new Random();
                    
                    List<String> offShopList = new ArrayList<>();
                    for (int i = 0 ; i < random.nextInt(10) + 1 ; i++) {
                        offShopList.add(String.format("shop%d", i));
                    }

                    OffshopCheckResult result = new OffshopCheckResult(itemId, offShopList, LocalDateTime.now());
                
                    checkResultDeque.addHistoryFirst(result);
                            
                    return result;
                }, executor)
                .exceptionally(e -> {
                    Logger.getInstance().writeLog(e);
                    return OffshopCheckResult.getExceptionalCheckResult(itemId);
                });
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

    public CompletableFuture<OffshopCheckResult> emptyUpdate(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
                    OffshopCheckResult result;

                    this.previousOffshopCheckResult = checkResultDeque.getDeque().peekFirst();

                    result = OffshopCheckResult.getEmptyCheckResult(itemId);
                    checkResultDeque.addHistoryFirst(OffshopCheckResult.getEmptyCheckResult(itemId));

                    return result;
                }, executor)
                .exceptionally(e -> {
                    Logger.getInstance().writeLog(e);
                    return OffshopCheckResult.getExceptionalCheckResult(itemId);
                });
    }
    
    public CompletableFuture<OffshopCheckResult> exceptionalUpdate(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
                    OffshopCheckResult result;

                    this.previousOffshopCheckResult = checkResultDeque.getDeque().peekFirst();

                    result = OffshopCheckResult.getEmptyCheckResult(itemId);
                    checkResultDeque.addHistoryFirst(OffshopCheckResult.getExceptionalCheckResult(itemId));

                    return result;
                }, executor)
                .exceptionally(e -> {
                    Logger.getInstance().writeLog(e);
                    return OffshopCheckResult.getExceptionalCheckResult(itemId);
                });
    }
}
