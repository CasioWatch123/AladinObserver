package com.github.casiowatch123.aladinobserver.view.impl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ItemImageJPanel extends JPanel {
    public static final int MAX_WIDTH = 100;
    public static final int MAX_HEIGHT = 140;
    private final BufferedImage image;

    public ItemImageJPanel(BufferedImage image) {
        this.image = image;
//        setPreferredSize(preferredSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(
                    image, 0, 0, getWidth(), getHeight(), 
                    2, 2, image.getWidth()-2, image.getHeight()-2, 
                    this);
        }
    }
}
