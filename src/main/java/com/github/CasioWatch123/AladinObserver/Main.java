package com.github.CasioWatch123.AladinObserver;

import com.github.CasioWatch123.AladinObserver.log.Logger;
import com.github.CasioWatch123.AladinObserver.model.TTBKeyHolder;
import com.github.CasioWatch123.AladinObserver.model.offshop.OffShopProductTray;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.CheckResult;
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
        OffShopProductTray model = new OffShopProductTray(new ProductTrayImpl());

        model.addProduct(KOSMOS);
        model.addProduct(GoF);

        System.out.println("subscribing...");
        model.subscribeTray(keySet -> {
            keySet.forEach(key -> {
                model.getProductData(key)
                        .thenAccept(aladinProductData -> {
                            CheckResult checkResult = aladinProductData.getHistoryFirst();
                            System.out.println(checkResult.getTimestamp().withNano(0) + aladinProductData.itemId());
                        });
            });
        });
        System.out.println("run...");
        model.run(5, TimeUnit.SECONDS);

        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

        timer.schedule(() -> {
            model.shutdown();
            timer.shutdown();
            System.out.println("shutdown!");
        }, 1, TimeUnit.MINUTES);
    }
}