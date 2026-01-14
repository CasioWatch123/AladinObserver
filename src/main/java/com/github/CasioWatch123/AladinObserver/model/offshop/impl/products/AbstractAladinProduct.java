package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products;

import com.github.CasioWatch123.AladinObserver.log.Logger;
import com.github.CasioWatch123.AladinObserver.model.TTBKeyHolder;
import com.github.CasioWatch123.AladinObserver.model.TTBKeyService;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions.AladinAPIException;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.exceptions.ProductUpdateException;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.CheckResult;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.HistoryObjectDeque;
import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.HistoryPolicies;
import com.github.CasioWatch123.AladinObserver.model.ModelPolicies;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractAladinProduct implements AladinProduct{
    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    
    protected static final Gson GSON_PARSER = new Gson();

    protected static final int TIMEOUT_SEC = ModelPolicies.TIMEOUT_SEC;
    
    protected final URI imageURI;
    protected final String itemId;
    protected final String itemName;
    
    protected Image itemImage;
    
    protected final HistoryDeque historyDeque;
    
    protected static final TTBKeyService TTB_KEY_SERVICE = TTBKeyHolder.getInstance();
    
    protected AbstractAladinProduct(HistoryObjectDeque<CheckResult> historyObjectDeque, 
                                    URI imageURI, 
                                    String itemId, 
                                    String itemName, 
                                    Image defaultImage) {        
        this.imageURI = imageURI;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImage = defaultImage;
        this.historyDeque = generateHistoryDeque(historyObjectDeque);
    }
    
    protected HistoryDeque generateHistoryDeque(HistoryObjectDeque<CheckResult> historyObjectList) {
        HistoryDeque historyDeque = new HistoryDeque();
        //Initialize history deque based on execution history            
        
        historyObjectList
                .getDeque()
                .forEach(historyObject -> {
                    if (historyObject.isValidKey(itemId)) {
                        historyDeque.addHistoryLast(historyObject.get(itemId));
                    }
                });
        
        return historyDeque;
    }
    
    @Override
    public CompletableFuture<CheckResult> updateAsync(Executor asyncExecutor) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getUpdateURI())
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .GET()
                    .build();

            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .orTimeout(5, TimeUnit.SECONDS)
                    .thenApplyAsync(response -> {
                        try {
                            JsonObject responseJson = GSON_PARSER.fromJson(response.body(), JsonObject.class);
                            //error response from aladin api.
                            if (responseJson.get("errorCode") != null) {
                                throw new AladinAPIException(itemId, responseJson.get("errorMessage").getAsString());
                            }

                            //generate valid off shop list
                            List<String> offShopList = responseJson.getAsJsonArray("itemOffStoreList").asList()
                                    .stream()
                                    .map(element -> element
                                            .getAsJsonObject()
                                            .get("offName")
                                            .getAsString())
                                    .toList();

                            CheckResult result;
                            if (offShopList.isEmpty()) {
                                result = CheckResult.getEmptyCheckResult(itemId);
                            } else {
                                result = new CheckResult(itemId, offShopList, LocalDateTime.now().withNano(0));
                                historyDeque.addHistoryFirst(result);
                            }
                            return result;
                        } catch (AladinAPIException | JsonSyntaxException e) {
                            throw new ProductUpdateException(e);
                        }
                    }, asyncExecutor)
                    .exceptionally(e -> {
                        Logger.getInstance().writeLog(e);
                        return CheckResult.getExceptionalCheckResult(itemId);
                    });
        } catch (RejectedExecutionException e) {
            return CompletableFuture.completedFuture(CheckResult.getExceptionalCheckResult(itemId));
        } catch (URISyntaxException e) {
            Logger.getInstance().writeLog(e);
            return CompletableFuture.completedFuture(CheckResult.getExceptionalCheckResult(itemId));
        }
    }
    
    @Override
    public String itemId() {
        return this.itemId;
    }
    
    @Override
    public String itemName() {
        return this.itemName;
    }
    
    @Override
    public Deque<CheckResult> getHistories() {
        return this.historyDeque.getDeque();
    }

    @Override
    public CheckResult getHistoryFirst() {
        return historyDeque.getDeque().getFirst();
    }
    
    @Override
    public Image itemImage() {
        return this.itemImage;
    }

    protected abstract URI getUpdateURI() throws URISyntaxException;

    protected static class HistoryDeque {
        private final AtomicReference<Deque<CheckResult>> historyRef;
    
        public HistoryDeque() {
            historyRef = new AtomicReference<>(new ArrayDeque<>());
        }
    
        public void addHistoryLast(CheckResult checkResult) {
            while (true) {
                Deque<CheckResult> oldDeque = historyRef.get();
                Deque<CheckResult> newDeque = new ArrayDeque<>(oldDeque);
                if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                    return;
                }
    
                newDeque.addLast(checkResult);
                
                if (historyRef.compareAndSet(oldDeque, newDeque)) {
                    return;
                }
            }
        }
    
        public void addHistoryFirst(CheckResult checkResult) {
            while (true) {
                Deque<CheckResult> oldDeque = historyRef.get();
                Deque<CheckResult> newDeque = new ArrayDeque<>(oldDeque);
                if (newDeque.size() >= HistoryPolicies.MAX_HISTORY_DEQUE_SIZE) {
                    newDeque.removeLast();
                }
    
                newDeque.addFirst(checkResult);
                
                if (historyRef.compareAndSet(oldDeque, newDeque)) {
                    return;
                }
            }
        }
    
        public Deque<CheckResult> getDeque() {
            return new ArrayDeque<>(historyRef.get());
        }
    }
}
