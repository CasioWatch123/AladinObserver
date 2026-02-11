package com.github.casiowatch123.aladinobserver.model.config;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigElement <T> {
    private final Consumer<T> applier;
    private final Function<T, T> normalizer;
    private final BiPredicate<T, T> equals;
    private final Function<T, String> serializer;
    private final Function<String, T> deSerializer;

    private T value;


    public ConfigElement(
            Consumer<T> applier,
            Function<T, T> normalizer, 
            BiPredicate<T, T> equals,
            Function<T, String> serializer,
            Function<String, T> deSerializer) {
        this.applier = applier;
        this.normalizer = normalizer;
        this.equals = equals;
        this.serializer = serializer;
        this.deSerializer = deSerializer;
    }
    
    public ConfigElement(
            Consumer<T> applier,
            BiPredicate<T, T> equals,
            Function<T, String> serializer,
            Function<String, T> deSerializer) {
        this(
                applier,
                T -> T,
                equals,
                serializer, 
                deSerializer
        );
    }

    public Boolean applyIfChanged(T newValue) {
        T normalizedValue = normalizer.apply(newValue);
        if (this.value != null && equals.test(this.value, normalizedValue)) {
            return false;
        }

        apply(normalizedValue);
        return true;
    }
    public void apply(T newValue) {
        T normalizedValue = normalizer.apply(newValue);
        this.applier.accept(normalizedValue);
        this.value = normalizedValue;
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
