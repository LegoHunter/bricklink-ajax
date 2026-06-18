package com.bricklink.api.ajax.autoconfigure;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.model.v1.Item;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class BricklinkAjaxClientIntegrationTest {
    private WireMockServer server;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void resolvesInternalCatalogItemIdFromSearchProductAjax() {
        server.stubFor(get(urlPathEqualTo("/ajax/clone/search/searchproduct.ajax"))
                .withQueryParam("q", equalTo("6390-1"))
                .withQueryParam("type", equalTo("S"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "result": {
                                    "typeList": [{
                                      "type": "S",
                                      "count": 1,
                                      "items": [{
                                        "idItem": 4997,
                                        "typeItem": "S",
                                        "strItemNo": "6390-1",
                                        "strItemName": "Main Street"
                                      }]
                                    }],
                                    "nCustomItemCnt": 0
                                  },
                                  "returnCode": 0,
                                  "returnMessage": ""
                                }
                                """)));

        contextRunner().run(context -> {
            Item item = context.getBean(BricklinkAjaxClient.class)
                    .findCatalogItem("6390-1", "S")
                    .orElseThrow();

            assertThat(item.getIdItem()).isEqualTo(4997);
            assertThat(item.getStrItemNo()).isEqualTo("6390-1");
        });
    }

    @Test
    void fetchesCatalogItemsForSaleByInternalItemId() {
        server.stubFor(get(urlPathEqualTo("/ajax/clone/catalogifs.ajax"))
                .withQueryParam("itemid", equalTo("4997"))
                .withQueryParam("cond", equalTo("U"))
                .withQueryParam("rpp", equalTo("500"))
                .withQueryParam("iconly", equalTo("0"))
                .withQueryParam("pi", equalTo("1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "total_count": 1,
                                  "rpp": 500,
                                  "pi": 1,
                                  "list": [{
                                    "idInv": 100,
                                    "strDesc": "Complete with instructions",
                                    "codeNew": "U",
                                    "codeComplete": "C",
                                    "n4Qty": 1,
                                    "mDisplaySalePrice": "US $220.00"
                                  }]
                                }
                                """)));

        contextRunner().run(context -> {
            CatalogItemsForSaleResult result = context.getBean(BricklinkAjaxClient.class)
                    .catalogItemsForSaleByInternalItemId(4997, "U", 500);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().getFirst().getSalePrice()).isEqualTo(220.00d);
        });
    }

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BricklinkAjaxAutoConfiguration.class))
                .withPropertyValues(
                        "bricklink.ajax.uri=" + server.baseUrl(),
                        "bricklink.ajax.rate-limit.enabled=false"
                );
    }
}
