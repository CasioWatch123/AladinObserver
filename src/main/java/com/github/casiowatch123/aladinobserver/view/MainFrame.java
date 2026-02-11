package com.github.casiowatch123.aladinobserver.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.config.ConfigRegistry;
import com.github.casiowatch123.aladinobserver.model.config.StateRepo;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTrayImpl;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.storage.TextDataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.test.FakeDataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.test.FakeProductTray;
import com.github.casiowatch123.aladinobserver.model.ttbkey.TTBKeyHolder;
import com.github.casiowatch123.aladinobserver.view.impl.LowerCaseTextButton;
import com.github.casiowatch123.aladinobserver.viewmodel.MainVM;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final MainVM mainVM;
    private final Runnable terminateCommand;
    
    public MainFrame(MainVM mainVM, Runnable terminateCommand) {
        this.mainVM = mainVM;
        this.terminateCommand = terminateCommand;
        
        setTitle("Aladin Observer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setResizable(true);
        setSize(800, 500);
        setLocationRelativeTo(null);//창 열릴 때 화면 중앙에 배치
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        
    //create config button
        JButton configButton = new LowerCaseTextButton("config");
        configButton.addActionListener(e -> {
            new ConfigWindow(
                    this, 
                    mainVM.getConfigVM(),
                    terminateCommand
            );
        });
        
    //create ttb setting button
        JButton ttbSetButton = new JButton("set TTBKey");
        ttbSetButton.addActionListener(e -> {
            System.out.println("ttb set button clicked");
            new TTBKeySetWindow(this, mainVM);
        });
    
    //create open api info label
        JLabel openapiInfoLabel = new JLabel("api provided by Aladin OpenAPI");
        
        bottomPanel.add(openapiInfoLabel, BorderLayout.SOUTH);
        bottomPanel.add(ttbSetButton, BorderLayout.WEST);
        bottomPanel.add(configButton, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
    //create center panel
        ProductTrayPanel trayPanel = ProductTrayPanel.newInstance(mainVM.getProductTrayVM());
        mainPanel.add(trayPanel, BorderLayout.CENTER);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                trayPanel.dispose();
            }
        });
        
        add(mainPanel);
    }
    
    public void bringToFront() {
        if (!isVisible()) {
            setVisible(true);
        }

        // 최소화 상태 복구
        setExtendedState(JFrame.NORMAL);

        // 최상단으로 이동
        toFront();

        // 포커스 요청
        requestFocus();
    }
    
    public void openWindow() {
        setVisible(true);
        
        setAlwaysOnTop(true);
        toFront();
        setAlwaysOnTop(false);
    }
    public static MainVM createMainVM() {
        System.out.println("initializing...");
        DataStorageFactory dataStorageFactory = new TextDataStorageFactory();
        TTBKeyHolder ttbKeyHolder = new TTBKeyHolder(dataStorageFactory);
        OffShopProductTrayService productTrayService = new OffShopProductTrayService(new ProductTrayImpl(ttbKeyHolder), dataStorageFactory);
        StateRepo stateRepo = new StateRepo();

        ConfigRegistry configRegistry = new ConfigRegistry(productTrayService, stateRepo, dataStorageFactory);
        
        return new MainVM(ttbKeyHolder, productTrayService, configRegistry, stateRepo);
    }
    
    public static MainVM createTestMainVM() {
        System.out.println("initializing test MainVM...");
        DataStorageFactory dataStorageFactory = new FakeDataStorageFactory();
        TTBKeyHolder ttbKeyHolder = new TTBKeyHolder(dataStorageFactory);
        OffShopProductTrayService productTrayService = new OffShopProductTrayService(FakeProductTray.newInstanceWithUpdate(), dataStorageFactory);
        StateRepo stateRepo = new StateRepo();

        ConfigRegistry configRegistry = new ConfigRegistry(productTrayService, stateRepo, dataStorageFactory);
        return new MainVM(ttbKeyHolder, productTrayService, configRegistry, stateRepo);
    }
}
