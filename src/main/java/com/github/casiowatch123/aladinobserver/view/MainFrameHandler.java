package com.github.casiowatch123.aladinobserver.view;

import com.github.casiowatch123.aladinobserver.Disposable;
import com.github.casiowatch123.aladinobserver.viewmodel.MainVM;

import javax.swing.*;

public class MainFrameHandler implements Disposable {
    private final MainVM mainVM;
    private final Runnable terminateCommand;
    
    private MainFrame mainFrame;
    
    public MainFrameHandler(MainVM mainVM, Runnable terminateCommand) {
        this.mainVM = mainVM;
        this.terminateCommand = terminateCommand;
    }
    
    //Use in EDT!!
    public void openMainFrame() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame == null || !mainFrame.isDisplayable()) {
                mainFrame = new MainFrame(mainVM, terminateCommand);
                mainFrame.openWindow();
            } else {
                mainFrame.bringToFront();
            }
        });
    }
    
    @Override
    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && mainFrame.isDisplayable()) {
                mainFrame.dispose();
            }
        });
    }
}
