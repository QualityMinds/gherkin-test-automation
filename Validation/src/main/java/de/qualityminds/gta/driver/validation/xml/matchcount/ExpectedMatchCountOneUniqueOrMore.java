package de.qualityminds.gta.driver.validation.xml.matchcount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil;
import de.qualityminds.gta.driver.validation.xml.XMLParsingUtil.XPathField;
import de.qualityminds.gta.driver.exceptions.ValidationException;
import de.qualityminds.gta.driver.validation.xml.beans.*;

public class ExpectedMatchCountOneUniqueOrMore extends ExpectedMatchCount implements Serializable {


	public ExpectedMatchCountOneUniqueOrMore() {
		super("1+");
	}

	@Override
	public void validate(XPathAnalyzeData data, List<XPathAnalyzeData> xPathAnalyzeData, XPathMatcher matcher, int i,
						 String preReportData, List<String> reportData, List<ValidationError> validationErrorList)
			throws ValidationException {

		try {
			List<XPathField> idFields = XMLParsingUtil.extractIdFields(matcher);
			if (idFields.isEmpty()) {
				XPathAnalyzeData basePathWithoutIdData = xPathAnalyzeData.get(0);
				Integer matchCount = basePathWithoutIdData.getActualMatchCount();
				if (matchCount < 1) {
					reportData.add(preReportData + "Eintrag nicht gefunden.");
					validationErrorList.add(new ValidationError(matcher, basePathWithoutIdData, "1(+)",
							MatchingErrorType.PathNotFoundError, i));

				} else {
					reportData.add(preReportData + "Eintrag wie gesucht " + matchCount + "x gefunden.");
				}
			} else {
				validateBasePathAndFields(xPathAnalyzeData.get(0), xPathAnalyzeData.subList(1, xPathAnalyzeData.size()),
						matcher, i, preReportData, reportData, validationErrorList);
			}
		} catch (IllegalArgumentException | IllegalAccessException | IndexOutOfBoundsException | NullPointerException e) {
			throw new ValidationException();
		}
	}

	@Override
	public List<XPathAnalyzeData> getXpathAnalyzeDataList(String baseXPath, List<XPathField> fields) {
		List<XPathAnalyzeData> xpathsData = new ArrayList<>();
		List<XPathField> idFields = XMLParsingUtil.extractIdFields(fields);
		if (!idFields.isEmpty()) {
			List<XPathField> nonIdFields = XMLParsingUtil.extractNonIdFields(fields);
			// to find element with correct id
			xpathsData.add(new XPathAnalyzeData().setXPath(baseXPath + XMLParsingUtil.combinedFieldMatcher(idFields)));
			// to find each field with element with correct id
			xpathsData.addAll(XMLParsingUtil.makeXPathToCheckFieldContent(baseXPath, idFields, nonIdFields));
		} else {
			xpathsData.add(new XPathAnalyzeData().setXPath(baseXPath + XMLParsingUtil.combinedFieldMatcher(fields)));
		}
		return xpathsData;
	}

}
