package com.bricklink.api.ajax;

import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;

import java.util.Map;
import java.util.Optional;

public interface BricklinkAjaxClient {
    SearchProductResult searchProduct(Map<String, Object> params);

    CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params);

    Optional<Item> findCatalogItem(String itemNumber, String itemType);

    CatalogItemsForSaleResult catalogItemsForSaleByInternalItemId(Integer itemId, String condition, Integer resultsPerPage);
}
