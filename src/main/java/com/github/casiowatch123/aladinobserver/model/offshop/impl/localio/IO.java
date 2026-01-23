package com.github.casiowatch123.aladinobserver.model.offshop.impl.localio;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryObjectDeque;

public interface IO<T> {
    HistoryObjectDeque<T> readLines(int n);
    
    void write(HistoryObject<T> detail);
}
