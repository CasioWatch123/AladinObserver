package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckResult {
    private final String itemId;
    private final LocalDateTime timestamp;
    private final List<String> OffShopResult;
    
    public static final List<String> EMPTY_ARRAY = Collections.unmodifiableList(new ArrayList<>());
    public static final List<String> EXCEPTION_ARRAY = List.copyOf(List.of("exception occurred"));
    
    public CheckResult(String itemId, List<String> OffShopResult, LocalDateTime timestamp) {
        this.itemId = itemId;
        this.timestamp = timestamp;
        if (OffShopResult.isEmpty()) {
            this.OffShopResult = EMPTY_ARRAY;
        } else {
            this.OffShopResult = OffShopResult;
        }
    }
    
    public static CheckResult getEmptyCheckResult(String itemId, LocalDateTime timestamp) {
        return new CheckResult(itemId, EMPTY_ARRAY, timestamp);
    }

    public static CheckResult getEmptyCheckResult(String itemId) {
        return new CheckResult(itemId, EMPTY_ARRAY, LocalDateTime.now());
    }
    
    public static CheckResult getExceptionalCheckResult(String itemId, LocalDateTime timestamp) {
        return new CheckResult(itemId, EXCEPTION_ARRAY, timestamp);
    }

    public static CheckResult getExceptionalCheckResult(String itemId) {
        return new CheckResult(itemId, EXCEPTION_ARRAY, LocalDateTime.now());
    }
    public String getItemId() {
        return itemId;
    }
    
    public List<String> getOffShopResult() {
        return Collections.unmodifiableList(OffShopResult);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
