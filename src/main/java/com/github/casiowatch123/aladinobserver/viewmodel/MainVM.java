package com.github.casiowatch123.aladinobserver.viewmodel;

import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyHolder;
import com.github.casiowatch123.aladinobserver.model.config.StateRepo;

public final class MainVM {
    private final TTBKeyHolder ttbKeyHolder;
    private final OffShopProductTrayService productTrayService;
    private final ConfigRegistry configRegistry;
    
    private final ConfigVM configVM;
    private final ProductTrayVM productTrayVM;
    private final NotifierVM notifierVM;
    
    private final StateRepo stateRepo;
    public MainVM(
            TTBKeyHolder ttbKeyHolder, 
            OffShopProductTrayService productTrayService, 
            ConfigRegistry configRegistry, 
            StateRepo stateRepo
    ) {
        this.ttbKeyHolder = ttbKeyHolder;
        this.productTrayService = productTrayService;
        this.configRegistry = configRegistry;
        this.stateRepo = stateRepo;
        
        this.productTrayVM = new ProductTrayVM(productTrayService);
        this.configVM = new ConfigVM(productTrayService, configRegistry);
        this.notifierVM = new NotifierVM(productTrayService, stateRepo);
    }
    
    public ConfigVM getConfigVM() {
        return configVM;
    }
    
    public ProductTrayVM getProductTrayVM() {
        return productTrayVM;
    }
    
    public NotifierVM getNotifierVM() {
        return notifierVM;
    }
    
    
    
    public void setTtbKey(String ttbKey) {
        ttbKeyHolder.setTTBKey(ttbKey);
    }
    public String getTtbKey() {
        return ttbKeyHolder.getTTBKey();
    }
}
