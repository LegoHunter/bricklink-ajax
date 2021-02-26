package com.bricklink.api.ajax.support;

import com.bricklink.api.ajax.model.v1.AjaxResult;
import com.bricklink.api.ajax.model.v1.ItemForSale;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class CatalogItemsForSaleResult extends AjaxResult {
    private Integer total_count;
    private Integer idColor;
    private Integer rpp;
    private Integer pi;
    private List<ItemForSale> list;

    public List<ItemForSale> getList() {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }
}
