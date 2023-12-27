package com.bricklink.api.ajax;

import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class PagingBricklinkAjaxClient implements BricklinkAjaxClient {
    private final BricklinkAjaxClient bricklinkAjaxClient;

    @Override
    public SearchProductResult searchProduct(Map<String, Object> params) {
        return bricklinkAjaxClient.searchProduct(params);
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
        CatalogItemsForSaleResult completedCatalogItemsForSaleResult = new CatalogItemsForSaleResult();
        completedCatalogItemsForSaleResult.setList(new ArrayList<>());

        while (true) {
            pause();
            CatalogItemsForSaleResult catalogItemsForSaleResult = bricklinkAjaxClient.catalogItemsForSale(params);
            Integer thisPageNumber = catalogItemsForSaleResult.getPi();
            Integer totalCount = catalogItemsForSaleResult.getTotal_count();
            Integer resultsPerPage = catalogItemsForSaleResult.getRpp();
            Integer thisPageResultSize = catalogItemsForSaleResult.getList().size();
            completedCatalogItemsForSaleResult.getList().addAll(catalogItemsForSaleResult.getList());
            if (thisPageNumber*resultsPerPage >= totalCount) {
                break;
            }
            params.put("pi", ++thisPageNumber);
            completedCatalogItemsForSaleResult.setTotal_count(completedCatalogItemsForSaleResult.getList().size());
        }
        return completedCatalogItemsForSaleResult;
    }

    void pause() {
        try {
            Thread.sleep(1300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
