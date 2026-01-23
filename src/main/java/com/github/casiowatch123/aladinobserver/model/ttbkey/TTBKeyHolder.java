package com.github.casiowatch123.aladinobserver.model.ttbkey;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.ModelPolicies;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TTBKeyHolder implements TTBKeyService{
    private final DataStorage dataStorage;
    private final String storageId = "ttbkey";
    private String TTBKey;
    
    public TTBKeyHolder(DataStorageFactory dataStorageFactory) {
        this.dataStorage = dataStorageFactory.getStorage(storageId);
        
        dataStorage.readOrDefault(
                reader -> {
                    try {
                        this.TTBKey = reader.readLine();
                    } catch (IOException e) {
                        this.TTBKey = "";
                    }
                },
                () -> this.TTBKey = "");
    }
    
    public synchronized void setTTBKey(String newTTBKey) {
        this.TTBKey = newTTBKey;
        dataStorage.write(writer -> {
            try {
                writer.write(newTTBKey);
            } catch (IOException e) {
                Logger.getInstance().writeLog(e);
            }
        });
    }
    
    @Override
    public String getTTBKey() {
        return TTBKey;
    }
}
