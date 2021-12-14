package de.qualityminds.gta.driver.validation.xml;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.thucydides.core.annotations.Step;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.qualityminds.gta.driver.exceptions.ValidationException;
import de.qualityminds.gta.driver.validation.xml.beans.*;
import de.qualityminds.gta.driver.validation.xml.matchcount.ExpectedMatchCount;

@Component
public class XPathValidationUtil {

	private static final Logger logger = LoggerFactory.getLogger(XPathValidationUtil.class);

	public static final String XML_STRUCTURES_HEADER = "In XML-Datei beobachtete Strukturtypen:";

	@Step("validiere die in der XML-Datei enthaltenen Informationen")
	public ValidationReport validateXMLParseResult(Matchers matchers)
			throws IllegalArgumentException, ValidationException {
		List<String> reportData = new ArrayList<>();
		List<ValidationError> validationErrorList = new ArrayList<>();

		reportData.add(XML_STRUCTURES_HEADER);
		for (Map.Entry<Class<? extends XPathMatcher>, XPathAnalyzeData> entry : matchers.getXPathClassCountAnalyzeData().entrySet()) {
			Class<? extends XPathMatcher> key = entry.getKey();
			XPathAnalyzeData value = entry.getValue();
			try {
				//TODO kind of stupid to create a new Instance only for the name :( think of a better way
				reportData.add("\t" + key.newInstance().getName() + ": " + value.getActualMatchCount());
			} catch (Exception e) {
				logger.warn("Unable to record information about {}: {}", key.getSimpleName(), e.getMessage());
			}
		}
		reportData.add("");

		int nrOfMatchers = matchers.getMatchers().size();
		for (int i = 0; i < nrOfMatchers; i++) {
			XPathMatcher matcher = matchers.getMatchers().get(i);
			ExpectedMatchCount expectedMatchCount = matchers.getMatchExpectation().get(i);
			List<XPathAnalyzeData> xPathAnalyzeData = matchers.getXPathAnalyzeData().get(i);
			XPathAnalyzeData matcherClassCountData = matchers.getXPathClassCountAnalyzeData().get(matcher.getClass());

			String preReportData = "Information #" + (i + 1) + " (" + matcher.getName() + "):\n\t";

			expectedMatchCount.validate(matcherClassCountData, xPathAnalyzeData, matcher, i,
					preReportData, reportData, validationErrorList);
		}

		return new ValidationReport().setReport(reportData).setErrorOccurred(!validationErrorList.isEmpty())
				.setValidationErrors(validationErrorList);
	}

}
