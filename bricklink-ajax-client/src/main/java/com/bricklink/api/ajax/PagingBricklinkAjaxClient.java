package com.bricklink.api.ajax;

import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class PagingBricklinkAjaxClient implements BricklinkAjaxClient {
    private final BricklinkAjaxClient delegate;

    @Override
    public SearchProductResult searchProduct(Map<String, Object> params) {
        return delegate.searchProduct(params);
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
        return delegate.catalogItemsForSale(params);
    }

    @Override
    public Optional<Item> findCatalogItem(String itemNumber, String itemType) {
        return delegate.findCatalogItem(itemNumber, itemType);
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSaleByInternalItemId(Integer itemId, String condition, Integer resultsPerPage) {
        return delegate.catalogItemsForSaleByInternalItemId(itemId, condition, resultsPerPage);
    }
}
