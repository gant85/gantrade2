package com.gant.trade.model.mapper;

import com.gant.trade.model.CandlestickEvent;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CandlestickEventMapper {

    CandlestickEvent map(com.gant.binance.api.client.domain.event.CandlestickEvent source);

    List<CandlestickEvent> mapList(List<com.gant.binance.api.client.domain.event.CandlestickEvent> source);
}
