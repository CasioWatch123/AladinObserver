package com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions;

public class ProductInitializeException extends Exception {
    public ProductInitializeException(String message) {
        super(message);
    }
    
    public ProductInitializeException(String message, Exception e) {
        super(message + "; " + e.toString());
    }
}
