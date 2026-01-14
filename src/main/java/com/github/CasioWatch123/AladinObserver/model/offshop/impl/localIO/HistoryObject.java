package com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO;

import java.util.HashMap;
import java.util.Map;

public class HistoryObject<T> implements MapHistoryObject<String, T>{
    private final Map<String, T> historyMap;
    
    public HistoryObject() {
        this.historyMap = new HashMap<>();
    }
    
    @Override
    public void put(String key, T history) {
        historyMap.put(key, history);
    }

    @Override
    public T get(String key) {
        return historyMap.get(key);
    }

    @Override
    public boolean isValidKey(String key) {
        return historyMap.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return historyMap.isEmpty();
    }
}
