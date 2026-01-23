package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio;

public interface MapHistoryObject<K, V> {
    void put(K key, V history);
    
    V get(K key);
    
    boolean isValidKey(K key);
    
    boolean isEmpty();
}
