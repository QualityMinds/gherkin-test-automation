package de.qualityminds.gta;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.qualityminds.gta.config.TestProperties;
import de.qualityminds.gta.spring.SpringConfig;

@SpringBootTest(classes = SpringConfig.class)	
public class TestJunit {	
	@Autowired
	TestProperties properties;
	
	@Test
	public void testAutowired() {
		assertNotNull(properties);
	}
	
	@Test
	public void propertyRead() {
		assertNotNull(properties.getTestProperty());
		System.out.println("Test Property read from config : " + properties.getTestProperty());
	}
}
