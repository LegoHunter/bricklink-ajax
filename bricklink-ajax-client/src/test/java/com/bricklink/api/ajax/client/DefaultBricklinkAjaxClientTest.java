package com.bricklink.api.ajax.client;

import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.model.v1.Result;
import com.bricklink.api.ajax.model.v1.Type;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultBricklinkAjaxClientTest {
    @Test
    void findCatalogItemReturnsExactPublicItemNumberMatch() {
        CapturingHttpClient httpClient = new CapturingHttpClient();
        httpClient.searchProductResult = searchResult(item("4997", "S", "6390-1", "Main Street"));
        DefaultBricklinkAjaxClient client = new DefaultBricklinkAjaxClient(httpClient);

        Optional<Item> item = client.findCatalogItem("6390-1", "S");

        assertThat(item).hasValueSatisfying(match -> {
            assertThat(match.getIdItem()).isEqualTo(4997);
            assertThat(match.getStrItemNo()).isEqualTo("6390-1");
            assertThat(match.getStrItemName()).isEqualTo("Main Street");
        });
        assertThat(httpClient.searchParams).containsEntry("q", "6390-1")
                .containsEntry("type", "S");
    }

    @Test
    void findCatalogItemReturnsEmptyWhenNoExactMatchExists() {
        CapturingHttpClient httpClient = new CapturingHttpClient();
        httpClient.searchProductResult = searchResult(item("4997", "S", "6390-2", "Main Street"));
        DefaultBricklinkAjaxClient client = new DefaultBricklinkAjaxClient(httpClient);

        assertThat(client.findCatalogItem("6390-1", "S")).isEmpty();
    }

    @Test
    void findCatalogItemFailsWhenExactMatchIsAmbiguous() {
        CapturingHttpClient httpClient = new CapturingHttpClient();
        httpClient.searchProductResult = searchResult(
                item("4997", "S", "6390-1", "Main Street"),
                item("4998", "S", "6390-1", "Main Street Duplicate")
        );
        DefaultBricklinkAjaxClient client = new DefaultBricklinkAjaxClient(httpClient);

        assertThatThrownBy(() -> client.findCatalogItem("6390-1", "S"))
                .hasMessageContaining("Ambiguous BrickLink catalog search");
    }

    @Test
    void catalogItemsForSaleFetchesAllPages() {
        CapturingHttpClient httpClient = new CapturingHttpClient();
        httpClient.catalogPages = List.of(
                catalogPage(1, 2, 3, "page-one-a", "page-one-b"),
                catalogPage(2, 2, 3, "page-two-a")
        );
        DefaultBricklinkAjaxClient client = new DefaultBricklinkAjaxClient(httpClient);

        CatalogItemsForSaleResult result = client.catalogItemsForSaleByInternalItemId(4997, "U", 2);

        assertThat(result.getList()).hasSize(3);
        assertThat(result.getTotal_count()).isEqualTo(3);
        assertThat(httpClient.catalogParams).extracting(params -> params.get("pi"))
                .containsExactly(1, 2);
        assertThat(httpClient.catalogParams.getFirst())
                .containsEntry("itemid", 4997)
                .containsEntry("cond", "U")
                .containsEntry("rpp", 2)
                .containsEntry("iconly", 0);
    }

    private static SearchProductResult searchResult(Item... items) {
        Type type = new Type();
        type.setType("S");
        type.setCount(items.length);
        type.setItems(List.of(items));

        Result result = new Result();
        result.setTypeList(List.of(type));

        SearchProductResult searchProductResult = new SearchProductResult();
        searchProductResult.setResult(result);
        searchProductResult.setReturnCode(0);
        return searchProductResult;
    }

    private static Item item(String idItem, String typeItem, String itemNumber, String itemName) {
        Item item = new Item();
        item.setIdItem(Integer.valueOf(idItem));
        item.setTypeItem(typeItem);
        item.setStrItemNo(itemNumber);
        item.setStrItemName(itemName);
        return item;
    }

    private static CatalogItemsForSaleResult catalogPage(int page, int resultsPerPage, int totalCount, String... descriptions) {
        CatalogItemsForSaleResult result = new CatalogItemsForSaleResult();
        result.setPi(page);
        result.setRpp(resultsPerPage);
        result.setTotal_count(totalCount);
        result.setList(new ArrayList<>());
        for (String description : descriptions) {
            com.bricklink.api.ajax.model.v1.ItemForSale item = new com.bricklink.api.ajax.model.v1.ItemForSale();
            item.setStrDesc(description);
            result.getList().add(item);
        }
        return result;
    }

    private static class CapturingHttpClient implements BricklinkAjaxHttpClient {
        private SearchProductResult searchProductResult;
        private List<CatalogItemsForSaleResult> catalogPages = List.of();
        private Map<String, Object> searchParams;
        private final List<Map<String, Object>> catalogParams = new ArrayList<>();
        private int catalogPageIndex;

        @Override
        public SearchProductResult searchProduct(Map<String, Object> params) {
            searchParams = Map.copyOf(params);
            return searchProductResult;
        }

        @Override
        public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
            catalogParams.add(Map.copyOf(params));
            return catalogPages.get(catalogPageIndex++);
        }
    }
}
