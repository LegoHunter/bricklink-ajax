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
    void getSalePriceParsesThreeLetterAndOtherDollarCurrencyFormats() {
        assertThat(displayPrice("EUR 23.00").getSalePrice()).isEqualTo(23.00d, within(0.001d));
        assertThat(displayPrice("EUR 12.3884").getSalePrice()).isEqualTo(12.3884d, within(0.00001d));
        assertThat(displayPrice("HUF 15,000.00").getSalePrice()).isEqualTo(15000.00d, within(0.001d));
        assertThat(displayPrice("AU $8.00").getSalePrice()).isEqualTo(8.00d, within(0.001d));
    }

    @Test
    void getDisplayCurrencyCodeReadsTheCurrencyFromTheDisplayPrice() {
        assertThat(displayPrice("US $220.00").getDisplayCurrencyCode()).isEqualTo("USD");
        assertThat(displayPrice("AU $8.00").getDisplayCurrencyCode()).isEqualTo("AUD");
        assertThat(displayPrice("CA $10.00").getDisplayCurrencyCode()).isEqualTo("CAD");
        assertThat(displayPrice("EUR 23.00").getDisplayCurrencyCode()).isEqualTo("EUR");
        assertThat(displayPrice("CHF 9.90").getDisplayCurrencyCode()).isEqualTo("CHF");
    }

    @Test
    void getDisplayCurrencyCodeIsNullWhenThePriceIsMissingOrUnrecognized() {
        assertThat(new ItemForSale().getDisplayCurrencyCode()).isNull();
        assertThat(displayPrice("$220.00").getDisplayCurrencyCode()).isNull();
        assertThat(displayPrice("US 220.00").getDisplayCurrencyCode()).isNull();
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

    private static ItemForSale displayPrice(String price) {
        ItemForSale itemForSale = new ItemForSale();
        itemForSale.setMDisplaySalePrice(price);
        return itemForSale;
    }
}
