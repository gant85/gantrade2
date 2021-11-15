package com.gant.trade.domain.mapper;

import com.gant.trade.domain.Order;
import com.gant.trade.rest.model.OrderTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order convert(OrderTO source);

    List<Order> convert(List<OrderTO> list);

    @InheritInverseConfiguration
    OrderTO convert(Order source);

    @InheritInverseConfiguration
    List<OrderTO> convertList(List<Order> list);

}
