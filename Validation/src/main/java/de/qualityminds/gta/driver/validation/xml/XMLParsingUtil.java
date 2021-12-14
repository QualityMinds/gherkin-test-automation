package de.qualityminds.gta.driver.validation.xml;

import static org.junit.Assert.assertEquals;

import javax.xml.xpath.XPathException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;

import de.qualityminds.gta.driver.utils.reflection.ObjectInspectionUtil;
import de.qualityminds.gta.driver.validation.xml.beans.*;
import de.qualityminds.gta.driver.compare.SearchQuery;
import de.qualityminds.gta.driver.exceptions.XMLException;
import de.qualityminds.gta.driver.validation.xml.matchcount.ExpectedMatchCount;

@Component
public class XMLParsingUtil implements Serializable {
	public void parseXMLforMatchers(InputSource inputSource, Matchers matchers) throws XMLException {
		try {
			// init XMLDog with Namespaces
			XMLDog charlie = new XMLDog(makeDefaultNamespaceContext(matchers.getMatchers()));

			for (int i = 0; i < matchers.getMatchers().size(); i++) {
				XPathMatcher matcher = matchers.getMatchers().get(i);


				// *************************
				// * init XPathAnalyzeData *
				// *************************
				// specific XPaths for validation
				matchers.getXPathAnalyzeData().add(prepareXPathAnalyzeData(matcher, matchers.getMatchExpectation().get(i)));


				// XPaths to count objects of checked classes
				matchers.getXPathClassCountAnalyzeData().put(matcher.getClass(), new XPathAnalyzeData().setXPath(matcher.getBaseXPath()));


				// ***********************
				// * feed charlie xPaths *
				// ***********************
				// specific XPaths for validation
				for (XPathAnalyzeData xPathAnalyzeData : matchers.getXPathAnalyzeData().get(i)) {
					xPathAnalyzeData.setExpression(charlie.addXPath(xPathAnalyzeData.getXPath()));
				}


				// XPaths to count objects of checked classes
				XPathAnalyzeData xPathAnalyzeData = matchers.getXPathClassCountAnalyzeData().get(matcher.getClass());
				xPathAnalyzeData.setExpression(charlie.addXPath(xPathAnalyzeData.getXPath()));
			}

			// let charlie sniff for xPaths
			XPathResults bittenLegs = charlie.sniff(inputSource);

			// write results back to XPathAnalyzeData
			for (int i = 0; i < matchers.getMatchers().size(); i++) {
				for (XPathAnalyzeData xPathAnalyzeData : matchers.getXPathAnalyzeData().get(i)) {
					Object result = bittenLegs.getResult(xPathAnalyzeData.getExpression());
					xPathAnalyzeData.setActualMatchCount(calculateMatchCount(result));
					if (xPathAnalyzeData.getExpectedValue() != null) {
						xPathAnalyzeData.setActualValue(getActualValue(result));
					}
				}
				for (XPathAnalyzeData xPathAnalyzeData : matchers.getXPathClassCountAnalyzeData().values()) {
					Object result = bittenLegs.getResult(xPathAnalyzeData.getExpression());
					xPathAnalyzeData.setActualMatchCount(calculateMatchCount(result));
				}
			}
		} catch (SAXPathException | IllegalArgumentException | IllegalAccessException e) {
			throw new XMLException("Fehler beim Erstellen der zu suchenden XML Pfade", e);
		} catch (XPathException e) {
			throw new XMLException("Interner Fehler der Bibliothek zum durchsuchen von XML-Dateien.", e);
		}
	}

	private static String getActualValue(Object result) {
		if (result instanceof List) {
			List<?> resultList = ((List<?>) result);
			if (!resultList.isEmpty()) {
				return ((NodeItem) resultList.get(0)).value;
			} else {
				return null;
			}
		} else {
			return result.toString();
		}
	}

	private static List<XPathAnalyzeData> prepareXPathAnalyzeData(XPathMatcher matcher, ExpectedMatchCount expectedMatchCount)
			throws IllegalArgumentException, IllegalAccessException {
		List<XPathField> fields = extractXPathFields(matcher);
		String baseXPath = matcher.getBaseXPath();

		return new ArrayList<>(expectedMatchCount.getXpathAnalyzeDataList(baseXPath, fields));
	}

	public static List<XPathAnalyzeData> makeXPathToCheckFieldContent(String basePath, List<XPathField> idFields, List<XPathField> checkValueFields) {
		return checkValueFields.stream().map(
				field -> {
					String fieldXpath = field.getXPath();
					String xpath = basePath + combinedFieldMatcher(idFields) + "/" + fieldXpath;

					if (!fieldXpath.contains("@")) { // @ is used for xpath attributes, not nodes. Only nodes require text()
						xpath += "/text()";
					}

					XPathAnalyzeData analyzeData = new XPathAnalyzeData();
					analyzeData.setXPath(xpath)
							.setExpectedValue(field.getFieldValue())
							.setFieldName(field.getJsonPath());
					return analyzeData;
				}
		).collect(Collectors.toList());
	}

	private static Integer calculateMatchCount(Object result) {
		if (result instanceof Boolean) {
			return ((boolean) result ? 1 : 0);
		}
		return ((List<?>) result).size();
	}

	public static List<XPathField> extractNonIdFields(List<XPathField> fields) {
		return fields.stream().filter(field -> !field.getId()).collect(Collectors.toList());
	}

	public static List<XPathField> extractIdFields(XPathMatcher matcher) throws IllegalArgumentException, IllegalAccessException {
		return extractIdFields(extractXPathFields(matcher));
	}

	public static List<XPathField> extractIdFields(List<XPathField> fields) {
		return fields.stream().filter(XPathField::getId).collect(Collectors.toList());
	}

	public static List<XPathField> extractXPathFields(XPathMatcher matcher) throws IllegalArgumentException, IllegalAccessException {
		return extractXPathFields(matcher, new ArrayList<>(), "", "");
	}

	private static List<XPathField> extractXPathFields(Object target, List<XPathField> xPathFields, String preXPath, String preJsonPath)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = ObjectInspectionUtil.getAllFields(target);
		for (Field field : fields) {
			addSubPath(target, xPathFields, field, preXPath, preJsonPath);
		}
		return xPathFields;
	}

	public static String combinedFieldMatcher(List<XPathField> fields) {
		StringBuilder path = new StringBuilder();
		if (!fields.isEmpty()) {
			boolean isFirst = true;

			for (XPathField field : fields) {
				String matcher = handleXpathFunction(field.getXPath(), field.getFieldValue());
				path.append(isFirst ? ("[" + matcher) : (" and " + matcher));
				isFirst = false;
			}

			path.append("]");
		}
		return path.toString();
	}

	private static String handleXpathFunction(String xPath, String fieldValue) {
		SearchQuery query = new SearchQuery(fieldValue);
		String value = query.getValue();
		switch (query.getOperator()) {
			case NOTEQUALS:
				if (value == null) {
					return "count(" + xPath + ")!=0";
				}
				if (!query.isValueHadQuotes() && value.length() < 16) {
					try {
						Double.parseDouble(value);
						return xPath + "!=number('" + value + "')";
					} catch (NumberFormatException ignored) {
						//Just checking as string then
					}
				}
				//TODO if we find a way to handle optional elements then change here! currently it fails if we search for a non-existant field being != <something> which should be true
				return xPath + " != '" + value + "'";
			case MORETHAN:
				return xPath + " > " + value;
			case LESSTHAN:
				return xPath + " < " + value;
			case MOREOREQUAL:
				return xPath + " >= " + value;
			case LESSOREQUAL:
				return xPath + " <= " + value;
			case STARTSWITH:
				return "starts-with(" + xPath + ", '" + value + "')";
			case ENDSWITH:
				return "ends-with(" + xPath + ", '" + value + "')";
			case CONTAINS:
				return "contains(" + xPath + ", '" + value + "')";

			case UNKNOWN:
			case EQUALS:
			default:
				if (value == null) {
					return "count(" + xPath + ")=0";
				}
				if (!query.isValueHadQuotes() && value.length() < 16) {
					try {
						Double.parseDouble(value);
						return xPath + "=number('" + value + "')";
					} catch (NumberFormatException ignored) {
						//Just checking as string then
					}
				}
				return xPath + "='" + value + "'";
		}
	}

	private static void addSubPath(Object target, List<XPathField> xPathFields, Field field, String preXPath, String preJsonPath) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		if (!field.isAnnotationPresent(MatchValueAtXPath.class)) {
			return;
		}

		Class<?> fieldType = field.getType();
		MatchValueAtXPath xPath = field.getAnnotation(MatchValueAtXPath.class);
		String xPathVal = replaceXpathNsPlaceholderWithParentNs(xPath.value(), preXPath);
		if (fieldType.equals(String.class)) {
			String value = (String) field.get(target);
			if (value != null) {
				XPathField newField = new XPathField().setJsonPath(preJsonPath + field.getName()).setXPath(preXPath + xPathVal).setFieldValue(value).setId(xPath.id());
				xPathFields.add(newField);
			}
		} else {
			Object newTarget = field.get(target);
			if (newTarget != null) {
				extractXPathFields(newTarget, xPathFields, preXPath + xPathVal + "/", preJsonPath + field.getName() + ".");
			}
		}
	}

	public static String replaceXpathNsPlaceholderWithParentNs(String xPathInput, String preXPath) {
		if (!xPathInput.contains(MatchValueAtXPath.VAR_PARENT_NAMESPACE) || StringUtils.isBlank(preXPath)) {
			return xPathInput;
		}

		String[] allParentsXpaths = preXPath.split("/");
		String parentXpath = allParentsXpaths[allParentsXpaths.length - 1];

		int firstColon = parentXpath.indexOf(":");
		if (firstColon > 0) {
			return xPathInput.replaceAll(MatchValueAtXPath.VAR_PARENT_NAMESPACE, parentXpath.substring(0, firstColon));
		}
		return xPathInput.replaceAll(MatchValueAtXPath.VAR_PARENT_NAMESPACE + ":", "");
	}

	@Accessors(chain = true)
	@Data
	public static class XPathField {
		String jsonPath = "";
		String XPath;
		String fieldValue;
		Boolean id = false;
	}

	private static DefaultNamespaceContext makeDefaultNamespaceContext(List<XPathMatcher> matchers) {
		DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
		Map<String, String> namespacePrefixes = new HashMap<>();
		for (XPathMatcher matcher : matchers) {
			for (Entry<String, String> namespace : matcher.getNamespaces().entrySet()) {
				if (namespacePrefixes.containsKey(namespace.getKey())) {
					//TODO do not use assert
					assertEquals("Namespace definition inconsistent between matchers!", namespacePrefixes.get(namespace.getKey()), namespace.getValue());
				} else {
					namespacePrefixes.put(namespace.getKey(), namespace.getValue());
				}
			}
		}
		namespacePrefixes.forEach(nsContext::declarePrefix);
		return nsContext;
	}

}
