package com.github.casiowatch123.aladinobserver.viewmodel;

import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;

import java.util.Set;
import java.util.function.Consumer;

public class ProductTrayVM {
    private final OffShopProductTrayService productTrayService;
    
    public ProductTrayVM(OffShopProductTrayService productTrayService) {
        this.productTrayService = productTrayService;
    }
    
    public Runnable subscribeTray(Consumer<Set<AladinProductData>> consumer) {
        return productTrayService.subscribeTray(consumer);
    }
    
    public void addProduct(String itemId) {
        productTrayService.addProduct(itemId);
    }
    
    public void removeProduct(String itemId) {
        productTrayService.removeProduct(itemId);
    }
}
