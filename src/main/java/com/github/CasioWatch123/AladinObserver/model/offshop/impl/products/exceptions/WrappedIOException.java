package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions;

import java.io.IOException;

public class WrappedIOException extends RuntimeException {
    public WrappedIOException(IOException e) {
        super(e);
    }
}
