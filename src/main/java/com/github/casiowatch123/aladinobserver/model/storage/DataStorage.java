package com.github.casiowatch123.aladinobserver.model.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.function.Consumer;

public interface DataStorage {
    void read(Consumer<BufferedReader> action) throws IOException;
    
    void readOrDefault(Consumer<BufferedReader> action, Runnable defaultAction);
    
    void readIfValid(Consumer<BufferedReader> action);
    
    void write(Consumer<BufferedWriter> action);
}
