package com.github.CasioWatch123.AladinObserver.model.offshop.impl.tray;

import com.github.CasioWatch123.AladinObserver.log.Logger;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO.CheckResultIO;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO.HistoryObject;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO.IO;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.AladinProduct;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.AladinProductData;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.book.Book;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions.ProductInitializeException;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.CheckResult;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.HistoryPolicies;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductTrayImpl implements ProductTray {
    private final Map<String, AladinProduct> productMap;
    private final HistoryObjectDeque<CheckResult> historyObjectDeque;
    
    private final ExecutorService asyncExecutor;
    
    private final IO<CheckResult> checkResultIO;
    public ProductTrayImpl() {
        this.checkResultIO = CheckResultIO.getInstance();
        this.productMap = new HashMap<>();
        this.historyObjectDeque = checkResultIO.readLines(HistoryPolicies.MAX_HISTORY_DEQUE_SIZE);
        //무지성
        this.asyncExecutor = Executors.newFixedThreadPool(8);
    }
    
    @Override
    public Set<String> updateAll() {
        Map<String, CompletableFuture<CheckResult>> completableFutureMap = new HashMap<>();
        
        productMap.forEach((key, product) -> 
                completableFutureMap.put(key, product.updateAsync(asyncExecutor)
        ));
        
        CompletableFuture
                .allOf(completableFutureMap.values().toArray(CompletableFuture<?>[]::new))
                .join();
        
        HistoryObject<CheckResult> historyObject = new HistoryObject<>();
        
        completableFutureMap.forEach((key, future) -> 
                historyObject.put(key, future.join())
        );

        historyObjectDeque.addHistory(historyObject);
        checkResultIO.write(historyObject);
        
        return Set.copyOf(completableFutureMap.keySet());
    }

    @Override
    public AladinProductData getProductData(String itemId) {
        return productMap.getOrDefault(itemId, null);
    }

    @Override
    public boolean addProduct(String itemId) {
        if (productMap.size() >= ProductTrayPolicies.MAX_TRAY_SIZE) {
            return false;
        }
        try {
            productMap.put(itemId, Book.create(itemId, historyObjectDeque));
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
