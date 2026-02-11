package com.github.casiowatch123.aladinobserver.model.config.dynamic;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class DynamicConfigRegistry {
    private final Map<String, DynamicConfigElement<?>> configElementMap = new HashMap<>();
    
    private final Map<String, String> configDataMap = new HashMap<>();
    
    private final String storageId = "config_elements";
    private final DataStorage dataStorage;
    
    public DynamicConfigRegistry(DataStorageFactory dataStorageFactory) {
        this.dataStorage = dataStorageFactory.getStorage(storageId);
        
        dataStorage.readIfValid(
                reader -> reader.lines().forEach(line -> {
                    int idx = line.indexOf(';');
                    if (idx <= 0 || idx == line.length() - 1) {
                        return;
                    }

                    String configId = line.substring(0, idx);
                    String configData = line.substring(idx + 1);
                    configDataMap.put(configId, configData);
                }));
    }
    
    public synchronized <T> void register(
            String configId, 
            DynamicConfigElement<T> element, 
            T defaultValue) {
        if (configDataMap.containsKey(configId)) {
            element.init(configDataMap.get(configId));
            configDataMap.put(configId, element.getSerializedValue());
        } else {
            element.apply(defaultValue);
            configDataMap.put(configId, element.getSerializedValue());
        }
        configElementMap.put(configId, element);
        saveConfigDataMap();
    }

    public void applyIfDif(String configId, Object value) {
        boolean changed;
        synchronized (this) {
            DynamicConfigElement<?> element = configElementMap.get(configId);
            
            if (element == null)  {
                return;
            }
            
            changed = element.applyIfChanged(value);
            if (changed) {
                configDataMap.put(configId, element.getSerializedValue());
            }
        }
        if (changed) {
            saveConfigDataMap();
        }
    }
    public void apply(String configId, Object value) {
        synchronized (this) {
            DynamicConfigElement<?> element = configElementMap.get(configId);

            if (element == null)  {
                return;
            }

            element.apply(value);
            configDataMap.put(configId, element.getSerializedValue());
        }
        saveConfigDataMap();
    }
    
    public Object value(String configId) throws ConfigElementNotFoundException {
        DynamicConfigElement<?> element = configElementMap.get(configId);
        
        if (element == null) {
            throw new ConfigElementNotFoundException(configId);
        }
        
        return element.value();
    }
    
    private void saveConfigDataMap() {
        Map<String, String> configDataMapSnapshot;
        
        synchronized (this) {
            configDataMapSnapshot = new HashMap<>(configDataMap);
        }
        
        dataStorage.write(writer -> {
            configDataMapSnapshot.forEach((configId, configData) -> {
                try {
                    writer.write(String.format("%s;%s", configId, configData));
                    writer.newLine();
                } catch (IOException e) {
                    Logger.getInstance().writeLog(e);
                }
            });
        });
    }
}