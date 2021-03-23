package de.qualityminds.gta.webapplication;

import de.qualityminds.gta.runner.RunnerInit;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = "singleton")
public class WebDriverLoader implements RunnerInit {
	
	@Override
	public void init() {
		setupWebDriver();
	}
		
	private void setupWebDriver() {
		if (System.getProperty("webdriver.driver") != null && !System.getProperty("webdriver.driver").isEmpty()) {
			switch (System.getProperty("webdriver.driver")) {
			case "chrome":
				if (System.getProperty("webdriver.chrome.driver") == null
						|| System.getProperty("webdriver.chrome.driver").isEmpty()) {
					WebDriverManager.chromedriver().setup();
				}
				break;
			case "opera":
				if (System.getProperty("webdriver.opera.driver") == null
						|| System.getProperty("webdriver.opera.driver").isEmpty()) {
					WebDriverManager.operadriver().setup();
				}
				break;
			case "ie":
				if (System.getProperty("webdriver.ie.driver") == null
						|| System.getProperty("webdriver.ie.driver").isEmpty()) {
					WebDriverManager.iedriver().setup();
				}
				break;
			case "edge":
				if (System.getProperty("webdriver.edge.driver") == null
						|| System.getProperty("webdriver.edge.driver").isEmpty()) {
					WebDriverManager.edgedriver().setup();
				}
				break;
			case "phantomjs":
				if (System.getProperty("phantomjs.binary.path") == null
						|| System.getProperty("phantomjs.binary.path").isEmpty()) {
					WebDriverManager.phantomjs().setup();
				}
				break;
			}
		}
	}
}