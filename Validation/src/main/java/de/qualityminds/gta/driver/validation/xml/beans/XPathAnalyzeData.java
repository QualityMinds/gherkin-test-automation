package de.qualityminds.gta.driver.validation.xml.beans;

import java.io.Serializable;

import jlibs.xml.sax.dog.expr.Expression;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class XPathAnalyzeData implements Serializable {
	private String XPath;
	private String fieldName;
	private String expectedValue;
	private Expression expression;
	private Integer actualMatchCount;
	private String actualValue;
}