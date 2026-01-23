package com.github.casiowatch123.aladinobserver.model.offshop.impl.products;

import com.github.casiowatch123.aladinobserver.model.offshop.impl.products.history.OffshopCheckResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface AladinProduct extends AladinProductData{
    CompletableFuture<OffshopCheckResult> updateAsync(Executor executor);
}
