package com.github.CasioWatch123.AladinObserver.model.offshop.impl.tray;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.AladinProductData;

import java.util.Set;

public interface ProductTray {
    Set<String> updateAll();
    
    AladinProductData getProductData(String itemId);
    
    boolean addProduct(String itemId);
    
    void removeProduct(String itemId);
    
    Set<String> getKeySet();
    
    void shutdown();
}
