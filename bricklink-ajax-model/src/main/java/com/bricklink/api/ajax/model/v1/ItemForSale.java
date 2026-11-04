package com.bricklink.api.ajax.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Slf4j
public class  ItemForSale {
    private Integer idInv;
    private String strDesc;
    private String codeNew;
    private String codeComplete;
    private String strInvImgUrl;
    private Integer idInvImg;
    private String typeInvImg;
    private Integer n4Qty;
    private Integer idColorDefault;
    private String typeImgDefault;
    private Integer hasExtendedDescription;
    private Boolean instantCheckout;
    @JsonProperty("mDisplaySalePrice")
    private String mDisplaySalePrice;
    @JsonProperty("mInvSalePrice")
    private String mInvSalePrice;
    private Integer nSalePct;
    private Integer nTier1Qty;
    private Integer nTier2Qty;
    private Integer nTier3Qty;
    private String nTier1DisplayPrice;
    private String nTier2DisplayPrice;
    private String nTier3DisplayPrice;
    private String nTier1InvPrice;
    private String nTier2InvPrice;
    private String nTier3InvPrice;
    private Integer idColor;
    private String strCategory;
    private String strStorename;
    private Integer idCurrencyStore;
    @JsonProperty("mMinBuy")
    private String mMinBuy;
    private String strSellerUsername;
    private Integer n4SellerFeedbackScore;
    private String strSellerCountryName;
    private String strSellerCountryCode;
    private String strColor;

    /**
     * BrickLink price strings: a two-letter prefix with a symbol ({@code "US $1,234.56"}, {@code "AU $8.00"}) or a
     * three-letter currency code ({@code "EUR 23.00"}, {@code "HUF 15,000.00"}).
     */
    private static final Pattern PRICE_PATTERN = Pattern.compile("^([A-Z]{2,3})\\s?([^\\d\\s]?)\\s*([\\d,]+(?:\\.\\d+)?)$");

    @ToString.Include
    public Double getSalePrice() {
        return amount(mDisplaySalePrice);
    }

    @ToString.Include
    public Double getMinBuy() {
        return amount(mMinBuy);
    }

    /**
     * ISO currency code of {@link #getSalePrice()}: BrickLink's display currency for the browsing session, read from
     * {@code mDisplaySalePrice}. {@code "US $"} is {@code USD}, {@code "AU $"} is {@code AUD}, {@code "EUR"} is
     * {@code EUR}. Null when the price string is missing or not recognized.
     */
    @ToString.Include
    public String getDisplayCurrencyCode() {
        Matcher matcher = matcher(mDisplaySalePrice);
        if (matcher == null) {
            return null;
        }
        String prefix = matcher.group(1);
        if (prefix.length() == 3) {
            return prefix;
        }
        return "$".equals(matcher.group(2)) ? prefix + "D" : null;
    }

    private static Double amount(String price) {
        Matcher matcher = matcher(price);
        if (matcher == null) {
            return 0.0d;
        }
        try {
            return new BigDecimal(matcher.group(3).replace(",", "")).doubleValue();
        } catch (NumberFormatException e) {
            log.error("Unable to parse decimal number [{}]", matcher.group(3));
            return 0.0d;
        }
    }

    private static Matcher matcher(String price) {
        if (price == null) {
            return null;
        }
        Matcher matcher = PRICE_PATTERN.matcher(price.trim());
        return matcher.matches() ? matcher : null;
    }
}
