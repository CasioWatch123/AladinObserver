package com.github.casiowatch123.aladinobserver.view;

import com.github.casiowatch123.aladinobserver.Disposable;
import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.viewmodel.NotifierVM;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TrayIconHandler implements Disposable {
    private static final BufferedImage ICON_IMAGE;
    private final TrayIcon trayIcon;
    private final PopupMenu popupMenu = new PopupMenu();
    
    private volatile boolean started = false;
    private final AtomicBoolean terminateFlag = new AtomicBoolean(false);
    
    private Runnable unsubscribeCommand;
    private final NotifierVM notifierVM;
    
    static {
        try (InputStream in = TrayIconHandler.class
                .getClassLoader()
                .getResourceAsStream("TrayIcon.png")) {
            ICON_IMAGE = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public TrayIconHandler(NotifierVM notifierVM, Runnable clickEvent) {
        this.notifierVM = notifierVM;
        
        trayIcon = new TrayIcon(ICON_IMAGE, "Aladin Observer");
        addMenuItem("open main view", clickEvent);
        trayIcon.setImageAutoSize(true);
    }
    
    public CompletableFuture<Boolean> start() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        SwingUtilities.invokeLater(() -> {
            try {
                if (!started) {
                    unsubscribeCommand = notifierVM.subscribeProductChanges(this::notify);
                    trayIcon.setPopupMenu(popupMenu);
                    SystemTray.getSystemTray().add(trayIcon);
                    started = true;
                }
                result.complete(true);
            } catch (AWTException e) {
                Logger.getInstance().writeLog(e);
                result.completeExceptionally(e);
            }
        });
        return result.exceptionally(ex -> {
            System.out.println("tray configuration exception");
            return false;
        });
    }
    
    public void notify(Set<AladinProductData> dataSet) {
        SwingUtilities.invokeLater(() -> {
            if (started) {
                String result;
                if (dataSet.size() > 2) {
                    String str = dataSet.stream()
                            .limit(2)
                            .map(data -> String.format("%s(%s)", data.itemName(), data.itemId()))
                            .collect(Collectors.joining(", "));
                    
                    result = str + String.format("... %s more", dataSet.size()-2);
                } else {
                    result = dataSet.stream()
                            .map(data -> String.format("%s(%s)", data.itemName(), data.itemId()))
                            .collect(Collectors.joining(", "));
                }
                
                trayIcon.displayMessage(
                        "off shop stock changed",
                        result,
                        TrayIcon.MessageType.INFO
                );
            }
        });
    }
    
    public void addMenuItem(String name, Runnable command) {
        MenuItem item = new MenuItem(name);
        item.addActionListener(e -> command.run());
        
        popupMenu.add(item);
        popupMenu.addSeparator();
    }
    
    public void dispose() {
        if (terminateFlag.compareAndSet(false, true) && started) {
            unsubscribeCommand.run();
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
