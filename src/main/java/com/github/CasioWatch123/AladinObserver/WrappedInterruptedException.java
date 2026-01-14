package com.github.CasioWatch123.AladinObserver;

public class WrappedInterruptedException extends RuntimeException {
    public WrappedInterruptedException(InterruptedException e) { super(e); }
}
