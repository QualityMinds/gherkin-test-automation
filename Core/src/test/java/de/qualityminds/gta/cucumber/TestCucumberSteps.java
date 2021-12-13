package de.qualityminds.gta.cucumber;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.java.en.Then;

import de.qualityminds.gta.config.SpringTestConfig;
import de.qualityminds.gta.config.TestProperties;

@SpringBootTest(classes = SpringTestConfig.class)
public class TestCucumberSteps {
	@Autowired
	TestProperties testProperties;

	@Then("step executes")
	public void stepExecutes() {
		System.out.println("Step works");
	}

	@Then("injection works")
	public void injectionWorks() {
		assertNotNull(testProperties);
		System.out.println("Injection works");
	}

	@Then("test property is correctly read")
	public void testPropertyIsCorrectlyRead() {
		assertNotNull(testProperties.getTestProperty());
		System.out.println("Test Property from config : " + testProperties.getTestProperty());
	}
}
