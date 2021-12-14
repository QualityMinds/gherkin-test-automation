package de.qualityminds.gta.driver.validation.xml;

import static org.assertj.core.api.Assertions.assertThat;

import de.qualityminds.gta.driver.validation.xml.beans.Matchers;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationReport;
import de.qualityminds.gta.driver.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class XPathValidationUtilTest {

	@Test
	void testValidateXMLParseResultAllEmpty() throws ValidationException {
		XPathValidationUtil util = new XPathValidationUtil();
		Matchers matchers = new Matchers();
		ValidationReport report = util.validateXMLParseResult(matchers);
		assertThat(report.getReport().size()).isEqualTo(2);
		assertThat(report.getReport().get(0)).isEqualTo(XPathValidationUtil.XML_STRUCTURES_HEADER);
		assertThat(report.getReport().get(1)).isEmpty();
		assertThat(report.getValidationErrors().size()).isZero();
		assertThat(report.isErrorOccurred()).isFalse();
	}

}
