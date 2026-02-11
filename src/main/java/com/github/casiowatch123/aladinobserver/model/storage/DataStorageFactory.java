package com.github.casiowatch123.aladinobserver.model.storage;

public interface DataStorageFactory {
    DataStorage getStorage(String id);
}
