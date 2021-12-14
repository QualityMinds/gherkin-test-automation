package de.qualityminds.gta.driver.validation.xml.matchcount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.validation.xml.XPathMatcherTestImpl;
import de.qualityminds.gta.driver.validation.xml.beans.MatchingErrorType;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationError;
import de.qualityminds.gta.driver.validation.xml.beans.XPathAnalyzeData;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;
import de.qualityminds.gta.driver.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExpectedMatchCountOneUniqueOrMoreTest {

	@Test
	void testGetXpathAnalyzeDataListNoIdField() {
		String baseXPath = "XPath";
		List<XPathField> fields = new ArrayList<>();
		XPathField field = new XPathField().setFieldValue("fieldValue1").setXPath("XPath1").setJsonPath("jsonPath1");
		fields.add(field);
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
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
		assertThat(data.getXPath()).isEqualTo("XPath[XPath1='fieldValue1']");
	}

	@Test
	void testGetXpathAnalyzeDataListWithIdField() {
		String baseXPath = "XPath";
		List<XPathField> fields = new ArrayList<>();
		XPathField field1 = new XPathField().setFieldValue("fieldValue1").setXPath("XPath1").setJsonPath("jsonPath1");
		fields.add(field1);
		XPathField field2 = new XPathField().setFieldValue("fieldValue2").setXPath("XPath2").setJsonPath("jsonPath2").setId(true);
		fields.add(field2);
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		List<XPathAnalyzeData> result = matchCount.getXpathAnalyzeDataList(baseXPath, fields);
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isInstanceOf(XPathAnalyzeData.class);
		XPathAnalyzeData data = result.get(0);
		assertThat(data.getActualMatchCount()).isNull();
		assertThat(data.getActualValue()).isNull();
		assertThat(data.getExpectedValue()).isNull();
		assertThat(data.getFieldName()).isNull();
		assertThat(data.getExpression()).isNull();
		assertThat(data.getXPath()).isEqualTo("XPath[XPath2='fieldValue2']");
		assertThat(result.get(1)).isInstanceOf(XPathAnalyzeData.class);
		XPathAnalyzeData data1 = result.get(1);
		assertThat(data1.getActualMatchCount()).isNull();
		assertThat(data1.getActualValue()).isNull();
		assertThat(data1.getExpectedValue()).isEqualTo("fieldValue1");
		assertThat(data1.getFieldName()).isEqualTo("jsonPath1");
		assertThat(data1.getExpression()).isNull();
		assertThat(data1.getXPath()).isEqualTo("XPath[XPath2='fieldValue2']/XPath1/text()");
	}

	@Test
	void testValidateEmptyAnalyzeDataList() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeData = new ArrayList<>();
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		assertThrows(ValidationException.class, () -> matchCount.validate(data, xPathAnalyzeData, matcher, i, preReportData, reportData, validationErrorList));
	}

	@Test
	void testValidateNoMatchCount() throws ValidationException {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData().setXPath("AnalyzeXPath").setFieldName("AnalyzeFieldName");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		assertThrows(ValidationException.class, () -> matchCount.validate(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList));
	}

	@Test
	void testValidateActualMatchCountZero() throws ValidationException {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData().setXPath("AnalyzeXPath").setFieldName("AnalyzeFieldName").setActualMatchCount(0);
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validate(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo("preReportDataEintrag nicht gefunden.");
		assertThat(validationErrorList.size()).isEqualTo(1);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(xPathAnalyzeData);
		assertThat(error.expectedMatchCount).isEqualTo("1(+)");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.PathNotFoundError);
		assertThat(error.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountOne() throws ValidationException {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData().setXPath("AnalyzeXPath").setFieldName("AnalyzeFieldName").setActualMatchCount(1);
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validate(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo("preReportDataEintrag wie gesucht 1x gefunden.");
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidateIdFieldsNotZero() throws ValidationException {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData().setXPath("AnalyzeXPath").setFieldName("AnalyzeFieldName").setActualMatchCount(1);
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl().setMsgId("1");
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validate(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo("preReportDataHauptpfad 1x vorhanden und alle Felder wie erwartet belegt.");
		assertThat(validationErrorList.size()).isZero();
	}

}
