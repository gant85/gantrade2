package com.gant.trade.websocket.bybit.mapper;

import com.gant.trade.model.CandlestickEvent;
import com.gant.trade.websocket.bybit.model.TopicKLineData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface TopicKLineDataMapper {

    @Mapping(target = "eventTime", source = "timestamp")
    @Mapping(target = "openTime", source = "start")
    @Mapping(target = "closeTime", source = "end")
    @Mapping(target = "isBarFinal", source = "confirm")
    CandlestickEvent map(TopicKLineData source);

    List<CandlestickEvent> mapList(List<TopicKLineData> source);

}
