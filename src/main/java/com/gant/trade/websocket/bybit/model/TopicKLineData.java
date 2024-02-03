package com.gant.trade.websocket.bybit.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicKLineData {
    private Long start;
    private Long end;
    private String interval;
    private String open;
    private String close;
    private String high;
    private String low;
    private String volume;
    private String turnover;
    private Boolean confirm;
    private Long timestamp;
}
