package com.github.casiowatch123.aladinobserver.view;

import com.github.casiowatch123.aladinobserver.viewmodel.MainVM;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class TTBKeySetWindow extends JDialog {
    private static final int MAX_TTBKEY_LENGTH = 30;
    private final MainVM mainVM;
    private String ttbKey;
    public TTBKeySetWindow(JFrame parent, MainVM mainVM) {
        super(parent, "TTBKey setting", true);
        
        this.mainVM = mainVM;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(500, 150);
        setLocationRelativeTo(null);//창 열릴 때 화면 중앙에 배치
        
        //initialize...
        this.ttbKey = mainVM.getTtbKey();

        initUI();

        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
    //generate bottom section
        JButton applyBotton = new JButton("apply");
        applyBotton.addActionListener(e -> {
            dispose();
            apply();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        bottomPanel.add(applyBotton, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    //generate center section(setting opts)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        //generate label
        JLabel label = new JLabel("TTBKey : ");
        label.setAlignmentY(Component.TOP_ALIGNMENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerPanel.add(label);

        centerPanel.add(Box.createVerticalStrut(5));
        
        //generate text field
        JTextField textField = new JTextField(ttbKey);
        textField.setAlignmentY(Component.TOP_ALIGNMENT);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }

            private void changed() {
                ttbKey = textField.getText();
            }
        });
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length,
                                String text, AttributeSet attrs)
                    throws BadLocationException {

                if (fb.getDocument().getLength() < MAX_TTBKEY_LENGTH) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        centerPanel.add(textField);
        
        centerPanel.add(Box.createVerticalStrut(8));
        
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void apply() {
        new SwingWorker<Void, Void>() {
            
            @Override
            protected Void doInBackground() {
                mainVM.setTtbKey(ttbKey);
                return null;
            }
        }.execute();
    }
}
