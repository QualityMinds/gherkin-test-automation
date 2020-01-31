package de.qualityminds.gta.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import de.qualityminds.gta.SpringConfig;

@EnableConfigurationProperties({TestProperties.class})
public class SpringConfigTest extends SpringConfig {}
