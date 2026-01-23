package com.github.casiowatch123.aladinobserver;

public class WrappedInterruptedException extends RuntimeException {
    public WrappedInterruptedException(InterruptedException e) { super(e); }
}
