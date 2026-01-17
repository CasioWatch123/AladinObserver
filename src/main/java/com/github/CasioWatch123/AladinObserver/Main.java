package com.github.CasioWatch123.AladinObserver;

import com.github.CasioWatch123.AladinObserver.log.Logger;
import com.github.CasioWatch123.AladinObserver.model.offshop.OffShopProductTrayModel;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.tray.ProductTrayImpl;
import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Gson gsonParser = new Gson();
    
    private static final Logger logger = Logger.getInstance();
    
    public static final String KOSMOS = "870950";
    public static final String GoF = "56051596";
    public static final String KAJEKAOKA = "113379001";
    
    public static void main(String[] args) {
        System.out.println("initializing...");
        OffShopProductTrayModel model = new OffShopProductTrayModel(new ProductTrayImpl());

        System.out.println("subscribing...");
        
        model.subscribeTray(keySet -> {
            keySet.forEach(key -> {
                model.getProductData(key)
                        .thenAccept(aladinProductData -> {
                            OffshopCheckResult offshopCheckResult = aladinProductData.getHistoryFirst();
                            System.out.println(offshopCheckResult.getTimestamp().withNano(0) + " : " + aladinProductData.itemId());
                        });
            });
        });
        model.subscribeProductStockChanges(keySet -> {
            System.out.print("Product stock changed : ");
            keySet.forEach(key -> System.out.print(key + " "));
            System.out.println();
        });
        
        System.out.println("run...");
        model.run(5, TimeUnit.SECONDS);
        
        try (ScheduledExecutorService timer = Executors.newScheduledThreadPool(1)) {
            timer.schedule(() -> {
                model.shutdown();
                System.out.println("shutdown!");
            }, 21, TimeUnit.SECONDS);
        }
    }
}