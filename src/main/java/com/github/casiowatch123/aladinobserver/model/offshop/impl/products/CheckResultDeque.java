package com.github.casiowatch123.aladinobserver.model.offshop.impl.products;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.HistoryPolicies;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;

public class CheckResultDeque {
    private final AtomicReference<Deque<OffshopCheckResult>> historyRef;

    public CheckResultDeque() {
        historyRef = new AtomicReference<>(new ArrayDeque<>());
    }

    public void addHistoryLast(OffshopCheckResult offshopCheckResult) {
        while (true) {
            Deque<OffshopCheckResult> oldDeque = historyRef.get();
            Deque<OffshopCheckResult> newDeque = new ArrayDeque<>(oldDeque);
            if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                return;
            }

            newDeque.addLast(offshopCheckResult);

            if (historyRef.compareAndSet(oldDeque, newDeque)) {
                return;
            }
        }
    }

    public void addHistoryFirst(OffshopCheckResult offshopCheckResult) {
        while (true) {
            Deque<OffshopCheckResult> oldDeque = historyRef.get();
            Deque<OffshopCheckResult> newDeque = new ArrayDeque<>(oldDeque);
            if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                newDeque.removeLast();
            }

            newDeque.addFirst(offshopCheckResult);

            if (historyRef.compareAndSet(oldDeque, newDeque)) {
                return;
            }
        }
    }

    public Deque<OffshopCheckResult> getDeque() {
        return new ArrayDeque<>(historyRef.get());
    }
}
