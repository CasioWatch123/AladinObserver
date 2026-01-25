package com.github.casiowatch123.aladinobserver.viewmodel;

import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NotifierVM {
    private final OffShopProductTrayService productTrayService;
    private final StateRepo stateRepo;
    
    private final List<Consumer<Set<AladinProductData>>> subscribers = new ArrayList<>();
    
    private Boolean flag;
    
    public NotifierVM(OffShopProductTrayService productTrayService, StateRepo stateRepo) {
        this.productTrayService = productTrayService;
        this.stateRepo = stateRepo;
        
        stateRepo.notifierEnabled().subscribe(bool -> {
            if (bool) {
                enableNotifying();
            } else {
                disableNotifying();
            }
        });
        
        productTrayService.subscribeProductStockChanges(this::notifyProductChanges);
    }
    
    public Runnable subscribeProductChanges(Consumer<Set<AladinProductData>> subscriber) {
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }
    
    private void notifyProductChanges(Set<AladinProductData> changedProductDataSet) {
        if (flag) {
            new ArrayList<>(subscribers).forEach(consumer -> consumer.accept(changedProductDataSet));
        }
    }
    
    private void enableNotifying() {
        this.flag = true;
    }
    
    private void disableNotifying() {
        this.flag = false;
    }
}
