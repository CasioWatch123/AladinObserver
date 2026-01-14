package com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO;

public interface MapHistoryObject<K, V> {
    void put(K key, V history);
    
    V get(K key);
    
    boolean isValidKey(K key);
    
    boolean isEmpty();
}
