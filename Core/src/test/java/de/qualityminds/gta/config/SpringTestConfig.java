package de.qualityminds.gta.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TestProperties.class})
public class SpringTestConfig extends SpringConfig {
}
