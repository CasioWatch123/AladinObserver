package com.github.CasioWatch123.AladinObserver.model.offshop;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.AladinProductData;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.tray.ProductTray;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OffShopProductTray {
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ExecutorService executor;
    private volatile Thread executorThread;
    private final ProductTray tray;

    private final ObservableTray observableTray;
    
    private ScheduledFuture<?> nowTaskHandle;
    
    private volatile boolean shutdownFlag = false;
    
    
    public OffShopProductTray(ProductTray tray) {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ProductTray-Executor");
            executorThread = t;
            return t;
        });
        this.tray = tray;
        observableTray = new ObservableTray(tray.getKeySet());
    }
    
    
    public void addProduct(String itemId) {
        CompletableFuture.supplyAsync(() -> tray.addProduct(itemId), executor)
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
                if (!shutdownFlag) {
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
        Set<String> updateResult = tray.updateAll();
        
        try {
            executor.execute(() -> {
                publish(updateResult);
            });
        } catch (RejectedExecutionException ignored) {}
    }
    
    private void publish(Set<String> itemIdSet) {
        if(shutdownFlag) {
            return;
        }

        Set<String> filtered = itemIdSet.stream()
                .filter(id -> tray.getProductData(id) != null)
                .collect(Collectors.toSet());
        observableTray.set(filtered);
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
        try {
            executor.execute(() -> {
                this.shutdownFlag = true;

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
