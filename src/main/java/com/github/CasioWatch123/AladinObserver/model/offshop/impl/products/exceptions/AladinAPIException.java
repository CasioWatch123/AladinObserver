package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions;

public class AladinAPIException extends Exception {
    public AladinAPIException(String message) {
        super(message);
    }

    public AladinAPIException(String itemId, String errorMessage) {
        super(String.format("itemId:%s, Message:%s\n", itemId, errorMessage));
    }
}
