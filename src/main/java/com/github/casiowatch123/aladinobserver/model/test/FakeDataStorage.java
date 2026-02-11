package com.github.casiowatch123.aladinobserver.model.test;

import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.function.Consumer;

public class FakeDataStorage implements DataStorage {
    
    public FakeDataStorage() {
    }
    @Override
    public void read(Consumer<BufferedReader> action) throws IOException {
    }

    @Override
    public void readOrDefault(Consumer<BufferedReader> action, Runnable defaultAction) {
        try {
            read(action);
        } catch (IOException e) {
            defaultAction.run();
        }
    }

    @Override
    public void readIfValid(Consumer<BufferedReader> action) {
        readOrDefault(action, () -> {});
    }

    @Override
    public void write(Consumer<BufferedWriter> action) {
    }
}
