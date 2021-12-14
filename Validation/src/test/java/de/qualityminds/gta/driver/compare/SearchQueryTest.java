package de.qualityminds.gta.driver.compare;

import static org.junit.jupiter.api.Assertions.*;

import de.qualityminds.gta.driver.compare.SearchQuery;
import de.qualityminds.gta.driver.compare.SearchQuery.Operator;
import org.junit.jupiter.api.Test;

class SearchQueryTest {

	@Test
	void testOperatorEqualsStringInputNoQuotes() {
		SearchQuery searchQuery = new SearchQuery("= a");
		assertEquals("= a", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsStringInputWithDoubleQuotes() {
		SearchQuery searchQuery = new SearchQuery("= \"a\"");
		assertEquals("= \"a\"", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsStringInputWithSingleQuotes() {
		SearchQuery searchQuery = new SearchQuery("= 'a'");
		assertEquals("= 'a'", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertTrue(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsStringInputNoQuotesWildcardEnd() {
		SearchQuery searchQuery = new SearchQuery("a*");
		assertEquals("a*", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("startsWith", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.STARTSWITH, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsStringInputWithDoubleQuotesWildcardEnd() {
		SearchQuery searchQuery = new SearchQuery("= \"a*\"");
		assertEquals("= \"a*\"", searchQuery.getOriginalInput());
		assertEquals("a*", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsStringInputWithSingleQuotesWildcardEnd() {
		SearchQuery searchQuery = new SearchQuery("= 'a*'");
		assertEquals("= 'a*'", searchQuery.getOriginalInput());
		assertEquals("a*", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertTrue(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsStringInputNoQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= a");
		assertEquals("!= a", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsStringInputWithDoubleQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= \"a\"");
		assertEquals("!= \"a\"", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsStringInputWithSingleQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= 'a'");
		assertEquals("!= 'a'", searchQuery.getOriginalInput());
		assertEquals("a", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertTrue(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}


	//region Tests with digits
	//=============================================================================
	@Test
	void testOperatorEqualsDigitInputNoQuotes() {
		SearchQuery searchQuery = new SearchQuery("= 1");
		assertEquals("= 1", searchQuery.getOriginalInput());
		assertEquals("1", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsDigitInputWithDoubleQuotes() {
		SearchQuery searchQuery = new SearchQuery("= \"3\"");
		assertEquals("= \"3\"", searchQuery.getOriginalInput());
		assertEquals("3", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorEqualsDigitInputWithSingleQuotes() {
		SearchQuery searchQuery = new SearchQuery("= '3'");
		assertEquals("= '3'", searchQuery.getOriginalInput());
		assertEquals("3", searchQuery.getValue());
		assertEquals("=", searchQuery.getOperatorString());
		assertTrue(searchQuery.isValueHadQuotes());
		assertEquals(Operator.EQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsDigitInputNoQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= 3");
		assertEquals("!= 3", searchQuery.getOriginalInput());
		assertEquals("3", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsDigitInputWithDoubleQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= \"7\"");
		assertEquals("!= \"7\"", searchQuery.getOriginalInput());
		assertEquals("7", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertFalse(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}

	@Test
	void testOperatorNotEqualsDigitInputWithSingleQuotes() {
		SearchQuery searchQuery = new SearchQuery("!= '7'");
		assertEquals("!= '7'", searchQuery.getOriginalInput());
		assertEquals("7", searchQuery.getValue());
		assertEquals("!=", searchQuery.getOperatorString());
		assertTrue(searchQuery.isValueHadQuotes());
		assertEquals(Operator.NOTEQUALS, searchQuery.getOperator());
	}
	//=============================================================================
	//endregion


}
