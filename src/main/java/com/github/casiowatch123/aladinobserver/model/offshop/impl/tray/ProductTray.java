package com.github.casiowatch123.aladinobserver.model.offshop.impl.tray;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ProductTray {
    CompletableFuture<Void> updateAllAsync();
    
    Set<AladinProductData> getTrayData();
    
    boolean addProduct(String itemId);
    
    void removeProduct(String itemId);
    
    Set<String> getKeySet();
    
    void shutdown();
}
