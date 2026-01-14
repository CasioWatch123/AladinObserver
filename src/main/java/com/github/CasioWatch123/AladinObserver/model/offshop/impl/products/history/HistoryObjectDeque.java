package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.localIO.HistoryObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;

public class HistoryObjectDeque<T> {
    private final AtomicReference<Deque<HistoryObject<T>>> historyRef;

    public HistoryObjectDeque() {
        historyRef = new AtomicReference<>(new ArrayDeque<>());
    }

    public void addHistoryLast(HistoryObject<T> historyObject) {
        while (true) {
            Deque<HistoryObject<T>> oldDeque = historyRef.get();
            Deque<HistoryObject<T>> newDeque = new ArrayDeque<>(oldDeque);
            
            if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                return;
            }
            newDeque.addLast(historyObject);

            if (historyRef.compareAndSet(oldDeque, newDeque)) {
                return;
            }
        }
    }

    public void addHistoryFirst(HistoryObject<T> historyObject) {
        while (true) {
            Deque<HistoryObject<T>> oldDeque = historyRef.get();
            Deque<HistoryObject<T>> newDeque = new ArrayDeque<>(oldDeque);

            if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                newDeque.removeLast();
            }
            
            newDeque.addFirst(historyObject);

            if (historyRef.compareAndSet(oldDeque, newDeque)) {
                return;
            }
        }
    }

    public void addHistory(HistoryObject<T> historyObject) {
        addHistoryFirst(historyObject);
    }
    
    public Deque<HistoryObject<T>> getDeque() {
        return new ArrayDeque<>(historyRef.get());
    }
}