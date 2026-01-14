package com.github.CasioWatch123.AladinObserver.model.offshop.impl.products;

import com.github.CasioWatch123.AladinObserver.model.offshop.impl.products.history.CheckResult;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface AladinProduct extends AladinProductData{
    CompletableFuture<CheckResult> updateAsync(Executor executor);
}
