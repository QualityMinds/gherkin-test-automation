package de.qualityminds.gta.driver.validation.xml.matchcount;

import java.util.ArrayList;
import java.util.List;

import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil;
import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.validation.xml.beans.ValidationError;
import de.qualityminds.gta.driver.validation.xml.beans.XPathAnalyzeData;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;

public class ExpectedMatchCountExactlyOne extends ExpectedMatchCount {

	public ExpectedMatchCountExactlyOne() {
		super("1");
	}

	@Override
	public void validate(XPathAnalyzeData data, List<XPathAnalyzeData> xPathAnalyzeData, XPathMatcher matcher,
						 int i, String preReportData, List<String> reportData,
						 List<ValidationError> validationErrorList) {

		validateBasePathAndFields(data, xPathAnalyzeData, matcher, i, preReportData, reportData, validationErrorList);

	}

	@Override
	public List<XPathAnalyzeData> getXpathAnalyzeDataList(String baseXPath, List<XPathField> fields) {
		return new ArrayList<>(XMLParsingUtil.makeXPathToCheckFieldContent(baseXPath, new ArrayList<>(), fields));
	}

}
