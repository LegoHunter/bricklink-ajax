package com.bricklink.api.ajax.model.v1;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public abstract class AjaxResult {
    private Integer reqCnt;
    private Integer updated;
    private Integer returnCode;
    private String returnMessage;
    private Integer errorTicket;
    private Integer procssingTime;
}
