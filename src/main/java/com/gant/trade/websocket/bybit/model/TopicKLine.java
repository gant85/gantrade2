package com.gant.trade.websocket.bybit.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TopicKLine {
    private String topic;
    private List<TopicKLineData> data;
    private Long ts;
    private String type;
}
