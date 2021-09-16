package com.gant.trade.config.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class I18NAutoConfiguration {

    @Bean
    public I18NUtil i18NUtil() {
        return new I18NUtilImpl();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("META-INF/locale/gant-trade-exceptions");
        return messageSource;
    }
}
