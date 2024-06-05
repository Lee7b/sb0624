package com.sburkett.toolrentalapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "application.tool-info")
@Getter
@Setter
public class ToolConfig {
    private Map<String, Map<String, String>> toolInfoMap;
    private Map<String, Map<String, String>> toolPriceMap;
}
