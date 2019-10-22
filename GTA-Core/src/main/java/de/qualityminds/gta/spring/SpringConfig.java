package de.qualityminds.gta.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import de.qualityminds.gta.config.TestProperties;

@ComponentScan("de.qualityminds.rd.bdd")
@EnableConfigurationProperties({TestProperties.class})
public class SpringConfig {}
