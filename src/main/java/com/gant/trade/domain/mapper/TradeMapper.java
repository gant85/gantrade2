package com.gant.trade.domain.mapper;

import com.gant.trade.domain.Trade;
import com.gant.trade.rest.model.TradeTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = OrderMapper.class)
public interface TradeMapper {

    Trade convert(TradeTO source);

    List<Trade> convert(List<TradeTO> list);

    @InheritInverseConfiguration
    TradeTO convert(Trade source);

    @InheritInverseConfiguration
    List<TradeTO> convertList(List<Trade> list);

}
