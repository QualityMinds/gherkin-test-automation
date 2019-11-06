package de.qualityminds.gta;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.qualityminds.gta.config.SpringConfigTest;
import de.qualityminds.gta.config.TestProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringConfigTest.class)	
public class TestJunit {	
	@Autowired
	TestProperties testProperties;
	
	@Test
	public void testAutowired() {
		assertNotNull(testProperties);
	}
	
	@Test
	public void propertyRead() {
		assertNotNull(testProperties.getTestProperty());
		System.out.println("Test Property read from config : " + testProperties.getTestProperty());
	}
}
