package com.github.casiowatch123.aladinobserver.model.storage;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.ModelPolicies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class TextDataStorageFactory implements DataStorageFactory {
    public static final Path ROOT_DIR = ModelPolicies.LOCAL_ROOT_DIR.resolve("storage");
    
    private final ConcurrentHashMap<String, TextDataStorage> cache = new ConcurrentHashMap<>();
    
    static {
        try {
            Files.createDirectories(ROOT_DIR);
        } catch (IOException e) {
            System.err.println(e);
            Logger.getInstance().writeLog(e);
        }
    }
    public TextDataStorageFactory() {}
    
    @Override
    public TextDataStorage getStorage(String id) {
        return cache.computeIfAbsent(id, this::createStorage);
    }
    
    private TextDataStorage createStorage(String id) {
        return new TextDataStorage(ROOT_DIR, id);
    }
}
