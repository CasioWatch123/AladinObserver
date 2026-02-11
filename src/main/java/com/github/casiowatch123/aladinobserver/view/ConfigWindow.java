package com.github.casiowatch123.aladinobserver.view;

import com.github.casiowatch123.aladinobserver.view.impl.LowerCaseLabel;
import com.github.casiowatch123.aladinobserver.view.impl.LowerCaseTextButton;
import com.github.casiowatch123.aladinobserver.viewmodel.ConfigVM;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ConfigWindow extends JDialog {
    private final ConfigVM configVM;
    private final Runnable terminateCommand;
    
    private long runPeriod;
    private boolean executionEnabled;
    private boolean notifyingEnabled;
    
    
    public ConfigWindow(JFrame parent, ConfigVM configVM, Runnable terminateCommand) {
        super(parent, "config setting", true); // modal
        this.terminateCommand = terminateCommand;
        this.configVM = configVM;
        
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(350, 300);
        setLocationRelativeTo(null);//창 열릴 때 화면 중앙에 배치
        
        //initializing...
        runPeriod = configVM.getCurrentRunPeriod();
        executionEnabled = configVM.getCurrentExecutionFlag();
        notifyingEnabled = configVM.getCurrentNotificationFlag();
        
        initUI();

        setVisible(true);
    }
    
    private void initUI() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    //generate bottom section
        JButton terminateButton = new LowerCaseTextButton("application terminate");
        terminateButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    terminateCommand.run();
                    return null;
                }
            }.execute();
        });
        
        JButton applyBotton = new LowerCaseTextButton("apply");
        applyBotton.addActionListener(e -> {
            dispose();
            apply(runPeriod, executionEnabled, notifyingEnabled);
        });
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        
        bottomPanel.add(terminateButton, BorderLayout.WEST);
        bottomPanel.add(applyBotton, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    //generate center section(setting opts)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
    //running period opt
        JSlider periodSlider = new JSlider(
                Long.valueOf(ConfigVM.MIN_RUN_PERIOD).intValue(), 
                Long.valueOf(ConfigVM.MAX_RUN_PERIOD).intValue(),
                Long.valueOf(this.runPeriod).intValue());
        periodSlider.setMajorTickSpacing(10);
        periodSlider.setPaintLabels(true);
        periodSlider.setPaintTicks(true);
        
        JTextField periodTextField = new JTextField(String.valueOf(this.runPeriod));
        periodTextField.setMaximumSize(new Dimension(
                Short.MAX_VALUE, 
                periodTextField.getPreferredSize().height
        ));
        
        periodSlider.addChangeListener(e -> {
            if (!periodSlider.getValueIsAdjusting()) {
                periodTextField.setText(String.valueOf(periodSlider.getValue()));
                this.runPeriod = Integer.valueOf(periodSlider.getValue()).longValue();
            }
        });
        periodTextField.addActionListener(e -> {
            try {
                int v = Integer.parseInt(periodTextField.getText());
                periodSlider.setValue(v);
                periodTextField.setText(String.valueOf(periodSlider.getValue()));
            } catch (NumberFormatException ex) {
                periodTextField.setText(String.valueOf(periodSlider.getValue()));
            }
        });
        centerPanel.add(createConfigItem(
                "running period",
                BoxLayout.Y_AXIS,
                periodTextField, 
                periodSlider));
        centerPanel.add(Box.createVerticalStrut(5));
        
    //execution opt
        JRadioButton executionEnabled = new JRadioButton("enable");
        executionEnabled.addActionListener(e -> {
            this.executionEnabled = true;
        });
        JRadioButton executionDisabled = new JRadioButton("disable");
        executionDisabled.addActionListener(e -> {
            this.executionEnabled = false;
        });
        ButtonGroup executionGroup = new ButtonGroup();
        executionGroup.add(executionEnabled);
        executionGroup.add(executionDisabled);
        if (this.executionEnabled) {
            executionEnabled.setSelected(true);
        } else {
            executionDisabled.setSelected(true);
        }

        centerPanel.add(createConfigItem(
                "execution",
                BoxLayout.X_AXIS,
                executionEnabled,
                executionDisabled));
        centerPanel.add(Box.createVerticalStrut(5));

    //notifying opt
        JRadioButton notifyingEnabled = new JRadioButton("enable");
        notifyingEnabled.addActionListener(e -> {
            this.notifyingEnabled = true;
        });

        JRadioButton notifyingDisabled = new JRadioButton("disable");
        notifyingDisabled.addActionListener(e -> {
            this.notifyingEnabled = false;
        });
        ButtonGroup notifyingGroup = new ButtonGroup();
        notifyingGroup.add(notifyingEnabled);
        notifyingGroup.add(notifyingDisabled);
        if (this.notifyingEnabled) {
            notifyingEnabled.setSelected(true);
        } else {
            notifyingDisabled.setSelected(true);
        }

        centerPanel.add(createConfigItem(
                "notifying", 
                BoxLayout.X_AXIS, 
                notifyingEnabled, 
                notifyingDisabled));
        centerPanel.add(Box.createVerticalStrut(5));
        

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void apply(long runPeriod, boolean executionEnabled, boolean notifyingEnabled) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                configVM.setRunPeriod(runPeriod);
                configVM.trayObservingExecution(executionEnabled);
                configVM.notificationEnabled(notifyingEnabled);
                return null;
            }
        }.execute();
    }
    
    private JPanel createConfigItem(String title, int layoutOpt, JComponent... editors) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new LowerCaseLabel(title);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        
        JPanel editorPanel = new JPanel();
        
        if (layoutOpt == BoxLayout.X_AXIS) {
            editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
            editorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            editorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
            
            Arrays.asList(editors).forEach(editor -> {
                editorPanel.add(editor);
                editorPanel.add(Box.createHorizontalStrut(2));
            });
        } else {
            editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
            editorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            editorPanel.setAlignmentY(Component.TOP_ALIGNMENT);

            Arrays.asList(editors).forEach(editor -> {
                editorPanel.add(editor);
                editorPanel.add(Box.createVerticalStrut(2));
            });
        }
        panel.add(editorPanel);
        panel.add(Box.createVerticalStrut(1));
        return panel;
    }
}
