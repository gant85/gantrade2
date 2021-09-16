package com.gant.trade.domain.mapper;

import com.gant.trade.domain.User;
import com.gant.trade.rest.model.ExchangeConfiguration;
import com.gant.trade.rest.model.UserCreateRequest;
import com.gant.trade.rest.model.UserTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper {

    User convert(UserTO source);

    List<User> convert(List<UserTO> list);

    User convert(UserCreateRequest source);

    @InheritInverseConfiguration
    UserTO convert(User source);

    @InheritInverseConfiguration
    List<UserTO> convertList(List<User> list);

    @AfterMapping
    default void convertExchange(User user, @MappingTarget UserTO userTO) {
        if(user.getExchangeConfiguration() != null) {
            userTO.setExchange(user.getExchangeConfiguration().stream().map(ExchangeConfiguration::getExchangeTO).collect(Collectors.toList()));
        }
    }
}
