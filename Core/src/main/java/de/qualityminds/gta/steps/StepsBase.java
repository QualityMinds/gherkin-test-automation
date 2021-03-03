package de.qualityminds.gta.steps;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import de.qualityminds.gta.SpringConfig;

@SpringBootTest(classes = SpringConfig.class)
@ContextConfiguration(classes = {SpringConfig.class})
public abstract class StepsBase {

}