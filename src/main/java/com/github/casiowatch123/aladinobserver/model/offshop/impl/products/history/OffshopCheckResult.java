package com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OffshopCheckResult {
    public static final List<String> EMPTY_ARRAY = Collections.unmodifiableList(new ArrayList<>());
    public static final List<String> EXCEPTION_ARRAY = List.copyOf(List.of("exception occurred"));
    
    private final String itemId;
    private final LocalDateTime timestamp;
    private final List<String> OffshopList;
    
    
    public OffshopCheckResult(String itemId, List<String> OffshopList, LocalDateTime timestamp) {
        this.itemId = itemId;
        this.timestamp = timestamp;
        if (OffshopList == null || OffshopList.isEmpty()) {
            this.OffshopList = EMPTY_ARRAY;
        } else {
            this.OffshopList = OffshopList;
        }
    }
    
    public static OffshopCheckResult getEmptyCheckResult(String itemId, LocalDateTime timestamp) {
        return new OffshopCheckResult(itemId, EMPTY_ARRAY, timestamp);
    }

    public static OffshopCheckResult getEmptyCheckResult(String itemId) {
        return new OffshopCheckResult(itemId, EMPTY_ARRAY, LocalDateTime.now());
    }
    
    public static OffshopCheckResult getExceptionalCheckResult(String itemId, LocalDateTime timestamp) {
        return new OffshopCheckResult(itemId, EXCEPTION_ARRAY, timestamp);
    }

    public static OffshopCheckResult getExceptionalCheckResult(String itemId) {
        return new OffshopCheckResult(itemId, EXCEPTION_ARRAY, LocalDateTime.now());
    }
    public String getItemId() {
        return itemId;
    }
    
    public List<String> getOffshopList() {
        return Collections.unmodifiableList(OffshopList);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Boolean isExceptional() {
        return this.OffshopList.contains("exception occurred");
    }
}
