package com.github.casiowatch123.aladinobserver.viewmodel;

import com.github.casiowatch123.aladinobserver.model.config.BooleanConfigElement;
import com.github.casiowatch123.aladinobserver.model.config.ConfigElement;
import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;

import java.util.concurrent.TimeUnit;

public class ConfigVM {
    private final OffShopProductTrayService productTrayService;
    private final StateRepo stateRepo;
    private final ConfigRegistry configRegistry;
    
    private static final String RUN_PERIOD_CONFIG_ID = "Run_Period";
    private static final String EXECUTION_FLAG_CONFIG_ID = "Execution_Flag";
    private static final String NOTIFICATION_FLAG_CONFIG_ID = "Notification_Flag";
    
    public ConfigVM(OffShopProductTrayService productTrayService, ConfigRegistry configRegistry, StateRepo stateRepo) {
        this.productTrayService = productTrayService;
        this.configRegistry = configRegistry;
        this.stateRepo = stateRepo;
        
        //register config elements
        configRegistry.register(
                RUN_PERIOD_CONFIG_ID, 
                new ConfigElement<Long> (
                        Long.class, 
                        ld -> productTrayService.setPeriod(ld, TimeUnit.MINUTES), 
                        Long::equals, 
                        ld -> Long.toString(ld), 
                        Long::valueOf
                ), 
                30L
        );
        
        configRegistry.register(
                EXECUTION_FLAG_CONFIG_ID, 
                new BooleanConfigElement(flag -> {
                    if (flag) {
                        productTrayService.run();
                    } else {
                        productTrayService.stop();
                    }
                }), 
                false
        );
        
        configRegistry.register(
                NOTIFICATION_FLAG_CONFIG_ID, 
                new BooleanConfigElement(flag -> {
                    if (flag) {
                        stateRepo.enableNotification();
                    } else {
                        stateRepo.disableNotification();
                    }
                }), 
                true
        );
        stateRepo.enableNotification();
    }
    
    
    public void trayObservingExecution(boolean flag) {
        configRegistry.applyIfDif(EXECUTION_FLAG_CONFIG_ID, flag);
    }
    
    public void setRunPeriod(Long period) {
        configRegistry.applyIfDif(RUN_PERIOD_CONFIG_ID, period);
    }
    
    public void notificationEnabled(Boolean flag) {
        configRegistry.applyIfDif(NOTIFICATION_FLAG_CONFIG_ID, flag);
    }
    
    
    public void applicationTerminate() {
        //todo:implement
        //productTrayModel.shutdown()...
    }
}
