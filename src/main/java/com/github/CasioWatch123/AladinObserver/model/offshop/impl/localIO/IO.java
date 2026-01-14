package com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.HistoryObjectDeque;

public interface IO<T> {
    HistoryObjectDeque<T> readLines(int n);
    
    void write(HistoryObject<T> detail);
}
