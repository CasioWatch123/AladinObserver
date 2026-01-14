package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions;

public class ProductInitializeException extends Exception {
    public ProductInitializeException(String message) {
        super(message);
    }
    
    public ProductInitializeException(String message, Exception e) {
        super(message + "; " + e.toString());
    }
}
