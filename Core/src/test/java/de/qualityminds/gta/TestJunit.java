package de.qualityminds.gta;

import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.qualityminds.gta.config.SpringTestConfig;
import de.qualityminds.gta.config.TestProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
public class TestJunit {

	@Autowired
	TestProperties testProperties;

	@Test
	public void testAutowired() {
		assertNotNull(testProperties);
		System.out.println("Test-Properties are available.");
	}

	@Test
	public void propertyRead() {
		assertNotNull(testProperties.getTestProperty());
		System.out.println("Test Property read from config : " + testProperties.getTestProperty());
	}
}
