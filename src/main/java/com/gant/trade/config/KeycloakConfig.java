package com.gant.trade.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {
    static Keycloak keycloak = null;
    private String authServerUrl;
    private String realm;
    private String resource;
    private Credentials credentials;

    @Setter
    @Getter
    @ConfigurationProperties(prefix = "keycloak.credentials")
    public static class Credentials {
        private String secret;
    }
}
