package com.bricklink.api.ajax;

import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import feign.Feign;
import feign.QueryMap;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

public interface BricklinkAjaxClient {
    @RequestLine("GET /com.bricklink.api.ajax/clone/search/searchproduct.com.bricklink.api.ajax?" +
            "q={q}&" +
            "st={st}&" +
            "cond={cond}&" +
            "brand={brand}&" +
            "type={type}&" +
            "cat={cat}&" +
            "yf={yf}&" +
            "yt={yt}&" +
            "loc={loc}&" +
            "reg={reg}&" +
            "ca={ca}&" +
            "ss={ss}&" +
            "pmt={pmt}&" +
            "nmp={nmp}&" +
            "color={color}&" +
            "min={min}&" +
            "max={max}&" +
            "minqty={minqty}&" +
            "nosuperlot={nosuperlot}&" +
            "incomplete={incomplete}&" +
            "showempty={showempty}&" +
            "rpp={rpp}&" +
            "pi={pi}&" +
            "ci={ci}")
    SearchProductResult searchProduct(@QueryMap Map<String, Object> params);

    @RequestLine("GET /com.bricklink.api.ajax/clone/catalogifs.com.bricklink.api.ajax?" +
            "itemid={itemid}&" +
            "cond={cond}&" +
            "rpp={rpp}")
    CatalogItemsForSaleResult catalogItemsForSale(@QueryMap Map<String, Object> params);
}
