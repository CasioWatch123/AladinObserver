package com.github.casiowatch123.aladinobserver.model.offshop.impl.products.exceptions;

import java.io.IOException;

public class WrappedIOException extends RuntimeException {
    public WrappedIOException(IOException e) {
        super(e);
    }
}
