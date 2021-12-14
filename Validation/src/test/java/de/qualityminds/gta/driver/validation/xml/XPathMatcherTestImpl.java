package de.qualityminds.gta.driver.validation.xml;

import de.qualityminds.gta.driver.validation.xml.beans.MatchValueAtXPath;
import de.qualityminds.gta.driver.validation.xml.beans.XPathMatcher;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XPathMatcherTestImpl implements XPathMatcher {
	@MatchValueAtXPath(value = "test:MsgId", id = true)
	private String msgId;

	@Override
	public String getName() {
		return "XPathMatcherTestName";
	}

	@Override
	public String getBaseXPath() {
		return "XPathMatcherTestXpath";
	}

	@Override
	public Map<String, String> getNamespaces() {
		return null;
	}

}
