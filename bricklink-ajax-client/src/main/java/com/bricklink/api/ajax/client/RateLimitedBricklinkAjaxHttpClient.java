package com.bricklink.api.ajax.client;

import com.bricklink.api.ajax.ratelimit.BricklinkAjaxRateLimiter;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class RateLimitedBricklinkAjaxHttpClient implements BricklinkAjaxHttpClient {
    private final BricklinkAjaxHttpClient delegate;
    private final BricklinkAjaxRateLimiter rateLimiter;

    @Override
    public SearchProductResult searchProduct(Map<String, Object> params) {
        rateLimiter.acquire("searchproduct.ajax");
        return delegate.searchProduct(params);
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
        rateLimiter.acquire("catalogifs.ajax");
        return delegate.catalogItemsForSale(params);
    }
}
