package de.qualityminds.gta.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import config.SpringConfig;

@EnableConfigurationProperties({TestProperties.class})
public class SpringConfigTest extends SpringConfig {}
