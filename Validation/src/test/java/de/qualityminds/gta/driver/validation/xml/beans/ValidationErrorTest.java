package de.qualityminds.gta.driver.validation.xml.beans;

import static org.assertj.core.api.Assertions.assertThat;

import de.qualityminds.gta.driver.validation.xml.XPathMatcherTestImpl;
import org.junit.jupiter.api.Test;

class ValidationErrorTest {

  @Test
  void testToStringMatchCountError() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    data.setXPath("XPath");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.MatchCountError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "Abweichung in Information #3 (XPathMatcherTestName): 5x gefunden, aber 20x erwartet.\nDetails:\n"
    + "\t\\--> XPath: XPath\nInformationsobjekt: XPathMatcherTestImpl(msgId=null)";
    assertThat(result.substring(1)).isEqualTo(expected);
  }

  @Test
  void testToStringFieldValueError() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    data.setXPath("XPath");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.FieldValueError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "Feld fieldName: erwartet \"two\", aber war \"five\".\n\t\\--> XPath: XPath";
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testToStringFieldValueErrorNoXPath() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.FieldValueError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "Feld fieldName: erwartet \"two\", aber war \"five\".\n\t\\--> XPath: ";
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testToStringFirstFieldValueError() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    data.setXPath("XPath");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.FirstFieldValueError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "\nAbweichung in Information #3 (XPathMatcherTestName): Hauptpfad 1x gefunden, aber Feldabweichung";
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testToStringPathNotFoundError() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    data.setXPath("XPath");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.PathNotFoundError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "\nAbweichung in Information #3 (XPathMatcherTestName): Eintrag nicht gefunden.\n\t\\--> XPath: XPath";
    assertThat(expected).isEqualTo(result);
  }

  @Test
  void testToStringPathNotFoundErrorWithNoNamespace() {
    XPathMatcher matcher = new XPathMatcherTestImpl();
    XPathAnalyzeData data = new XPathAnalyzeData();
    data.setActualMatchCount(5);
    data.setActualValue("five");
    data.setExpectedValue("two");
    data.setFieldName("fieldName");
    data.setXPath("/thisIsABlankNamespacePlaceholder:/ [thisIsABlankNamespacePlaceholder:]");
    String expectedMatchCount = "20";
    MatchingErrorType errorType = MatchingErrorType.PathNotFoundError;
    Integer matcherPos = 2;
    ValidationError validationError = new ValidationError(matcher, data, expectedMatchCount, errorType, matcherPos);
    String result = validationError.toString();
    String expected = "\nAbweichung in Information #3 (XPathMatcherTestName): Eintrag nicht gefunden.\n\t\\--> XPath: // []";
    assertThat(expected).isEqualTo(result);
  }
}
