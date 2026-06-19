package com.bricklink.api.ajax.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.text.ParseException;
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

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Pattern salePricePattern = Pattern.compile("^([A-Z]{2})\\s(.)(.*)$");

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final DecimalFormat decimalFormat = new DecimalFormat("0,000,000.00");

    @ToString.Include
    public Double getSalePrice() {
        return getDoubleFromSalePricePatternString(mDisplaySalePrice);
    }

    @ToString.Include
    public Double getMinBuy() {
        return getDoubleFromSalePricePatternString(mMinBuy);
    }

    private Double getDoubleFromSalePricePatternString(String salePricePatternString) {
        if (null != salePricePatternString) {
            Matcher salePriceMatcher = salePricePattern.matcher(salePricePatternString);
            if (salePriceMatcher.matches()) {
                try {
                    return decimalFormat.parse(salePriceMatcher.group(3)).doubleValue();
                } catch (ParseException e) {
                    log.error("Unable to parse decimal number [{}]", salePriceMatcher.group(3));
                }
            }
        }
        return 0.0d;
    }
}
