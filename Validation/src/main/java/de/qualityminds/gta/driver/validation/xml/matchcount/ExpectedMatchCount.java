package de.qualityminds.gta.driver.validation.xml.matchcount;


import static de.qualityminds.gta.driver.validation.xml.beans.ValidationError.*;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.qualityminds.gta.driver.compare.Comparator;
import de.qualityminds.gta.driver.exceptions.ValidationException;
import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.validation.xml.beans.*;

public abstract class ExpectedMatchCount implements Serializable {
	final String expMatchCount;

	protected ExpectedMatchCount(String expMatchCount) {
		this.expMatchCount = expMatchCount;
	}

	public abstract void validate(XPathAnalyzeData data, List<XPathAnalyzeData> xPathAnalyzeData, XPathMatcher matcher,
								  int i, String preReportData, List<String> reportData,
								  List<ValidationError> validationErrorList) throws ValidationException;

	public abstract List<XPathAnalyzeData> getXpathAnalyzeDataList(String baseXPath, List<XPathField> fields);

	protected void validateBasePathAndFields(XPathAnalyzeData basePathData, List<XPathAnalyzeData> fieldAnalyzeData,
											 XPathMatcher matcher, Integer i, String preReportData,
											 List<String> reportData, List<ValidationError> validationErrorList) {

		boolean basePathCountError = checkBasePathCountError(basePathData, matcher, i, preReportData, reportData, validationErrorList);

		if (basePathData.getActualMatchCount() == 0) {
			return;
		}

		boolean fieldDataMismatch = false;
		for (XPathAnalyzeData xPathData : fieldAnalyzeData) {
			Integer matchCount = xPathData.getActualMatchCount();
			if (matchCount == 1) {
				fieldDataMismatch = checkForMatchCountOne(xPathData, matcher, i, preReportData, reportData, validationErrorList, basePathCountError, fieldDataMismatch);
			} else {
				if (isValuesBlank(xPathData)) {
					continue;
				}
				boolean hasActualFieldError = isFieldError(xPathData);

				if (!basePathCountError && !fieldDataMismatch && hasActualFieldError) {
					reportData.add(preReportData + FOUND_BUT_FIELD_ERROR);
					validationErrorList.add(
							new ValidationError(matcher, xPathData, "1", MatchingErrorType.FirstFieldValueError, i)
					);
				}
				if (hasActualFieldError) {
					fieldDataMismatch = true;
					validationErrorList.add(
							new ValidationError(matcher, xPathData, "1", MatchingErrorType.FieldValueError, i)
					);
				}
			}
		}

		checkPathCountErrorAndFieldMismatch(basePathCountError, fieldDataMismatch, preReportData, reportData);
	}

	private boolean isFieldError(XPathAnalyzeData xPathData) {
		String expVal = xPathData.getExpectedValue();
		String actualValue = xPathData.getActualValue();

		try {
			return null != Comparator.compare(actualValue, expVal);
		} catch (ValidationException ve) {
			return true;
		}
	}

	private boolean checkBasePathCountError(XPathAnalyzeData basePathData,
											XPathMatcher matcher, Integer i, String preReportData,
											List<String> reportData, List<ValidationError> validationErrorList) {
		Integer basePathCount = basePathData.getActualMatchCount();
		if (basePathCount != 1) {
			reportData.add(preReportData + String.format(FOUND_BUT_MORE_THAN_ONCE, basePathCount));
			validationErrorList
					.add(new ValidationError(matcher, basePathData, "1", MatchingErrorType.MatchCountError, i));
			return true;
		}
		return false;
	}

	private void checkPathCountErrorAndFieldMismatch(boolean basePathCountError, boolean fieldDataMismatch,
													 String preReportData, List<String> reportData) {
		if (!basePathCountError && !fieldDataMismatch) {
			reportData.add(preReportData + FOUND_AND_EVERYTHING_FINE);
		}
	}

	private boolean checkForMatchCountOne(XPathAnalyzeData xPathData,
										  XPathMatcher matcher, Integer i, String preReportData, List<String> reportData,
										  List<ValidationError> validationErrorList, boolean basePathCountError, boolean fieldDataMismatch) {

		if (isFieldError(xPathData)) {
			if (!basePathCountError && !fieldDataMismatch) {
				reportData.add(preReportData + FOUND_BUT_FIELD_ERROR);
				validationErrorList.add(new ValidationError(matcher, xPathData, "1",
						MatchingErrorType.FirstFieldValueError, i));
			}
			validationErrorList
					.add(new ValidationError(matcher, xPathData, "1", MatchingErrorType.FieldValueError, i));
			return true;
		}
		return fieldDataMismatch;
	}

	private boolean isValuesBlank(XPathAnalyzeData xPathData) {
		String expVal = xPathData.getExpectedValue();
		String actualValue = xPathData.getActualValue();
		return StringUtils.isBlank(expVal) && StringUtils.isBlank(actualValue);
	}
}
