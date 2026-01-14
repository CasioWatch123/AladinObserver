package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions;

public class ProductUpdateException extends RuntimeException {
    public ProductUpdateException(String message) {
        super(message);
    }
    
    public ProductUpdateException(Exception e) {
        super(e);
    }
}
