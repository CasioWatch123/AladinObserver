package com.github.casiowatch123.aladinobserver.model.offshop;

import com.github.casiowatch123.aladinobserver.log.Logger;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.AladinProductData;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.casiowatch123.aladinobserver.model.offshop.impl.tray.ProductTray;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorage;
import com.github.casiowatch123.aladinobserver.model.storage.DataStorageFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class OffShopProductTrayService {
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();;
    private final ProductTray tray;

    private final DataStorage dataStorage;
    private final String storageId = "tray_itemid_set";
    
    private final ObservableTray observableTray;
    private final List<Consumer<Set<AladinProductData>>> changedProductSubscribers = new ArrayList<>();
    
    private TimeUnit timeUnit;
    private long period;
    
    private ScheduledFuture<?> nowTaskHandle;
    
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);


    public OffShopProductTrayService(ProductTray tray, DataStorageFactory dataStorageFactory) {
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        
        this.tray = tray;

        this.dataStorage = dataStorageFactory.getStorage(this.storageId);
        dataStorage.readIfValid(reader -> reader.lines().forEach(tray::addProduct));
        
        this.observableTray = new ObservableTray(tray.getTrayData());
    }
    
    
    public void addProduct(String itemId) {
        try {
            executor.execute(() -> {
                Set<String> oldKeySet = new HashSet<>(tray.getKeySet());
                
                tray.addProduct(itemId);
                
                if (!oldKeySet.equals(tray.getKeySet())) {
                    dataStorage.write(writer -> {
                        tray.getKeySet().forEach(trayItemId -> {
                            try {
                                writer.write(trayItemId);
                                writer.newLine();
                            } catch (IOException e) {
                                Logger.getInstance().writeLog(e);
                            }
                        });
                    });
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    public void removeProduct(String itemId) {
        try {
            executor.execute(() -> {
                Set<String> oldKeySet = new HashSet<>(tray.getKeySet());
                
                tray.removeProduct(itemId);

                if (!oldKeySet.equals(tray.getKeySet())) {
                    dataStorage.write(writer -> {
                        tray.getKeySet().forEach(trayItemId -> {
                            try {
                                writer.write(trayItemId);
                                writer.newLine();
                            } catch (IOException e) {
                                Logger.getInstance().writeLog(e);
                            }
                        });
                    });
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    public void setPeriod(Long period, TimeUnit unit) {
        try {
            executor.execute(() -> {
                this.period = period;
                this.timeUnit = unit;
                
                if (nowTaskHandle != null) {
                    nowTaskHandle.cancel(false);
                    run();
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    public void run() {
        try {
            executor.execute(() -> {
                if (shutdownFlag.get()) {
                    return;
                }
                if (period <= 0 || timeUnit == null){
                    Logger.getInstance().writeLog("invalid period(timeunit)");
                    return;
                }
                try {
                    ScheduledFuture<?> newTask =
                            scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                                    this::updateTick,
                                    this.period,
                                    this.period,
                                    this.timeUnit);

                    //replace now task handle if old task handle exist
                    if (nowTaskHandle != null) {
                        nowTaskHandle.cancel(false);
                    }

                    nowTaskHandle = newTask;
                } catch (RejectedExecutionException ignored) {
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    private void updateTick() {
        try {
            tray.updateAllAsync().whenCompleteAsync((V, T) -> {
                publishTray(tray.getTrayData());

                //notify changed product
                Set<AladinProductData> changedProductDataSet = new HashSet<>();

                tray.getTrayData().forEach(productData -> {
                    OffshopCheckResult prev = productData.getPreviousCheckResult();
                    OffshopCheckResult latest = productData.getHistoryFirst();

                    if (prev != null && !prev.isExceptional() && !latest.isExceptional()) {
                        Set<String> prevSet = new HashSet<>(prev.getOffshopList());
                        Set<String> latestSet = new HashSet<>(latest.getOffshopList());

                        if (!prevSet.equals(latestSet)) {
                            changedProductDataSet.add(productData);
                        }
                    }
                });

                if (!changedProductDataSet.isEmpty()) {
                    changedProductSubscribers.forEach(consumer -> consumer.accept(Set.copyOf(changedProductDataSet)));
                }
            }, executor);
        } catch (RejectedExecutionException ignored) {}
    }
    
    private void publishTray(Set<AladinProductData> aladinProductDataSet) {
        try {
            executor.execute(() -> {
                if(shutdownFlag.get()) {
                    return;
                }

                observableTray.set(aladinProductDataSet);
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    
    public Runnable subscribeProductStockChanges(Consumer<Set<AladinProductData>> consumer) {
        this.changedProductSubscribers.add(consumer);
        return () -> this.changedProductSubscribers.remove(consumer);
    }
    
    public Runnable subscribeTray(Consumer<Set<AladinProductData>> consumer) {
        return observableTray.subscribe(consumer);
    }
    
    
    public CompletableFuture<Set<AladinProductData>> getTrayData() {
        return CompletableFuture.supplyAsync(tray::getTrayData, executor);
    }
    
    
    public void stop() {
        try {
            executor.execute(() -> {
                if (nowTaskHandle != null) {
                    nowTaskHandle.cancel(false);
                }

                nowTaskHandle = null;
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    public void shutdown() {
        //stop and wait
        if (!shutdownFlag.compareAndSet(false, true)) {
            return;
        }
        
        try {
            executor.execute(() -> {
                if (nowTaskHandle != null) {
                    nowTaskHandle.cancel(false);
                }

                nowTaskHandle = null;
            });
        } catch (RejectedExecutionException ignored) {}
        
        scheduledThreadPoolExecutor.shutdown();
        executor.shutdown();
        tray.shutdown();
    }
    
    
    private static class ObservableTray {
        private final List<Consumer<Set<AladinProductData>>> subscribers = new CopyOnWriteArrayList<>();
        private volatile Set<AladinProductData> value;
        
        public ObservableTray(Set<AladinProductData> value) {
            this.value = value;
        }
        
        public Runnable subscribe(Consumer<Set<AladinProductData>> consumer) {
            subscribers.add(consumer);
            consumer.accept(value);
            
            return () -> subscribers.remove(consumer);
        }
        
        public void set(Set<AladinProductData> newValue) {
            this.value = newValue;
            subscribers.forEach(subscriber -> {
                subscriber.accept(newValue);
            });
        }
    }
}
