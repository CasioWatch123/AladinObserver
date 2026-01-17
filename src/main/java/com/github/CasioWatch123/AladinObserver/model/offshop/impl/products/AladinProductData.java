package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.OffshopCheckResult;

import java.awt.*;
import java.util.Deque;

public interface AladinProductData {
    OffshopCheckResult getPreviousCheckResult();

    Image itemImage();

    String itemId();

    String itemName();

    Deque<OffshopCheckResult> getHistories();
    
    OffshopCheckResult getHistoryFirst();
}
