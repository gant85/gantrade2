package com.gant.trade.exception;

import com.gant.trade.config.i18n.I18NUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class ExceptionUtil {

    @Autowired
    private I18NUtil i18NUtil;

    public String getLocalizedCode(String prefix) {
        String codeKey = prefix.concat(".errorCode");
        return this.i18NUtil.getMessage(codeKey);
    }

    public StatusType getCustomStatus(String prefix, String status) {
        String codeKey = prefix.concat(".errorStatus");
        String s = Optional.ofNullable(status).orElseGet(() -> {
            try {
                return String.valueOf(Integer.parseInt(this.i18NUtil.getMessage(codeKey)));
            } catch (Exception e) {
                log.debug("Incorrect status for {}", codeKey);
                return null;
            }
        });

        return StringUtils.isEmpty(s) ?
                Status.BAD_REQUEST :
                Arrays.stream(Status.values()).filter(value -> value.getStatusCode() == Integer.parseInt(s))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("HTTP code '" + s + "' is not allowed"));
    }

    public String getLocalizedDetail(String prefix, Object[] parameters) {
        String detailKey = prefix.concat(".errorMessage");
        return parameters != null && parameters.length > 0 ? this.i18NUtil.formatCompoundMessage(detailKey, parameters) : this.i18NUtil.getMessage(detailKey);
    }

    public String getkeyPrefix(Exception e) {
        String prefix = e.getClass().getName();
        if (e.getClass() == BusinessRuntimeException.class) {
            prefix = e.getClass().getName().concat(".").concat(((BusinessRuntimeException) e).getCode());
        }
        return prefix;
    }
}
