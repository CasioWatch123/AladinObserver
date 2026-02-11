package com.github.casiowatch123.aladinobserver.view.impl;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class RoundPanel extends JPanel {
    private final int arc;

    public RoundPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
        setBackground(UIManager.getColor("Panel.background"));
    }

    public static Border createRoundBorder(int length, Color color, int arc) {
        return new AbstractBorder() {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(length, length, length, length);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(length, length, length, length);
                return insets;
            }

            @Override
            public void paintBorder(Component c, Graphics g,
                                    int x, int y, int width, int height) {

                if (color == null) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                float stroke = length;
                g2.setStroke(new BasicStroke(stroke));

                float offset = stroke / 2f;

                Shape shape = new RoundRectangle2D.Float(
                        x + offset,
                        y + offset,
                        width - stroke,
                        height - stroke,
                        arc, arc
                );
                
                g2.draw(shape);
                g2.dispose();
            }
        };
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = getBackground();
        g2.setColor(bg);

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 
                arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
    
}