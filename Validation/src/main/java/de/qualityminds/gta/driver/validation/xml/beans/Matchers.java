package de.qualityminds.gta.driver.validation.xml.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.qualityminds.gta.driver.validation.xml.matchcount.ExpectedMatchCount;

import lombok.Data;


@Data
public class Matchers implements Serializable {
	private List<XPathMatcher> matchers = new ArrayList<>();
	private List<ExpectedMatchCount> matchExpectation = new ArrayList<>();
	private List<List<XPathAnalyzeData>> xPathAnalyzeData = new ArrayList<>();
	private Map<Class<? extends XPathMatcher>, XPathAnalyzeData> xPathClassCountAnalyzeData = new HashMap<>();
}