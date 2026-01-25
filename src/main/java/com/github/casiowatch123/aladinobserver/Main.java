package com.github.casiowatch123.aladinobserver;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.config.ConfigElement;
import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTrayImpl;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyHolder;
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
    
    public static final String RUN_PERIOD_ID = "Run_Period";
    public static final String EXECUTION_FLAG_ID = "Execution_Flag";
    
    public static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger.getInstance().writeLog(throwable, "occurred thread : " + thread.getName());
        });
        
        
        System.out.println("initializing...");
        DataStorageFactory dataStorageFactory = new DataStorageFactory();

        TTBKeyHolder ttbKeyHolder = new TTBKeyHolder(dataStorageFactory);
        
        OffShopProductTrayService productTrayService = new OffShopProductTrayService(new ProductTrayImpl(ttbKeyHolder), dataStorageFactory);


        ConfigRegistry configRegistry = new ConfigRegistry(dataStorageFactory);
        
        ConfigElement<Boolean> executionFlagElement = new ConfigElement<>(
                Boolean.class, 
                flag -> {
                    if (flag) {
                        productTrayService.run();
                    } else {
                        productTrayService.stop();
                    }
                }, 
                Boolean::equals,
                b -> Boolean.toString(b),
                Boolean::valueOf
        );
        
        ConfigElement<Long> runPeriodConfigElement = new ConfigElement<>(
                Long.class, 
                period -> productTrayService.setPeriod(period, TimeUnit.SECONDS), 
                Long::equals, 
                l -> Long.toString(l), 
                s -> {
                    try {
                        return Long.valueOf(s);
                    } catch (NumberFormatException e) {
                        return 5L;
                    }
                }
        );
        
        configRegistry.register(RUN_PERIOD_ID, runPeriodConfigElement, 5L);
        configRegistry.register(EXECUTION_FLAG_ID, executionFlagElement, false);
        
        
//        ttbKeyHolder.setTTBKey(<ttbKey>);
//        productTrayService.addProduct(GoF);
//        productTrayService.addProduct(KOSMOS);

        System.out.println("subscribing...");
        productTrayService.subscribeTray(dataset -> {
            if (dataset.isEmpty()) {
                System.out.println("is empty!");
                return;
            }

            System.out.print("+update        : ");
            System.out.println(dataset.iterator().next().getHistoryFirst().getTimestamp());
            dataset.forEach(data -> System.out.print(data.itemId() + " "));
            System.out.println();
        });
        productTrayService.subscribeProductStockChanges(dataset -> {
            System.out.print("+Stock changed : ");
            dataset.forEach(data -> System.out.print(data.itemId() + " "));
            System.out.println();
        });


        System.out.println("run...");
//        configRegistry.applyIfDif(RUN_PERIOD_ID, 5L);
//        configRegistry.applyIfDif(EXECUTION_FLAG_ID, true);

        try (ScheduledExecutorService timer = Executors.newScheduledThreadPool(1)) {
            timer.schedule(() -> {
                configRegistry.applyIfDif(RUN_PERIOD_ID, 15L);
                System.out.println("period changed!");

            }, 20, TimeUnit.SECONDS);

            timer.schedule(() -> {
                configRegistry.applyIfDif(EXECUTION_FLAG_ID, false);
                System.out.println("stop!");
            }, 1, TimeUnit.MINUTES);

            timer.schedule(() -> {
                configRegistry.applyIfDif(EXECUTION_FLAG_ID, true);
                System.out.println("run..");
            }, 2, TimeUnit.MINUTES);

            timer.schedule(() -> {
                System.out.println("shutdown...");
                configRegistry.apply(RUN_PERIOD_ID, 5L);
                productTrayService.shutdown();
                System.out.println("shutdown!");
            }, 3, TimeUnit.MINUTES);
        }
    }
}