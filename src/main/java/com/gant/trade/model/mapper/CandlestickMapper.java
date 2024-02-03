package com.gant.trade.model.mapper;

import com.gant.trade.model.Candlestick;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CandlestickMapper {

    Candlestick map(com.gant.binance.api.client.domain.market.Candlestick source);

    List<Candlestick> mapList(List<com.gant.binance.api.client.domain.market.Candlestick> source);
}
