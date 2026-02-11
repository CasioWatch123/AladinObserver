package com.github.casiowatch123.aladinobserver.model.config;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.OffShopProductTrayService;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;
import com.github.casiowatch123.aladinobserver.model.storage.TextDataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.TextDataStorageFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigRegistry {
    private static final String STORAGE_ID = "config_elements";
    private final DataStorage dataStorage;

    private final Map<String, String> configDataMap = new HashMap<>();

    public static final String RUN_PERIOD_CONFIG_ID = "Run_Period";
    public static final long MIN_RUN_PERIOD = 30;
    public static final long MAX_RUN_PERIOD = 120;
    public static final String EXECUTION_FLAG_CONFIG_ID = "Execution_Flag";
    public static final String NOTIFICATION_FLAG_CONFIG_ID = "Notification_Flag";

    private static final long DEFAULT_RUN_PERIOD = 30L;
    private static final boolean DEFAULT_EXECUTION_FLAG = false;
    private static final boolean DEFAULT_NOTIFICATION_FLAG = true;
    
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    
    private final ConfigElement<Long> runPeriodConfigElement;
    private final ConfigElement<Boolean> executionFlagConfigElement;
    private final ConfigElement<Boolean> notificationFlagConfigElement;
    
    public ConfigRegistry(OffShopProductTrayService productTrayService, StateRepo stateRepo, DataStorageFactory dataStorageFactory) {
        this.dataStorage = dataStorageFactory.getStorage(STORAGE_ID);

        dataStorage.readIfValid(
                reader -> reader.lines().forEach(line -> {
                    int idx = line.indexOf(';');
                    if (idx <= 0 || idx == line.length() - 1) {
                        return;
                    }

                    String configId = line.substring(0, idx);
                    String configData = line.substring(idx + 1);
                    configDataMap.put(configId, configData);
                })
        );

        this.runPeriodConfigElement = new ConfigElement<Long>(
                ld -> productTrayService.setPeriod(ld, TIME_UNIT),
                ld -> {
                    if (ld > MAX_RUN_PERIOD) {
                        return MAX_RUN_PERIOD;
                    } else if (ld < MIN_RUN_PERIOD) {
                        return MIN_RUN_PERIOD;
                    } else {
                        return ld;
                    }
                }, 
                Long::equals,
                ld -> Long.toString(ld),
                Long::parseLong
        );
        this.executionFlagConfigElement = new BooleanConfigElement(flag -> {
            if (flag) {
                productTrayService.run();
            } else {
                productTrayService.stop();
            }
        });
        this.notificationFlagConfigElement = new BooleanConfigElement(flag -> {
            if (flag) {
                stateRepo.enableNotification();
            } else {
                stateRepo.disableNotification();
            }
        });
        
        initConfigElements();
    }
    
    public void setRunPeriod(long period) {
        boolean changed;
        synchronized (this) {
            changed = runPeriodConfigElement.applyIfChanged(period);
            if (changed) {
                configDataMap.put(RUN_PERIOD_CONFIG_ID, runPeriodConfigElement.getSerializedValue());
            }
        }
        if (changed) {
            saveConfigDataMap();
        }
    }
    
    public void setExecutionFlag(boolean flag) {
        boolean changed;
        synchronized (this) {
            changed = executionFlagConfigElement.applyIfChanged(flag);
            if (changed) {
                configDataMap.put(EXECUTION_FLAG_CONFIG_ID, executionFlagConfigElement.getSerializedValue());
            }
        }
        if (changed) {
            saveConfigDataMap();
        }
    }
    
    public void setNotificationFlag(boolean flag) {
        boolean changed;
        synchronized (this) {
            changed = notificationFlagConfigElement.applyIfChanged(flag);
            if (changed) {
                configDataMap.put(NOTIFICATION_FLAG_CONFIG_ID, notificationFlagConfigElement.getSerializedValue());
            }
        }
        if (changed) {
            saveConfigDataMap();
        }
    }

    public long getRunPeriod() {
        return runPeriodConfigElement.value();
    }
    
    public boolean getExecutionFlag() {
        return executionFlagConfigElement.value();
    }
    
    public boolean getNotificationFlag() {
        return notificationFlagConfigElement.value();
    }
    
    private void initConfigElements() {
        String runPeriodData = configDataMap.get(RUN_PERIOD_CONFIG_ID);
        if (runPeriodData != null) {
            runPeriodConfigElement.init(runPeriodData);
        } else {
            runPeriodConfigElement.apply(DEFAULT_RUN_PERIOD);
        }
        configDataMap.put(RUN_PERIOD_CONFIG_ID, runPeriodConfigElement.getSerializedValue());

        String executionFlagData = configDataMap.get(EXECUTION_FLAG_CONFIG_ID);
        if (executionFlagData != null) {
            executionFlagConfigElement.init(executionFlagData);
        } else {
            executionFlagConfigElement.apply(DEFAULT_EXECUTION_FLAG);
        }
        configDataMap.put(EXECUTION_FLAG_CONFIG_ID, executionFlagConfigElement.getSerializedValue());

        String notificationFlagData = configDataMap.get(NOTIFICATION_FLAG_CONFIG_ID);
        if (notificationFlagData != null) {
            notificationFlagConfigElement.init(notificationFlagData);
        } else {
            notificationFlagConfigElement.apply(DEFAULT_NOTIFICATION_FLAG);
        }
        configDataMap.put(NOTIFICATION_FLAG_CONFIG_ID, notificationFlagConfigElement.getSerializedValue());


        saveConfigDataMap();
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
