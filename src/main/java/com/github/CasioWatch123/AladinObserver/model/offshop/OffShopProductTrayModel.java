package com.github.CasioWatch123.AladinObserver.model.offshop;

import com.github.CasioWatch123.AladinObserver.log.Logger;
import com.github.CasioWatch123.AladinObserver.model.ModelPolicies;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.AladinProductData;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.OffshopCheckResult;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.tray.ProductTray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class OffShopProductTrayModel {
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ExecutorService executor;
    private final ProductTray tray;

    private static final Path DIR = ModelPolicies.LOCAL_REPO_DIR;
    private static final Path FILE_PATH = Path.of(DIR.toString(), "ItemIdSet.txt");
    
    private final ObservableTray observableTray;
    private final List<Consumer<Set<String>>> changedProductSubscribers;
    private ScheduledFuture<?> nowTaskHandle;
    
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    
    static {
        if(!Files.exists(FILE_PATH)) {
            try {
                Files.createFile(FILE_PATH);
            } catch (IOException e) {
                System.err.println( e);
            }
        }
    }
    
    public OffShopProductTrayModel(ProductTray tray) {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.executor = Executors.newSingleThreadExecutor();
        
        this.tray = tray;
        this.observableTray = new ObservableTray(tray.getKeySet());
        this.changedProductSubscribers = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)){
            reader.lines().forEach(tray::addProduct);
        } catch (IOException e) {
            Logger.getInstance().writeLog(e);
        }
    }
    
    
    public CompletableFuture<Boolean> addProduct(String itemId) {
        return CompletableFuture.supplyAsync(() -> tray.addProduct(itemId), executor)
                .exceptionally(e -> {
                    removeProduct(itemId);
                    return false;
                });
    }
    
    public void removeProduct(String itemId) {
        try {
            executor.execute(() -> {
                tray.removeProduct(itemId);
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    public void run(long delay, TimeUnit unit) {
        try {
            executor.execute(() -> {
                if (!shutdownFlag.get()) {
                    try {
                        ScheduledFuture<?> newTask =
                                scheduledThreadPoolExecutor.scheduleWithFixedDelay(this::updateTick, delay, delay, unit);

                        //replace now task handle if old task handle exist
                        if (nowTaskHandle != null) {
                            nowTaskHandle.cancel(false);
                        }

                        nowTaskHandle = newTask;
                    } catch (RejectedExecutionException ignored) {
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    
    private void updateTick() {
        try {
            executor.execute(() -> {
                Set<String> updateKeySet = tray.updateAll();
                publish(updateKeySet);
                
                //notify changed product
                Set<String> changedItemIdSet = new HashSet<>();
                
                updateKeySet.forEach(itemId -> {
                    AladinProductData productData = tray.getProductData(itemId);
                    if (productData.getPreviousCheckResult() != null) {
                        OffshopCheckResult prevCheckResult = productData.getPreviousCheckResult();
                        OffshopCheckResult latestCheckResult = productData.getHistoryFirst();
                        
                        if (!prevCheckResult.isExceptional() && !latestCheckResult.isExceptional()) {
                            Set<String> prevCheckResultSet = new HashSet<>(prevCheckResult.getOffshopList());
                            Set<String> latestCheckResultSet = new HashSet<>(latestCheckResult.getOffshopList());
                            
                            if (!prevCheckResultSet.equals(latestCheckResultSet)) {
                                changedItemIdSet.add(itemId);
                            }
                        } else {
                            System.out.println("dd");
                        }
                    }
                });
                
                if (!changedItemIdSet.isEmpty()) {
                    this.changedProductSubscribers.forEach(consumer -> {
                        consumer.accept(changedItemIdSet);
                    });
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    private void publish(Set<String> itemIdSet) {
        if(shutdownFlag.get()) {
            return;
        }

        observableTray.set(itemIdSet);
    }
    
    
    public Runnable subscribeProductStockChanges(Consumer<Set<String>> consumer) {
        this.changedProductSubscribers.add(consumer);
        return () -> this.changedProductSubscribers.remove(consumer);
    }
    
    public Runnable subscribeTray(Consumer<Set<String>> consumer) {
        return observableTray.subscribe(consumer);
    }
    
    
    public CompletableFuture<AladinProductData> getProductData(String itemId) {
        return CompletableFuture.supplyAsync(() -> tray.getProductData(itemId), executor);
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
                
                try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardOpenOption.WRITE)) {
                    tray.getKeySet().forEach(itemId -> {
                        try {
                            writer.write(itemId);
                            writer.newLine();
                        } catch (IOException e) {
                            Logger.getInstance().writeLog(e);
                        }
                    });
                } catch (IOException e) {
                    Logger.getInstance().writeLog(e);
                }
            });
        } catch (RejectedExecutionException ignored) {}
        
        scheduledThreadPoolExecutor.shutdown();
        executor.shutdown();
        tray.shutdown();
    }
    
    
    private static class ObservableTray {
        private final List<Consumer<Set<String>>> subscribers = new CopyOnWriteArrayList<>();
        private volatile Set<String> value;
        
        public ObservableTray(Set<String> value) {
            this.value = value;
        }
        
        public Runnable subscribe(Consumer<Set<String>> consumer) {
            subscribers.add(consumer);
            consumer.accept(value);
            
            return () -> subscribers.remove(consumer);
        }
        
        public void set(Set<String> newValue) {
            this.value = newValue;
            subscribers.forEach(subscriber -> {
                subscriber.accept(newValue);
            });
        }
    }
}
