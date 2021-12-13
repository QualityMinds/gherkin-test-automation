package de.qualityminds.gta.runner;

import io.cucumber.junit.CucumberOptions;

import org.junit.runner.RunWith;


@RunWith(CucumberWithSerenityBDDStackRunner.class)
@CucumberOptions(features = "src/test/resources/features/")
public abstract class AbstractCucumberAcceptanceRunner {
	protected AbstractCucumberAcceptanceRunner() {
	}
}
