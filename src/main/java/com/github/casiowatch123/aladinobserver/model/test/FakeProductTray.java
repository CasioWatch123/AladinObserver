package com.github.casiowatch123.aladinobserver.model.test;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.HistoryObject;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProduct;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTray;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTrayPolicies;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FakeProductTray implements ProductTray {
    private final Map<String, FakeAladinProduct> productMap = new HashMap<>();
    private final HistoryObjectDeque<OffshopCheckResult> historyObjectDeque = new HistoryObjectDeque<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private FakeProductTray() {
    }
    
    
    public static FakeProductTray newInstanceWithUpdate() {
        FakeProductTray instance = new FakeProductTray();
        instance.addProduct("product01");
        instance.addProduct("product02");
        instance.updateAllAsync().join();
        instance.exceptionalUpdateAllAsync().join();
        instance.emptyUpdateAllAsync().join();
        
        return instance;
    }
    @Override
    public CompletableFuture<Void> updateAllAsync() {
        Map<String, CompletableFuture<OffshopCheckResult>> completableFutureMap = new HashMap<>();

        productMap.forEach((key, product) ->
                completableFutureMap.put(key, product.updateAsync(executor))
        );

        return CompletableFuture
                .allOf(completableFutureMap.values().toArray(CompletableFuture<?>[]::new))
                .whenCompleteAsync((V, T) -> {
                    HistoryObject<OffshopCheckResult> historyObject = new HistoryObject<>();

                    completableFutureMap.forEach((key, future) ->
                            historyObject.put(key, future.join())
                    );

                    historyObjectDeque.addHistory(historyObject);
                }, executor);
    }

    public CompletableFuture<Void> emptyUpdateAllAsync() {
        Map<String, CompletableFuture<OffshopCheckResult>> completableFutureMap = new HashMap<>();

        productMap.forEach((key, product) ->
                completableFutureMap.put(key, product.emptyUpdate(executor))
        );

        return CompletableFuture
                .allOf(completableFutureMap.values().toArray(CompletableFuture<?>[]::new))
                .whenCompleteAsync((V, T) -> {
                    HistoryObject<OffshopCheckResult> historyObject = new HistoryObject<>();

                    completableFutureMap.forEach((key, future) ->
                            historyObject.put(key, future.join())
                    );

                    historyObjectDeque.addHistory(historyObject);
                }, executor);
    }
    
    public CompletableFuture<Void> exceptionalUpdateAllAsync() {
        Map<String, CompletableFuture<OffshopCheckResult>> completableFutureMap = new HashMap<>();

        productMap.forEach((key, product) ->
                completableFutureMap.put(key, product.exceptionalUpdate(executor))
        );

        return CompletableFuture
                .allOf(completableFutureMap.values().toArray(CompletableFuture<?>[]::new))
                .whenCompleteAsync((V, T) -> {
                    HistoryObject<OffshopCheckResult> historyObject = new HistoryObject<>();

                    completableFutureMap.forEach((key, future) ->
                            historyObject.put(key, future.join())
                    );

                    historyObjectDeque.addHistory(historyObject);
                }, executor);
    }

    @Override
    public Set<AladinProductData> getTrayData() {
        return productMap.values().stream()
                .map(AladinProduct::getData)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean addProduct(String itemId) {
        if (productMap.size() >= ProductTrayPolicies.MAX_TRAY_SIZE || productMap.containsKey(itemId)) {
            return false;
        }
        productMap.put(itemId, new FakeAladinProduct(itemId, historyObjectDeque));
//        productMap.put(itemId, Book.create(itemId, historyObjectDeque, ttbKeyService));
        return true;
    }

    @Override
    public void removeProduct(String itemId) {
        productMap.remove(itemId);
    }

    @Override
    public Set<String> getKeySet() {
        return Set.copyOf(productMap.keySet());
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
