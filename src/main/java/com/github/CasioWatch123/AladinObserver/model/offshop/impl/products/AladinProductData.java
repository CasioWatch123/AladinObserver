package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.CheckResult;

import java.awt.*;
import java.util.Deque;

public interface AladinProductData {


    Image itemImage();

    String itemId();

    String itemName();

    Deque<CheckResult> getHistories();
    
    CheckResult getHistoryFirst();
}
