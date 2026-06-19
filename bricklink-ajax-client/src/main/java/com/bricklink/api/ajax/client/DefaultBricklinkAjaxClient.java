package com.bricklink.api.ajax.client;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.exception.BricklinkAjaxClientException;
import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.model.v1.Type;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultBricklinkAjaxClient implements BricklinkAjaxClient {
    private static final int DEFAULT_RESULTS_PER_PAGE = 500;
    private static final String SET_TYPE = "S";

    private final BricklinkAjaxHttpClient httpClient;

    @Override
    public SearchProductResult searchProduct(Map<String, Object> params) {
        return httpClient.searchProduct(nonNullParams(params));
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
        Map<String, Object> queryParams = nonNullParams(params);
        CatalogItemsForSaleResult completeResult = new CatalogItemsForSaleResult();
        completeResult.setList(new ArrayList<>());

        int page = intParam(queryParams, "pi").orElse(1);
        queryParams.put("pi", page);

        while (true) {
            CatalogItemsForSaleResult pageResult = httpClient.catalogItemsForSale(queryParams);
            copyResponseMetadata(completeResult, pageResult);
            completeResult.getList().addAll(pageResult.getList());

            Integer resultsPerPage = pageResult.getRpp();
            Integer totalCount = pageResult.getTotal_count();
            Integer thisPage = pageResult.getPi();
            if (resultsPerPage == null || totalCount == null || thisPage == null || thisPage * resultsPerPage >= totalCount) {
                break;
            }

            page = thisPage + 1;
            queryParams.put("pi", page);
        }

        completeResult.setTotal_count(completeResult.getList().size());
        return completeResult;
    }

    @Override
    public Optional<Item> findCatalogItem(String itemNumber, String itemType) {
        if (itemNumber == null || itemNumber.isBlank()) {
            return Optional.empty();
        }
        String expectedType = normalizeItemType(itemType);
        SearchProductResult result = searchProduct(Map.of("q", itemNumber, "type", expectedType));
        List<Item> exactMatches = Optional.ofNullable(result)
                .map(SearchProductResult::getResult)
                .map(com.bricklink.api.ajax.model.v1.Result::getTypeList)
                .orElse(List.of())
                .stream()
                .filter(type -> Objects.equals(expectedType, type.getType()))
                .map(Type::getItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(item -> Objects.equals(expectedType, item.getTypeItem()))
                .filter(item -> Objects.equals(itemNumber, item.getStrItemNo()))
                .filter(item -> item.getIdItem() != null)
                .toList();

        if (exactMatches.size() > 1) {
            throw new BricklinkAjaxClientException(
                    "Ambiguous BrickLink catalog search for itemNumber [%s], itemType [%s]".formatted(itemNumber, expectedType)
            );
        }
        return exactMatches.stream().findFirst();
    }

    @Override
    public CatalogItemsForSaleResult catalogItemsForSaleByInternalItemId(Integer itemId, String condition, Integer resultsPerPage) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemid", itemId);
        params.put("cond", condition);
        params.put("rpp", Optional.ofNullable(resultsPerPage).orElse(DEFAULT_RESULTS_PER_PAGE));
        params.put("iconly", 0);
        return catalogItemsForSale(params);
    }

    private String normalizeItemType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return SET_TYPE;
        }
        return itemType.trim().toUpperCase();
    }

    private Map<String, Object> nonNullParams(Map<String, Object> params) {
        Map<String, Object> nonNullParams = new HashMap<>();
        if (params == null) {
            return nonNullParams;
        }
        params.forEach((key, value) -> {
            if (key != null && value != null) {
                nonNullParams.put(key, value);
            }
        });
        return nonNullParams;
    }

    private Optional<Integer> intParam(Map<String, Object> params, String key) {
        return Optional.ofNullable(params.get(key))
                .map(String::valueOf)
                .map(Integer::valueOf);
    }

    private void copyResponseMetadata(CatalogItemsForSaleResult target, CatalogItemsForSaleResult source) {
        if (source == null) {
            return;
        }
        target.setReqCnt(source.getReqCnt());
        target.setUpdated(source.getUpdated());
        target.setReturnCode(source.getReturnCode());
        target.setReturnMessage(source.getReturnMessage());
        target.setErrorTicket(source.getErrorTicket());
        target.setProcssingTime(source.getProcssingTime());
        target.setIdColor(source.getIdColor());
        target.setRpp(source.getRpp());
        target.setPi(source.getPi());
        target.setTotal_count(source.getTotal_count());
    }
}
