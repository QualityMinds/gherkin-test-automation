package de.qualityminds.gta.driver.compare;

import de.qualityminds.gta.driver.exceptions.ValidationException;

public class Comparator {


	/**
	 * Comparison is case sensitive
	 *
	 * @param actualValue
	 * @param condition   also known as expected value plus optional preceding operator
	 */
	public static ComparisonError compare(String actualValue, String condition) throws ValidationException {
		return compare(actualValue, condition, true);
	}

	public static ComparisonError compare(String actualValue, String expectedWithOperator, boolean caseSensitive) throws ValidationException {
		SearchQuery searchQuery = new SearchQuery(expectedWithOperator);
		String actualTrimmed = (actualValue != null) ? actualValue.trim() : null;
		return executeFullValueCompare(searchQuery, actualTrimmed, caseSensitive);
	}

	private static ComparisonError executeFullValueCompare(SearchQuery sq, String actualValue, boolean caseSensitive) throws ValidationException {
		SearchQuery.Operator operator = sq.getOperator();
		String rawExpected = sq.getOriginalInput();
		String expectedValue = sq.getValue();

		double actualValueNum;
		double expectedValueNum;
		switch (operator) {
			case EQUALS:
			case UNKNOWN:
				if (!equals(actualValue, expectedValue, !sq.isValueHadQuotes(), caseSensitive)) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case NOTEQUALS:
				if (equals(actualValue, expectedValue, !sq.isValueHadQuotes(), caseSensitive)) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case MORETHAN:
				actualValueNum = Double.parseDouble(actualValue);
				expectedValueNum = Double.parseDouble(expectedValue);
				if (actualValueNum <= expectedValueNum) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case MOREOREQUAL:
				actualValueNum = Double.parseDouble(actualValue);
				expectedValueNum = Double.parseDouble(expectedValue);
				if (actualValueNum < expectedValueNum) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case LESSTHAN:
				actualValueNum = Double.parseDouble(actualValue);
				expectedValueNum = Double.parseDouble(expectedValue);
				if (actualValueNum >= expectedValueNum) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case LESSOREQUAL:
				actualValueNum = Double.parseDouble(actualValue);
				expectedValueNum = Double.parseDouble(expectedValue);
				if (actualValueNum > expectedValueNum) {
					return new ComparisonError(actualValue, rawExpected);
				}
				break;
			case ENDSWITH:
				if (actualValue == null || !actualValue.endsWith(expectedValue)) {
					return new ComparisonError(actualValue, "*" + expectedValue);
				}
				break;
			case STARTSWITH:
				if (actualValue == null || !actualValue.startsWith(expectedValue)) {
					return new ComparisonError(actualValue, expectedValue + "*");
				}
				break;
			case CONTAINS:
				if (actualValue == null || !actualValue.contains(expectedValue)) {
					return new ComparisonError(actualValue, "*" + expectedValue + "*");
				}
				break;
			default:
				throw new ValidationException("Es ist ein Fehler beim Vergleich aufgetreten: \"" + sq.getOriginalInput() + "\"");
		}
		return null;
	}

	private static boolean equals(String a, String b, boolean tryToParseNumericValues, boolean caseSensitive) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		if (b == null) return false;


		if (tryToParseNumericValues) {
			try {
				double aDouble = Double.parseDouble(a);
				double bDouble = Double.parseDouble(b);
				return aDouble == bDouble;
			} catch (NumberFormatException ignored) {
				//no quotes used since we were allowed to parse, so we can also just do a string compare
			}
		}

		if (caseSensitive) {
			return a.equals(b);
		} else {
			return a.equalsIgnoreCase(b);
		}
	}
}
