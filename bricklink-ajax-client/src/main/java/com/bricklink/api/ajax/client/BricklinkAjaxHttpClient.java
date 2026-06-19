package com.bricklink.api.ajax.client;

import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface BricklinkAjaxHttpClient {
    @GetExchange("/ajax/clone/search/searchproduct.ajax")
    SearchProductResult searchProduct(@RequestParam Map<String, Object> params);

    @GetExchange("/ajax/clone/catalogifs.ajax")
    CatalogItemsForSaleResult catalogItemsForSale(@RequestParam Map<String, Object> params);
}
