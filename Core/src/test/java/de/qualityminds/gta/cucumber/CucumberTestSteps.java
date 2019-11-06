package de.qualityminds.gta.cucumber;

import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cucumber.api.java.en.Then;
import de.qualityminds.gta.config.SpringConfigTest;
import de.qualityminds.gta.config.TestProperties;

@SpringBootTest(classes = SpringConfigTest.class)
public class CucumberTestSteps {
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
