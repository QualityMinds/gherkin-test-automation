package de.qualityminds.gta.driver.validation.xml.beans;

import java.io.Serializable;
import java.util.Map;

public interface XPathMatcher extends Serializable {
	String getName();
    String getBaseXPath();
    Map<String, String> getNamespaces();
}
