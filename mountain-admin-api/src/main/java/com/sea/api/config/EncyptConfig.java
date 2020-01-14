package com.sea.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "encrypt")
@Getter
@Setter
public class EncyptConfig {
    private String location;

    private String secret;

    private String alias;

    private String password;
}
