package de.qualityminds.gta.driver.validation.xml.matchcount;

import static de.qualityminds.gta.driver.validation.xml.beans.ValidationError.*;
import static org.assertj.core.api.Assertions.assertThat;

import de.qualityminds.gta.driver.validation.xml.XPathMatcherTestImpl;
import de.qualityminds.gta.driver.validation.xml.beans.MatchingErrorType;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationError;
import de.qualityminds.gta.driver.validation.xml.beans.XPathAnalyzeData;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExpectedMatchCountTest {

	@Test
	void testValidateActualMatchCountOneXPathAnalyzeDataEmpty() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_AND_EVERYTHING_FINE);
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidateActualMatchCountZero() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(0);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + String.format(FOUND_BUT_MORE_THAN_ONCE, 0));
		assertThat(validationErrorList.size()).isEqualTo(1);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(data);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.MatchCountError);
		assertThat(error.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountNotOneMatchCountOneValueEqualWithAsteriks() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue*")
				.setActualValue("expectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + String.format(FOUND_BUT_MORE_THAN_ONCE, 10));
		assertThat(validationErrorList.size()).isEqualTo(1);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(data);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.MatchCountError);
		assertThat(error.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountOneMatchCountOneDoesntEqual() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue*")
				.setActualValue("notExpectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_BUT_FIELD_ERROR);
		assertThat(validationErrorList.size()).isEqualTo(2);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(xPathAnalyzeData);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.FirstFieldValueError);
		assertThat(error.matcherPos).isEqualTo(1);
		ValidationError error2 = validationErrorList.get(1);
		assertThat(error2.matcher).isEqualTo(matcher);
		assertThat(error2.data).isEqualTo(xPathAnalyzeData);
		assertThat(error2.expectedMatchCount).isEqualTo("1");
		assertThat(error2.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		assertThat(error2.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountOneMatchCountNotOneEquals() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(10)
				.setExpectedValue("expectedValue")
				.setActualValue("expectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_AND_EVERYTHING_FINE);
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidateActualMatchCountOneMatchCountNotOneValuesBlank() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(10)
				.setExpectedValue("")
				.setActualValue("");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_AND_EVERYTHING_FINE);
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidateActualMatchCountOneMatchCountNotOneValuesNull() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(1);
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_AND_EVERYTHING_FINE);
		assertThat(validationErrorList.size()).isZero();
	}

	@Test
	void testValidateActualMatchCountNotOneXPathAnalyzeDataNotEmpty() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(0);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + String.format(FOUND_BUT_MORE_THAN_ONCE, 0));
		assertThat(validationErrorList.size()).isEqualTo(1);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(data);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.MatchCountError);
		assertThat(error.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountZeroXPathAnalyzeDataNotEmpty() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(10);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + String.format(FOUND_BUT_MORE_THAN_ONCE, 10));
		assertThat(validationErrorList.size()).isEqualTo(2);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(data);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.MatchCountError);
		assertThat(error.matcherPos).isEqualTo(1);
		ValidationError error2 = validationErrorList.get(1);
		assertThat(error2.matcher).isEqualTo(matcher);
		assertThat(error2.data).isEqualTo(xPathAnalyzeData);
		assertThat(error2.expectedMatchCount).isEqualTo("1");
		assertThat(error2.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		assertThat(error2.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountOneMatchCountNotOneValuesEqual() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(10)
				.setExpectedValue("expectedValue")
				.setActualValue("notExpectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_BUT_FIELD_ERROR);
		assertThat(validationErrorList.size()).isEqualTo(2);
		ValidationError error = validationErrorList.get(0);
		assertThat(error.matcher).isEqualTo(matcher);
		assertThat(error.data).isEqualTo(xPathAnalyzeData);
		assertThat(error.expectedMatchCount).isEqualTo("1");
		assertThat(error.errorType).isEqualTo(MatchingErrorType.FirstFieldValueError);
		assertThat(error.matcherPos).isEqualTo(1);
		ValidationError error2 = validationErrorList.get(1);
		assertThat(error2.matcher).isEqualTo(matcher);
		assertThat(error2.data).isEqualTo(xPathAnalyzeData);
		assertThat(error2.expectedMatchCount).isEqualTo("1");
		assertThat(error2.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		assertThat(error2.matcherPos).isEqualTo(1);
	}

	@Test
	void testValidateActualMatchCountOneMoreXPathAnalyzeData() {
		XPathAnalyzeData data = new XPathAnalyzeData();
		data.setActualMatchCount(1);
		List<XPathAnalyzeData> xPathAnalyzeDataList = createXPathAnalyzeDataList();
		XPathMatcher matcher = new XPathMatcherTestImpl();
		int i = 1;
		String preReportData = "preReportData";
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();
		ExpectedMatchCountOneUniqueOrMore matchCount = new ExpectedMatchCountOneUniqueOrMore();
		matchCount.validateBasePathAndFields(data, xPathAnalyzeDataList, matcher, i, preReportData, reportData, validationErrorList);
		assertThat(reportData.size()).isEqualTo(1);
		String reportDatum = reportData.get(0);
		assertThat(reportDatum).isEqualTo(preReportData + FOUND_BUT_FIELD_ERROR);
	}

	private List<XPathAnalyzeData> createXPathAnalyzeDataList() {
		List<XPathAnalyzeData> xPathAnalyzeDataList = new ArrayList<>();
		XPathAnalyzeData xPathAnalyzeData = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath")
				.setFieldName("AnalyzeFieldName")
				.setActualMatchCount(10)
				.setExpectedValue("expectedValue")
				.setActualValue("notExpectedValue");
		xPathAnalyzeDataList.add(xPathAnalyzeData);
		XPathAnalyzeData xPathAnalyzeData1 = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath1")
				.setFieldName("AnalyzeFieldName1")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue1")
				.setActualValue("expectedValue1");
		xPathAnalyzeDataList.add(xPathAnalyzeData1);
		XPathAnalyzeData xPathAnalyzeData2 = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath2")
				.setFieldName("AnalyzeFieldName2")
				.setActualMatchCount(0)
				.setExpectedValue("expectedValue2")
				.setActualValue("actualValue2");
		xPathAnalyzeDataList.add(xPathAnalyzeData2);
		XPathAnalyzeData xPathAnalyzeData3 = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath3")
				.setFieldName("AnalyzeFieldName3")
				.setActualMatchCount(1)
				.setExpectedValue("expectedValue3")
				.setActualValue("notExpectedValue3");
		xPathAnalyzeDataList.add(xPathAnalyzeData3);
		XPathAnalyzeData xPathAnalyzeData4 = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath4")
				.setFieldName("AnalyzeFieldName4")
				.setActualMatchCount(10)
				.setExpectedValue("expectedValue4*")
				.setActualValue("expectedValue4");
		xPathAnalyzeDataList.add(xPathAnalyzeData4);
		XPathAnalyzeData xPathAnalyzeData5 = new XPathAnalyzeData()
				.setXPath("AnalyzeXPath5")
				.setFieldName("AnalyzeFieldName5")
				.setActualMatchCount(0)
				.setExpectedValue("expectedValue5")
				.setActualValue("expectedValue5");
		xPathAnalyzeDataList.add(xPathAnalyzeData5);
		return xPathAnalyzeDataList;
	}

	private void assertValidationErrorList(List<ValidationError> validationErrorList) {
		XPathMatcher matcher = new XPathMatcherTestImpl();
		assertThat(validationErrorList.size()).isEqualTo(6);
		for (ValidationError error : validationErrorList) {
			assertThat(error.matcher).isEqualTo(matcher);
			assertThat(error.expectedMatchCount).isEqualTo("1");
			assertThat(error.matcherPos).isEqualTo(1);
		}
		ValidationError error = validationErrorList.get(0);
		assertThat(error.errorType).isEqualTo(MatchingErrorType.FirstFieldValueError);
		XPathAnalyzeData data = error.data;
		assertThat(data.getXPath()).isEqualTo("AnalyzeXPath");
		assertThat(data.getExpression()).isNull();
		assertThat(data.getFieldName()).isEqualTo("AnalyzeFieldName");
		assertThat(data.getExpectedValue()).isEqualTo("expectedValue");
		assertThat(data.getActualValue()).isEqualTo("notExpectedValue");
		assertThat(data.getActualMatchCount()).isEqualTo(10);

		ValidationError error1 = validationErrorList.get(1);
		assertThat(error1.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		XPathAnalyzeData data1 = error1.data;
		assertThat(data1.getXPath()).isEqualTo("AnalyzeXPath");
		assertThat(data1.getExpression()).isNull();
		assertThat(data1.getFieldName()).isEqualTo("AnalyzeFieldName");
		assertThat(data1.getExpectedValue()).isEqualTo("expectedValue");
		assertThat(data1.getActualValue()).isEqualTo("notExpectedValue");
		assertThat(data1.getActualMatchCount()).isEqualTo(10);

		ValidationError error2 = validationErrorList.get(2);
		assertThat(error2.errorType).isEqualTo(MatchingErrorType.FirstFieldValueError);
		XPathAnalyzeData data2 = error2.data;
		assertThat(data2.getXPath()).isEqualTo("AnalyzeXPath2");
		assertThat(data2.getExpression()).isNull();
		assertThat(data2.getFieldName()).isEqualTo("AnalyzeFieldName2");
		assertThat(data2.getExpectedValue()).isEqualTo("expectedValue2");
		assertThat(data2.getActualValue()).isEqualTo("actualValue2");
		assertThat(data2.getActualMatchCount()).isZero();

		ValidationError error3 = validationErrorList.get(3);
		assertThat(error3.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		XPathAnalyzeData data3 = error3.data;
		assertThat(data3.getXPath()).isEqualTo("AnalyzeXPath2");
		assertThat(data3.getExpression()).isNull();
		assertThat(data3.getFieldName()).isEqualTo("AnalyzeFieldName2");
		assertThat(data3.getExpectedValue()).isEqualTo("expectedValue2");
		assertThat(data3.getActualValue()).isEqualTo("actualValue2");
		assertThat(data3.getActualMatchCount()).isZero();

		ValidationError error4 = validationErrorList.get(4);
		assertThat(error4.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		XPathAnalyzeData data4 = error4.data;
		assertThat(data4.getXPath()).isEqualTo("AnalyzeXPath3");
		assertThat(data4.getExpression()).isNull();
		assertThat(data4.getFieldName()).isEqualTo("AnalyzeFieldName3");
		assertThat(data4.getExpectedValue()).isEqualTo("expectedValue3");
		assertThat(data4.getActualValue()).isEqualTo("notExpectedValue3");
		assertThat(data4.getActualMatchCount()).isEqualTo(1);

		ValidationError error5 = validationErrorList.get(5);
		assertThat(error5.errorType).isEqualTo(MatchingErrorType.FieldValueError);
		XPathAnalyzeData data5 = error5.data;
		assertThat(data5.getXPath()).isEqualTo("AnalyzeXPath4");
		assertThat(data5.getExpression()).isNull();
		assertThat(data5.getFieldName()).isEqualTo("AnalyzeFieldName4");
		assertThat(data5.getExpectedValue()).isEqualTo("expectedValue4*");
		assertThat(data5.getActualValue()).isEqualTo("expectedValue4");
		assertThat(data5.getActualMatchCount()).isEqualTo(10);

	}
}
