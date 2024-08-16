package com.bricklink.api.ajax;

import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class PagingBricklinkAjaxClient implements BricklinkAjaxClient {
    private final BricklinkAjaxClient bricklinkAjaxClient;

    @Override
    public SearchProductResult searchProduct(Map<String, Object> params) {
        return bricklinkAjaxClient.searchProduct(params);
    }

    public Set<Item> searchItemsByNumberAndName(String itemNumber, String itemName) {
        Map<String, Object> params = new HashMap<>();
        params.clear();
        params.put("q", itemNumber);
        params.put("type", "S");
        SearchProductResult result = bricklinkAjaxClient.searchProduct(params);

        // Collect a Map with item number keys and item name values.
        Map<String, Item> items = result.getResult().getTypeList().stream().flatMap(type -> type.getItems().stream()).collect(Collectors.toMap(Item::getStrItemNo, Function.identity()));
        Map<String, Set<String>> foundItemNameWords = items.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> splitWords(e.getValue().getStrItemName())));
        Map<String, Integer> foundMatchedItemWordCounts = foundItemNameWords.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> wordMatchCount(e.getValue(), splitWords(itemName))));

        String maxWordMatchedItem = null;
        final Integer maxMatchedCount = foundMatchedItemWordCounts.values().stream().mapToInt(v -> v).max().orElse(0);
        Map<String, Integer> matchedMap = foundMatchedItemWordCounts.entrySet().stream().filter(e -> e.getValue().equals(maxMatchedCount)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<String> matchItemNumbers = new HashSet<>(matchedMap.keySet());
        return items.values().stream().filter(item -> matchItemNumbers.contains(item.getStrItemNo())).collect(Collectors.toSet());
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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> splitWords(final String string) {
        return Optional.ofNullable(string)
                .map(String::toLowerCase)
                .map(s -> s.split("(?:(?<![a-zA-Z])'|'(?![a-zA-Z])|[^a-zA-Z'])+"))
                .map(Arrays::asList)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
    }

    public Integer wordMatchCount(final Set<String> foundItemNameWords, final Set<String> searchItemNameWords) {
        int foundItemNameWordsCount = foundItemNameWords.size();
        foundItemNameWords.removeAll(searchItemNameWords);
        int remainingfoundItemNameWordsCount = foundItemNameWords.size();
        return foundItemNameWordsCount - remainingfoundItemNameWordsCount;
    }
}
