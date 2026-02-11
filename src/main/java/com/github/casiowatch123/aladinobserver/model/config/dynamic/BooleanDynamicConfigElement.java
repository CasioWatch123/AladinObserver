package com.github.casiowatch123.aladinobserver.model.config.dynamic;

import java.util.function.Consumer;

public class BooleanDynamicConfigElement extends DynamicConfigElement<Boolean> {
    public BooleanDynamicConfigElement(Consumer<Boolean> applier) {
        super(
                Boolean.class, 
                applier, 
                Boolean::equals, 
                bool -> Boolean.toString(bool), 
                Boolean::valueOf
        );
    }
}
