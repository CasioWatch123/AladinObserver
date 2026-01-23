package com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions;

public class ProductUpdateException extends RuntimeException {
    public ProductUpdateException(String message) {
        super(message);
    }
    
    public ProductUpdateException(Exception e) {
        super(e);
    }
}
