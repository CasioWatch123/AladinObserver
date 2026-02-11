package com.github.casiowatch123.aladinobserver.model.offshop.impl.products;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;

import java.awt.image.BufferedImage;
import java.util.List;

public interface AladinProductData {
    OffshopCheckResult getPreviousCheckResult();

    BufferedImage itemImage();

    String itemId();

    String itemName();

    List<OffshopCheckResult> getHistoryList();
    
    OffshopCheckResult getHistoryFirst();
}
