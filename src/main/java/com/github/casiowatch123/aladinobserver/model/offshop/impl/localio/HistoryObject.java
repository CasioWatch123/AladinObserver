package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio;

import java.util.HashMap;
import java.util.Map;

public class HistoryObject<T>{
    private final Map<String, T> historyMap;
    
    public HistoryObject() {
        this.historyMap = new HashMap<>();
    }
    
    public void put(String key, T history) {
        historyMap.put(key, history);
    }

    public T get(String key) {
        return historyMap.get(key);
    }

    public boolean isValidKey(String key) {
        return historyMap.containsKey(key);
    }

    public boolean isEmpty() {
        return historyMap.isEmpty();
    }
}
