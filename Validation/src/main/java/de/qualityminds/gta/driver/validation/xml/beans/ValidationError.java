package de.qualityminds.gta.driver.validation.xml.beans;

import static de.qualityminds.gta.driver.validation.xml.beans.MatchValueAtXPath.VAR_NO_NAMESPACE;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidationError {

	public static final String FOUND_BUT_FIELD_ERROR = "Hauptpfad 1x gefunden, aber Feldabweichung";
	public static final String FOUND_BUT_MORE_THAN_ONCE = "Hauptpfad wurde %dx gefunden - erwartet: 1x.";
	public static final String FOUND_AND_EVERYTHING_FINE = "Hauptpfad 1x vorhanden und alle Felder wie erwartet belegt.";

	private static final String LogPrefixXpath = "\t\\--> XPath: ";
	public XPathMatcher matcher;
	public XPathAnalyzeData data;
	public String expectedMatchCount;
	public MatchingErrorType errorType;
	public Integer matcherPos;

	@Override
	public String toString() {
		String preValidationError = "\nAbweichung in Information #" + (matcherPos + 1) + " (" + matcher.getName()
				+ "): ";
		StringBuilder sb = new StringBuilder();
		switch (errorType) {
			case MatchCountError:
				sb.append(preValidationError).append(data.getActualMatchCount()).append("x gefunden, aber ")
						.append(expectedMatchCount).append("x erwartet.").append("\n");
				sb.append("Details:").append("\n");
				sb.append(LogPrefixXpath).append(removeEmptyNamespace(data.getXPath())).append("\n");
				sb.append("Informationsobjekt: ").append(matcher.toString());
				break;
			case FirstFieldValueError:
				sb.append(preValidationError).append(FOUND_BUT_FIELD_ERROR);
				break;
			case FieldValueError:
				//TODO improve displaying not existant values, instead of printing "null"
				sb.append("Feld ").append(data.getFieldName()).append(": erwartet \"").append(data.getExpectedValue())
						.append("\", aber war \"").append(data.getActualValue())
						.append("\".").append("\n");
				sb.append(LogPrefixXpath).append(removeEmptyNamespace(data.getXPath()));
				break;
			case PathNotFoundError:
				sb.append(preValidationError).append("Eintrag nicht gefunden.").append("\n");
				sb.append(LogPrefixXpath).append(removeEmptyNamespace(data.getXPath()));
				break;
			default:
				break;

		}
		return sb.toString();
	}

	private String removeEmptyNamespace(String s) {
		if (s == null) {
			return "";
		} else {
			return s.replace("/" + VAR_NO_NAMESPACE + ":", "/").replace("[" + VAR_NO_NAMESPACE + ":", "[");
		}
	}
}