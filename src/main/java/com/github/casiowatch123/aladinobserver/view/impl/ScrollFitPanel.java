package com.github.casiowatch123.aladinobserver.view.impl;

import javax.swing.*;
import java.awt.*;

public class ScrollFitPanel extends JPanel implements Scrollable {

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;   // 가로를 JScrollPane에 맞춤
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;  // 세로는 컨텐츠 크기 유지 → 세로 스크롤
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }
}