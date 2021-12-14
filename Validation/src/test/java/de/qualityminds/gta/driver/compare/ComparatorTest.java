package de.qualityminds.gta.driver.compare;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.qualityminds.gta.driver.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class ComparatorTest {

	@Test
	void compareEqualsPositive() throws ValidationException {
		assertNull(Comparator.compare("123", "123"));
		assertNull(Comparator.compare("ABC", "ABC"));

		assertNull(Comparator.compare("ABC", "=ABC"));
		assertNull(Comparator.compare("ABC", "= ABC"));
		assertNull(Comparator.compare("ABC", " = ABC "));
		assertNull(Comparator.compare("ABC", "='ABC'"));
		assertNull(Comparator.compare("ABC", " = 'ABC'"));
		assertNull(Comparator.compare("ABC", " = 'ABC' "));

		assertNull(Comparator.compare(" ABC ", "ABC"));
		assertNull(Comparator.compare(" ABC ", "'ABC'"));
		assertNull(Comparator.compare(" ABC ", " 'ABC' "));
	}

	@Test
	void compareEqualsWithQuotesAround() throws ValidationException {
		assertNull(Comparator.compare("ABC", "'ABC'"));
		assertNull(Comparator.compare("ABC", " 'ABC' "));
		assertNull(Comparator.compare("ABC", " ABC "));
		assertNotNull(Comparator.compare("ABC", "' ABC '"));
		assertNotNull(Comparator.compare("ABC", " ' ABC ' "));

		assertNull(Comparator.compare("ABC", "\"ABC\""));
		assertNull(Comparator.compare("ABC", " \"ABC\" "));
		assertNotNull(Comparator.compare("ABC", "\" ABC \""));
		assertNotNull(Comparator.compare("ABC", " \" ABC \" "));
	}

	@Test
	void compareEqualsWithNullAndBlanks() throws ValidationException {
		assertNull(Comparator.compare("", ""));
		assertNull(Comparator.compare("", " "));
		assertNull(Comparator.compare(" ", ""));
		assertNull(Comparator.compare(" ", " "));

		assertNotNull(Comparator.compare("", "="));
		assertNotNull(Comparator.compare("", "= "));
		assertNotNull(Comparator.compare("", " ="));
		assertNotNull(Comparator.compare("", " = "));

		assertNotNull(Comparator.compare(" ", "="));
		assertNotNull(Comparator.compare(" ", "="));
		assertNotNull(Comparator.compare(" ", " ="));
		assertNotNull(Comparator.compare(" ", " = "));
	}


	@Test
	void compareEqualsNegative() throws ValidationException {
		assertNotNull(Comparator.compare("ABC", "CBA"));
		assertNotNull(Comparator.compare("ABC", "ABCBA"));
		assertNotNull(Comparator.compare("ABC", "'CBA'"));
    assertNotNull(Comparator.compare("ABC", "=\" ABC \""));
		assertNotNull(Comparator.compare("ABC", "= \" ABC \" "));
	}

  @Test
  void compareLongNumbers() throws ValidationException {
    assertNull(Comparator.compare("12345678987654321", "12345678987654321"));
    assertNull(Comparator.compare("12345678987654321", "0012345678987654321"));
    assertNull(Comparator.compare("12345678987654321", " '12345678987654321' "));
    assertNull(Comparator.compare("12345678987654321", "=  12345678987654321  "));
    assertNotNull(Comparator.compare("12345678987654321", " > 12345678987654321"));
    assertNotNull(Comparator.compare("12345678987654321", " < 12345678987654321"));
    assertNotNull(Comparator.compare("12345678987654321", " != 12345678987654321"));
  }

  @Test
  void compareSpecialChars() throws ValidationException {
    assertNull(Comparator.compare("äü", "<> üä"));
    assertNull(Comparator.compare("àáâãäåçèéêëìíîðñòôõöö", "àáâãäåçèéêëìíîðñòôõöö"));
    assertNull(Comparator.compare("‘`|/\",;:&<>^*?", "!= ‘`/\",;:&<>^*?"));
    assertNull(Comparator.compare("‘`|/\",;:&<>^*?", "= ‘`|/\",;:&<>^*?"));
    assertNull(Comparator.compare("部首", "部首"));
    assertNull(Comparator.compare("部首", "!=首部"));
  }

  @Test
  void compareUnEqualsPositiv() throws ValidationException {
    assertNull(Comparator.compare("ABC", "!=CBA"));
    assertNull(Comparator.compare("ABC", "<>ABCBA"));
    assertNull(Comparator.compare("ABC", "!='CBA'"));
    assertNull(Comparator.compare("ABC", "!= 'CDE'"));

    assertNull(Comparator.compare("ABC", "<>' CDE '")); // is notNull but should be Null!
    assertNull(Comparator.compare("ABC", "<> \" ABC \"")); // is notNull but should be Null!
    assertNull(Comparator.compare("ABC", "!= \" ABC \" ")); // is notNull but should be Null!
  }

  @Test
  void compareUnEqualsNegativ() throws ValidationException {
    assertNotNull(Comparator.compare("123", "!=123"));
    assertNotNull(Comparator.compare("123", "<>00123"));
    assertNotNull(Comparator.compare("321", "!='321'"));
    assertNotNull(Comparator.compare("ABC", "<>\"ABC\""));
    assertNotNull(Comparator.compare("ABC", "!= \"ABC\" "));
    assertNotNull(Comparator.compare("ABC", "\" ABC \""));
  }

  @Test
  void compareMoreLessThen() throws ValidationException {
    assertNull(Comparator.compare("5", "<6"));
    assertNull(Comparator.compare("8", ">6"));
    assertNull(Comparator.compare("8", ">06"));
    assertNull(Comparator.compare("5", "<=6"));
    assertNull(Comparator.compare("8", ">=6"));
    assertNotNull(Comparator.compare("1.5", "<1.4"));
    assertNotNull(Comparator.compare("05", "<004"));
  }

	@Test
	void compareEqualsCaseInsensitive() throws ValidationException {
		assertNull(Comparator.compare("AKTIV", "aktiv", false));
		assertNull(Comparator.compare("AKTIV", "AKTIV", false));
		assertNull(Comparator.compare("aktiv", "AKTIV", false));
		assertNotNull(Comparator.compare("AKTIV", "aktiv", true));
    assertNotNull(Comparator.compare("AKTIV", "inaktiv", true));
    assertNotNull(Comparator.compare("AKTIV", "inaktiv", false));
    assertNotNull(Comparator.compare("AKTIV", "INAKTIV", true));
    assertNotNull(Comparator.compare("aktiv", "inaktiv", false));
	}
}
