package com.github.casiowatch123.aladinobserver.model.storage;

import com.github.casiowatch123.aladinobserver.log.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DataStorage {
    private final Path filePath;
    
    public DataStorage(Path rootDir, String id) {
        this.filePath = rootDir.resolve(id + ".txt");
        try {
            Files.createFile(filePath);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            Logger.getInstance().writeLog(e);
        }
    }
    
    public synchronized void read(Consumer<BufferedReader> action) throws IOException {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException();
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            action.accept(reader);
        } catch (IOException e) {
            Logger.getInstance().writeLog(e);
            throw e;
        }
    }
    public synchronized void readOrDefault(Consumer<BufferedReader> action, Runnable defaultAction) {
        try {
            read(action);
        } catch (IOException e) {
            defaultAction.run();
        }
    }
    public synchronized void readIfValid(Consumer<BufferedReader> action) {
        readOrDefault(action, () -> {});
    }
    
    public synchronized void write(Consumer<BufferedWriter> action) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            action.accept(writer);
        } catch (IOException e) {
            Logger.getInstance().writeLog(e);
        }
    }
}
