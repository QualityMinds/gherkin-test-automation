package de.qualityminds.gta.driver.compare;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComparisonError {
	private String actualValue;
	private String expectedValue;
}
