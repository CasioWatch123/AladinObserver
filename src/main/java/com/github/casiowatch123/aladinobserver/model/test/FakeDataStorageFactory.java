package com.github.casiowatch123.aladinobserver.model.test;

import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;

public class FakeDataStorageFactory implements DataStorageFactory {

    @Override
    public DataStorage getStorage(String id) {
        return new FakeDataStorage();
    }
}
