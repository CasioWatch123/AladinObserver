package com.github.casiowatch123.aladinobserver.view;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.view.impl.*;
import com.github.casiowatch123.aladinobserver.viewmodel.ProductTrayVM;

import javax.swing.*;
import java.awt.*;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class ProductTrayPanel extends JPanel {
    private final ProductTrayVM productTrayVM;
    private final JScrollPane scrollPane = new JScrollPane();
    private Runnable unSubscribe;
    private ProductTrayPanel(ProductTrayVM productTrayVM) {
        this.productTrayVM = productTrayVM;
    }
    
    public static ProductTrayPanel newInstance(ProductTrayVM productTrayVM) {
        ProductTrayPanel instance = new ProductTrayPanel(productTrayVM);
        instance.initUI();

        instance.unSubscribe = productTrayVM.subscribeTray(instance::updateCallback);
        
        return instance;
    }
    
    private void initUI() {
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
    //generate top side
        JTextField textField = new JTextField(15);
        textField.setMaximumSize(textField.getPreferredSize());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        JButton addButton = new LowerCaseTextButton("add");
        addButton.addActionListener(e -> {
            // vm/model work. not use swing worker(light work)
            productTrayVM
                    .addProduct(textField.getText())
                    .whenComplete((dataSet, T) -> {
                        updateCallback(dataSet);
                    });
            textField.setText("");
        });
        
        JButton removeButton = new LowerCaseTextButton("remove");
        removeButton.addActionListener(e -> {
            // vm/model work. not use swing worker(light work)
            productTrayVM
                    .removeProduct(textField.getText())
                    .whenComplete((dataSet, T) -> {
                        updateCallback(dataSet);
                    });
            textField.setText("");
        });
        
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(textField);
        buttonPanel.add(Box.createHorizontalStrut(3));
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(3));
        buttonPanel.add(removeButton);
        
        add(buttonPanel, BorderLayout.NORTH);
    }
    
    private void updateCallback(Set<AladinProductData> aladinProductDataSet) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new ScrollFitPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            aladinProductDataSet.forEach(productData -> {
                JPanel productPanel = ProductPanel.newInstance(productData);
                
                panel.add(productPanel);
                panel.add(Box.createVerticalStrut(8));
            });
            
            this.scrollPane.setViewportView(panel);
            this.scrollPane.revalidate();
            this.scrollPane.repaint();

            SwingUtilities.invokeLater(() -> {
                scrollPane.getViewport()
                        .setViewPosition(new Point(0, 0));
            });
        });
    }
    
    public void dispose() {
        if (unSubscribe != null) {
            unSubscribe.run();
            unSubscribe = null;
        }
    }
    
    private static class ProductPanel extends RoundPanel {
        private static final DateTimeFormatter FORMATTER =  DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
        private static final Color bgPanelColor = new Color(51, 51, 51);
        
        private final AladinProductData productData;
        private ProductPanel(AladinProductData productData) {
            super(10);
            this.productData = productData;
        }
        
        private static ProductPanel newInstance(AladinProductData productData) {
            ProductPanel instance = new ProductPanel(productData);

            instance.setLayout(new BorderLayout(4, 4));

            instance.setMaximumSize(new Dimension(Short.MAX_VALUE, 140));
            instance.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));

            instance.setBackground(ProductPanel.bgPanelColor);

            instance.add(instance.createThumbnailPanel(), BorderLayout.WEST);

            instance.add(instance.createProductInfoPanel(), BorderLayout.CENTER);

            return instance;
        }
        
        private JPanel createThumbnailPanel() {
            JPanel imageJPanel = new ItemImageJPanel(productData.itemImage());

            JPanel imageHolder = new JPanel();
            imageHolder.setOpaque(false);
            imageHolder.setLayout(new BorderLayout());
            imageHolder.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            imageHolder.add(imageJPanel, BorderLayout.CENTER);

            imageHolder.setPreferredSize(new Dimension(100, 140));
            imageHolder.setMaximumSize(imageHolder.getPreferredSize());
            imageHolder.setMinimumSize(imageHolder.getPreferredSize());

            imageHolder.setMinimumSize(new Dimension(0, imageHolder.getPreferredSize().height));
            return imageHolder;
        }
        
        private JPanel createProductInfoPanel() {
            RoundPanel infoPanel = new RoundPanel(10);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            infoPanel.setLayout(new BorderLayout(0, 4));

            //create product identity panel
            JPanel idenPanel = new JPanel();
            idenPanel.setLayout(new BorderLayout());
            idenPanel.setBorder(BorderFactory.createEmptyBorder(0, 4,4,4));

            JLabel nameLabel = new EllipsisLabel(productData.itemName());
            JLabel idLabel = new LowerCaseLabel("id : " + productData.itemId());
            nameLabel.setFont(nameLabel.getFont().deriveFont(16f));
            nameLabel.setBorder(BorderFactory.createEmptyBorder(-2, 0, 0, 0));

            idenPanel.add(nameLabel, BorderLayout.CENTER);
            idenPanel.add(idLabel, BorderLayout.EAST);

            infoPanel.add(idenPanel, BorderLayout.NORTH);

            //create history panel
            JPanel historyPanel = createHistoryLogPanel();

            infoPanel.add(historyPanel, BorderLayout.CENTER);

            //add "view more" button
            JButton viewMoreButton = new LowerCaseTextButton("view more..");
            viewMoreButton.addActionListener(e -> {
                JDialog dialog = new JDialog(
                        SwingUtilities.getWindowAncestor(this),
                        String.format(
                                "%s(%s)",
                                productData.itemName(),
                                productData.itemId()), 
                        Dialog.ModalityType.APPLICATION_MODAL
                );
                dialog.setSize(450, 300);
                dialog.setResizable(false);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setLocationRelativeTo(null);
                
                dialog.setLayout(new BorderLayout());
                
                JTextArea textArea = new JTextArea();
                textArea.setLineWrap(false);
                textArea.setWrapStyleWord(false);
                textArea.setTabSize(4);
                textArea.setEnabled(false);

                List<OffshopCheckResult> historyList = productData.getHistoryList();
                
                Deque<String> stringDeque = new ArrayDeque<>();
                
                historyList.forEach(checkResult -> {
                    stringDeque.addFirst("\n");
                    stringDeque.addFirst(historyToString(checkResult, false));
                });
                
                textArea.append(String.join("", stringDeque));
                
                JScrollPane scroll = new JScrollPane(
                        textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                );
                scroll.setWheelScrollingEnabled(true);
                scroll.getVerticalScrollBar().setUnitIncrement(16);

                JPanel holderPanel = new JPanel();
                holderPanel.setLayout(new BorderLayout());
                holderPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                holderPanel.add(scroll, BorderLayout.CENTER);
                
                dialog.add(holderPanel, BorderLayout.CENTER);
                
                dialog.setVisible(true);
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.add(viewMoreButton, BorderLayout.EAST);

            infoPanel.add(buttonPanel, BorderLayout.SOUTH);
            infoPanel.setMinimumSize(new Dimension(0, infoPanel.getPreferredSize().height));
            
            return infoPanel;
        }
        
        private JPanel createHistoryLogPanel() {
            JPanel basePanel = new JPanel();
            basePanel.setLayout(new BorderLayout());
            
            JPanel roundPanel = new RoundPanel(10);
            roundPanel.setLayout(new BorderLayout());
            roundPanel.setBorder(BorderFactory.createCompoundBorder(
                    RoundPanel.createRoundBorder(1, Color.BLACK, 10), 
                    BorderFactory.createEmptyBorder(2,2,2,2)
            ));
            
            JTextArea textArea = new JTextArea();
            textArea.setBorder(BorderFactory.createEmptyBorder(-2, 1, 2, 0));
            textArea.setEnabled(false);
            textArea.setBackground(UIManager.getColor("Panel.background"));

            Deque<String> stringDeque = new ArrayDeque<>();
            
            productData.getHistoryList()
                    .stream()
                    .limit(3)
                    .forEach(checkResult -> {
                        stringDeque.addFirst("\n");
                        stringDeque.addFirst(historyToString(checkResult, true));
                    });
            
            textArea.append(String.join("", stringDeque));
            
            roundPanel.add(textArea, BorderLayout.CENTER);
            
            basePanel.add(roundPanel, BorderLayout.CENTER);
            
            return basePanel;
        }
        
        private String historyToString(OffshopCheckResult checkResult, boolean isCompress) {
            StringWriter writer = new StringWriter();

            writer.append(String.format("%s : ", checkResult.getTimestamp().format(FORMATTER)));

            if (checkResult.isExceptional()) {
                writer.append("exception occurred");
            } else if (checkResult.isEmpty()) {
                writer.append("empty");
            } else {
                writer.append(String.format(
                        "%2d shop > ",
                        checkResult.getOffshopList().size()
                ));
                
                if (isCompress) {
                    checkResult.getOffshopList()
                            .stream()
                            .limit(2)
                            .forEach(name -> writer.append(name).append(" "));
                    writer.append("...");
                } else {
                    checkResult.getOffshopList()
                            .forEach(name -> writer.append(name).append(" "));
                }
            }
            
            return writer.toString();
        }
    }
}
