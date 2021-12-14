package de.qualityminds.gta.driver.validation.xml.beans;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ValidationReport {
	private List<String> report;
	private List<ValidationError> validationErrors;
	private boolean errorOccurred;
}
