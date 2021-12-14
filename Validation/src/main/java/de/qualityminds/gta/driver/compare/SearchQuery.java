package de.qualityminds.gta.driver.compare;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

@Getter
public class SearchQuery {

	private static final Pattern COMPARE_PATTERN = Pattern.compile("((=|<>|!=)\\s*(.+))|((=|>|<|<=|>=|<>|!=)\\s*(['\"]?-?[\\d.]+['\"]?))");
	private static final Pattern CONTAINS_PATTERN = Pattern.compile("([\\*%])(.+)([\\*%])|([\\*%])(.+)|(.+)([\\*%])");

	private static final String startsWith = "startsWith";
	private static final String endsWith = "endsWith";
	private static final String doesContain = "doesContain";

	private final Operator operator;
	private final String operatorString;
	private final String value;
	private final String originalInput;

	private final boolean valueHadQuotes;

	public enum Operator {
		EQUALS, NOTEQUALS, MORETHAN, LESSTHAN, MOREOREQUAL, LESSOREQUAL, STARTSWITH, ENDSWITH, CONTAINS, UNKNOWN;
	}

	public SearchQuery(String expectedWithOperator) {
		this.originalInput = expectedWithOperator;

		String expectedWithOperatorTrimmed = expectedWithOperator == null ? null : expectedWithOperator.trim();

		if (expectedWithOperatorTrimmed == null) {
			this.value = null;
			this.valueHadQuotes = false;
			this.operatorString = "";
			this.operator = Operator.EQUALS;
			return;
		}

		Matcher regularCompare = COMPARE_PATTERN.matcher(expectedWithOperatorTrimmed);
		Matcher containsCompare = CONTAINS_PATTERN.matcher(expectedWithOperatorTrimmed);

		String parsedOperator;
		String parsedExpectedValue;
		if (regularCompare.matches()) {
			parsedOperator = ((regularCompare.group(2) != null) ? regularCompare.group(2) : regularCompare.group(5)).trim();
			parsedExpectedValue = ((regularCompare.group(3) != null) ? regularCompare.group(3) : regularCompare.group(6)).trim();

		} else if (containsCompare.matches()) {
			String pre = containsCompare.group(1) != null ? containsCompare.group(1) : containsCompare.group(4);
			String post = containsCompare.group(3) != null ? containsCompare.group(3) : containsCompare.group(7);

			parsedExpectedValue = containsCompare.group(2) != null ? containsCompare.group(2) : (containsCompare.group(5) != null ? containsCompare.group(5) : containsCompare.group(6));
			parsedOperator = wildcardPosToOperator(pre, post);

		} else {
			parsedOperator = "=";
			parsedExpectedValue = expectedWithOperatorTrimmed;
		}

		this.operatorString = parsedOperator;
		this.valueHadQuotes = hasQuotes(parsedExpectedValue, "'");

		if ("null".equals(parsedExpectedValue)) {
			this.value = null;
		} else {
			this.value = trimAndRemoveQuotes(parsedExpectedValue, valueHadQuotes);
		}


		this.operator = opStringToOpEnum(parsedOperator);
	}

	private static Operator opStringToOpEnum(String parsedOperator) {
		switch (parsedOperator) {
			case "=":
				return Operator.EQUALS;
			case "!=":
			case "<>":
				return Operator.NOTEQUALS;
			case ">":
				return Operator.MORETHAN;
			case ">=":
				return Operator.MOREOREQUAL;
			case "<":
				return Operator.LESSTHAN;
			case "<=":
				return Operator.LESSOREQUAL;
			case endsWith:
				return Operator.ENDSWITH;
			case startsWith:
				return Operator.STARTSWITH;
			case doesContain:
				return Operator.CONTAINS;
			default:
				return Operator.UNKNOWN;
		}
	}

	private static String wildcardPosToOperator(String pre, String post) {
		if (pre != null && post != null) {
			return doesContain;
		} else if (pre != null) {
			return endsWith;
		} else {
			return startsWith;
		}
	}

	private static String trimAndRemoveQuotes(String inp, boolean valueHadQuotes) {
		if (inp == null) {
			return null;
		}
		if (valueHadQuotes || hasQuotes(inp, "\"")) {
			String trimmed = inp.trim();
			trimmed = trimmed.substring(1, trimmed.length() - 1);
			return trimmed;
		}
		return inp.trim();
	}

	private static boolean hasQuotes(String input, String quoteSign) {
		if (input == null) return false;

		String inp = input.trim();

		return (inp.startsWith(quoteSign) && inp.endsWith(quoteSign));
	}
}
