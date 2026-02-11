package com.github.casiowatch123.aladinobserver.model.config;

import java.util.function.Consumer;

public class BooleanConfigElement extends ConfigElement<Boolean> {
    public BooleanConfigElement(Consumer<Boolean> applier) {
        super(
                applier,
                Boolean::equals,
                bool -> Boolean.toString(bool),
                Boolean::valueOf
        );
    }
}
