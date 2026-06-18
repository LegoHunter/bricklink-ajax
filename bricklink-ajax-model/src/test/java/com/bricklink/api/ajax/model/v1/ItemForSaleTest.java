package com.bricklink.api.ajax.model.v1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ItemForSaleTest {
    @Test
    void getSalePriceParsesBricklinkDisplayPrice() {
        ItemForSale itemForSale = new ItemForSale();
        itemForSale.setMDisplaySalePrice("US $220.00");

        assertThat(itemForSale.getSalePrice()).isEqualTo(220.00d, within(0.001d));
    }

    @Test
    void getSalePriceParsesGroupedDisplayPrice() {
        ItemForSale itemForSale = new ItemForSale();
        itemForSale.setMDisplaySalePrice("US $1,234.56");

        assertThat(itemForSale.getSalePrice()).isEqualTo(1234.56d, within(0.001d));
    }

    @Test
    void getMinBuyParsesBricklinkMinimumBuyPrice() {
        ItemForSale itemForSale = new ItemForSale();
        itemForSale.setMMinBuy("US $10.50");

        assertThat(itemForSale.getMinBuy()).isEqualTo(10.50d, within(0.001d));
    }

    @Test
    void priceParsingReturnsZeroForNullMalformedOrNonMatchingValues() {
        ItemForSale nullPrice = new ItemForSale();
        ItemForSale malformedPrice = new ItemForSale();
        malformedPrice.setMDisplaySalePrice("US $not-a-number");
        ItemForSale nonMatchingPrice = new ItemForSale();
        nonMatchingPrice.setMDisplaySalePrice("$220.00");

        assertThat(nullPrice.getSalePrice()).isZero();
        assertThat(malformedPrice.getSalePrice()).isZero();
        assertThat(nonMatchingPrice.getSalePrice()).isZero();
    }
}
