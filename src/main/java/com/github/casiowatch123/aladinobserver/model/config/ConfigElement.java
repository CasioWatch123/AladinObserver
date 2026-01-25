package com.github.casiowatch123.aladinobserver.model.config;

import com.github.casiowatch123.aladinobserver.log.Logger;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigElement<T> {
    private final Class<T> type;
    private final Consumer<T> applier;
    private final BiPredicate<T, T> equals;
    private final Function<T, String> serializer;
    private final Function<String, T> deSerializer;
    
    private T value;
    
    
    public ConfigElement(
            Class<T> type, 
            Consumer<T> applier, 
            BiPredicate<T, T> equals, 
            Function<T, String> serializer, 
            Function<String, T> deSerializer) {
        this.type = type;
        this.applier = applier;
        this.equals = equals;
        this.serializer = serializer;
        this.deSerializer = deSerializer;
    }
    
    public Boolean applyIfChanged(Object value) {
        try {
            T castedValue = type.cast(value);

            if (this.value != null && equals.test(this.value, castedValue)) {
                return false;
            }
            
            apply(value);
            return true;
        } catch (ClassCastException e) {
            Logger.getInstance().writeLog(e);
            return false;
        }
    }
    public void apply(Object value) {
        try {
            T castedValue = type.cast(value);

            this.applier.accept(castedValue);
            this.value = castedValue;
        } catch (ClassCastException e) {
            Logger.getInstance().writeLog(e);
        }
    }
    
    public void init(String value) {
        apply(deSerializer.apply(value));
    }
    public String getSerializedValue() {
        return serializer.apply(value);
    }
    
    public T value() {
        return value;
    }
}
