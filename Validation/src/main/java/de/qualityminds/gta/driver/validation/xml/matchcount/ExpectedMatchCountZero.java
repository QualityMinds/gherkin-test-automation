package de.qualityminds.gta.driver.validation.xml.matchcount;

import java.util.ArrayList;
import java.util.List;

import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil;
import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.validation.xml.beans.MatchingErrorType;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationError;
import de.qualityminds.gta.driver.validation.xml.beans.XPathAnalyzeData;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;

public class ExpectedMatchCountZero extends ExpectedMatchCount {

	public ExpectedMatchCountZero() {
		super("0");
	}

	@Override
	public void validate(XPathAnalyzeData data, List<XPathAnalyzeData> xPathAnalyzeData, XPathMatcher matcher, int i,
						 String preReportData, List<String> reportData, List<ValidationError> validationErrorList) {
		Integer actualCount = data.getActualMatchCount();
		// TODO shouldn't cases actualCount == 0 excluded?
		reportData.add(preReportData + actualCount + "x gefunden - erwartet: 0x.");
		if (actualCount != 0) {
			validationErrorList.add(new ValidationError(matcher, data, "0", MatchingErrorType.MatchCountError, i));
		}
	}

	@Override
	public List<XPathAnalyzeData> getXpathAnalyzeDataList(String baseXPath, List<XPathField> fields) {
		List<XPathAnalyzeData> xpaths = new ArrayList<>();
		xpaths.add(new XPathAnalyzeData().setXPath(baseXPath + XMLParsingUtil.combinedFieldMatcher(fields)));
		return xpaths;
	}
}
