package com.github.casiowatch123.aladinobserver.viewmodel;


import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;

public class ConfigVM {
    private final OffShopProductTrayService productTrayService;
    private final ConfigRegistry configRegistry;
    public static final long MIN_RUN_PERIOD = ConfigRegistry.MIN_RUN_PERIOD;
    public static final long MAX_RUN_PERIOD = ConfigRegistry.MAX_RUN_PERIOD;
    
    public ConfigVM(OffShopProductTrayService productTrayService, ConfigRegistry configRegistry) {
        this.productTrayService = productTrayService;
        this.configRegistry = configRegistry;
    }
    
    
    public void trayObservingExecution(boolean flag) {
        configRegistry.setExecutionFlag(flag);
    }
    public void setRunPeriod(Long period) {
        configRegistry.setRunPeriod(period);
    }
    public void notificationEnabled(Boolean flag) {
        configRegistry.setNotificationFlag(flag);
    }
    
    public long getCurrentRunPeriod() {
        return configRegistry.getRunPeriod();
    }
    public boolean getCurrentExecutionFlag() {
        return configRegistry.getExecutionFlag();
    }
    public boolean getCurrentNotificationFlag() {
        return configRegistry.getNotificationFlag();
    }
    
    public void modelShutdown() {
        productTrayService.shutdown();
    }
}
