package com.github.casiowatch123.aladinobserver.model.offshop.impl.tray;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.OffshopCheckResultIO;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.HistoryObject;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.localio.IO;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProduct;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.book.Book;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions.ProductInitializeException;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryPolicies;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class ProductTrayImpl implements ProductTray {
    private final Map<String, AladinProduct> productMap = new HashMap<>();
    private final HistoryObjectDeque<OffshopCheckResult> historyObjectDeque;
    //무지성
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(8);
    
    private final IO<OffshopCheckResult> checkResultIO = new OffshopCheckResultIO();
    
    private final TTBKeyService ttbKeyService;
    
    public ProductTrayImpl(TTBKeyService ttbKeyService) {
        this.ttbKeyService = ttbKeyService;
        this.historyObjectDeque = checkResultIO.readLines(HistoryPolicies.MAX_HISTORY_DEQUE_SIZE);
    }
    
    @Override
    public CompletableFuture<Void> updateAllAsync() {
        Map<String, CompletableFuture<OffshopCheckResult>> completableFutureMap = new HashMap<>();
        
        productMap.forEach((key, product) -> 
                completableFutureMap.put(key, product.updateAsync(asyncExecutor)
        ));
        
        return CompletableFuture
                .allOf(completableFutureMap.values().toArray(CompletableFuture<?>[]::new))
                .whenCompleteAsync((V, T) -> {
                    HistoryObject<OffshopCheckResult> historyObject = new HistoryObject<>();

                    completableFutureMap.forEach((key, future) ->
                            historyObject.put(key, future.join())
                    );
                    
                    historyObjectDeque.addHistory(historyObject);
                    checkResultIO.write(historyObject);
                }, asyncExecutor);
    }

    @Override
    public Set<AladinProductData> getTrayData() {
        return productMap.values().stream()
                .map(product -> (AladinProductData) product)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean addProduct(String itemId) {
        if (productMap.size() >= ProductTrayPolicies.MAX_TRAY_SIZE || productMap.containsKey(itemId)) {
            return false;
        }
        try {
            productMap.put(itemId, Book.create(itemId, historyObjectDeque, ttbKeyService));
            return true;
        }  catch (ProductInitializeException e) {
            Logger.getInstance().writeLog(e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
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
        asyncExecutor.shutdown();
    }
}
