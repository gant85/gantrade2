package com.gant.trade.domain.mapper;

import com.gant.trade.domain.Strategy;
import com.gant.trade.rest.model.StrategyTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface StrategyMapper {

    Strategy convert(StrategyTO source);

    List<Strategy> convert(List<StrategyTO> list);

    @InheritInverseConfiguration
    StrategyTO convert(Strategy source);

    @InheritInverseConfiguration
    List<StrategyTO> convertList(List<Strategy> list);

}
