package com.github.casiowatch123.aladinobserver.viewmodel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observable <T> {
    private T value;
    private final List<Consumer<T>> observers = new ArrayList<>();
    
    public Observable(T value) {
        this.value = value;
    }
    
    public void set(T newValue) {
        this.value = newValue;
        new ArrayList<>(observers).forEach(observer -> observer.accept(newValue));
    }
    
    public Runnable subscribe(Consumer<T> observer) {
        observers.add(observer);
        observer.accept(value);
        return () -> observers.remove(observer);
    }
}
