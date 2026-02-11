package com.github.casiowatch123.aladinobserver;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.config.StateRepo;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTrayImpl;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.storage.TextDataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.test.FakeDataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.test.FakeProductTray;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyHolder;
import com.github.casiowatch123.aladinobserver.view.MainFrameHandler;
import com.github.casiowatch123.aladinobserver.view.TrayIconHandler;
import com.github.casiowatch123.aladinobserver.viewmodel.MainVM;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Application {
    private final List<Runnable> disposableList = new ArrayList<>();
    private final AtomicBoolean terminateFlag = new AtomicBoolean(false);
    
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        new Application().start();
    }
    
    public void start() {
        //create VM
        MainVM mainVM = createMainVM();
        
        //create view
        MainFrameHandler mainFrameHandler = new MainFrameHandler(mainVM, this::terminate);
        onTerminate(mainFrameHandler::dispose);

        //create tray icon
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported. terminate");
            terminate();
            return;
        }
        TrayIconHandler trayIconHandler = new TrayIconHandler(mainVM.getNotifierVM(), mainFrameHandler::openMainFrame);
        onTerminate(trayIconHandler::dispose);
        trayIconHandler.addMenuItem("quit", this::terminate);
        
        
        //service start
        if (!trayIconHandler.start().join()) {
            terminate();
        }
        mainFrameHandler.openMainFrame();
    }
    
    private void terminate() {
        if (terminateFlag.compareAndSet(false, true)) {
            disposableList.forEach(Runnable::run);
        }
//        try (ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1)) {
//            System.out.println("terminate!");
//            scheduler.schedule(() -> System.exit(0), 2, TimeUnit.SECONDS);
//        }
    }
    
    private void onTerminate(Runnable command) {
        disposableList.add(command);
    }
    
    
    
    private MainVM createMainVM() {
        //create model
        DataStorageFactory dataStorageFactory = new TextDataStorageFactory();
        TTBKeyHolder ttbKeyHolder = new TTBKeyHolder(dataStorageFactory);
        OffShopProductTrayService productTrayService = new OffShopProductTrayService(new ProductTrayImpl(ttbKeyHolder), dataStorageFactory);
        StateRepo stateRepo = new StateRepo();

        ConfigRegistry configRegistry = new ConfigRegistry(productTrayService, stateRepo, dataStorageFactory);

        onTerminate(productTrayService::shutdown);
        
        return new MainVM(ttbKeyHolder, productTrayService, configRegistry, stateRepo);
    }
    
    private MainVM createTestMainVM() {
        DataStorageFactory dataStorageFactory = new FakeDataStorageFactory();
        TTBKeyHolder ttbKeyHolder = new TTBKeyHolder(dataStorageFactory);
        OffShopProductTrayService productTrayService = new OffShopProductTrayService(FakeProductTray.newInstanceWithUpdate(), dataStorageFactory);
        StateRepo stateRepo = new StateRepo();

        ConfigRegistry configRegistry = new ConfigRegistry(productTrayService, stateRepo, dataStorageFactory);

        onTerminate(productTrayService::shutdown);

        return new MainVM(ttbKeyHolder, productTrayService, configRegistry, stateRepo);
    }
}