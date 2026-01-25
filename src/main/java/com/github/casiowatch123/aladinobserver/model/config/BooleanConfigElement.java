package com.github.casiowatch123.aladinobserver.model.config;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class BooleanConfigElement extends ConfigElement<Boolean> {
    public BooleanConfigElement(Consumer<Boolean> applier) {
        super(
                Boolean.class, 
                applier, 
                Boolean::equals, 
                bool -> Boolean.toString(bool), 
                Boolean::valueOf
        );
    }
}
