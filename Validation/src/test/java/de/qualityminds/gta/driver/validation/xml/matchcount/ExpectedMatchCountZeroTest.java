package de.qualityminds.gta.driver.validation.xml.matchcount;

import static org.assertj.core.api.Assertions.assertThat;

import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.validation.xml.XPathMatcherTestImpl;
import de.qualityminds.gta.driver.validation.xml.beans.MatchingErrorType;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationError;
import de.qualityminds.gta.driver.validation.xml.beans.XPathAnalyzeData;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExpectedMatchCountZeroTest {

	@Test
	void testGetXpathAnalyzeDataList() {
		String baseXPath = "XPath";
		List<XPathField> fields = new ArrayList<>();
		ExpectedMatchCountZero matchCount = new ExpectedMatchCountZero();
		List<XPathAnalyzeData> result = matchCount.getXpathAnalyzeDataList(baseXPath, fields);
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isInstanceOf(XPathAnalyzeData.class);
		XPathAnalyzeData data = result.get(0);
		assertThat(data.getActualMatchCount()).isNull();
		assertThat(data.getActualValue()).isNull();
		assertThat(data.getExpectedValue()).isNull();
		assertThat(data.getFieldName()).isNull();
		assertThat(data.getExpression()).isNull();
		assertThat(data.getXPath()).isEqualTo("XPath");
	}

	@Test
	void testValidateActualCountZero() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(0);
		List<XPathAnalyzeData> xPathAnalyzeData = null;
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountZero matchCount = new ExpectedMatchCountZero();
		matchCount.validate(data, xPathAnalyzeData, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo("preReportData0x gefunden - erwartet: 0x.");
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidate() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeData = null;
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountZero matchCount = new ExpectedMatchCountZero();
		matchCount.validate(data, xPathAnalyzeData, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo("preReportData10x gefunden - erwartet: 0x.");
		assertThat(validationErrorList.size()).isEqualTo(1);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(data);
		assertThat(error.expectedMatchCount).isEqualTo("0");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.MatchCountError);
		assertThat(error.matcherPos).isEqualTo(1);
	}

}
