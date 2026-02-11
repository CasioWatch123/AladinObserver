package com.github.casiowatch123.aladinobserver.view.impl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;

public class EllipsisLabel extends JLabel {
    private String original;

    public EllipsisLabel(String text) {
        super(text);
        this.original = text;
//
//        addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                updateText();
//            }
//        });

//        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
//            @Override
//            public void ancestorResized(HierarchyEvent e) {
//                Container parent = getParent();
//                if (parent == null) return;
//
//                Insets insets = parent.getInsets();
//                int parentWidth = parent.getWidth() - insets.left - insets.right;
//
//                setSize(parentWidth, getHeight());
//                System.out.println(getWidth() + " " + getHeight());
//                updateText();
//            }
//        });
    }

    @Override
    public void setText(String text) {
        this.original = text;
        super.setText(text);
        updateText();
    }

    private void updateText() {
        if (getWidth() <= 0) return;

        FontMetrics fm = getFontMetrics(getFont());
        String clipped = ellipsize(original, fm, getWidth() - getInsets().left - getInsets().right);
        super.setText(clipped);
    }

    private static String ellipsize(String s, FontMetrics fm, int maxWidth) {

        if (s == null) return null;
        if (fm.stringWidth(s) <= maxWidth) return s;

        String ellipsis = "...";
        int ellW = fm.stringWidth(ellipsis);

        int width = 0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            int w = fm.stringWidth(ch);

            if (width + w + ellW > maxWidth) break;

            sb.append(ch);
            width += w;
            i += Character.charCount(cp);
        }

        return sb.append(ellipsis).toString();
    }
}
