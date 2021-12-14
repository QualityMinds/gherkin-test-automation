package de.qualityminds.gta.driver.templating;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.qualityminds.gta.driver.templating.helper.TemplateNullHelper;

public class TemplateNullHelperTest {

	TemplateNullHelper templateNullHelper = new TemplateNullHelper();
	UserData userData = new UserData("first", "last", "mail", 5.5);
	UserData userData1 = new UserData();

	@Test
	public void isFieldAvailableTest() {
		assertTrue(templateNullHelper.isFieldAvailable(userData, "firstName"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "lastName"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "email"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "balance"));

		assertTrue(templateNullHelper.isFieldAvailable(userData, "FIRSTNAME"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "LASTNAME"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "EMAIL"));
		assertTrue(templateNullHelper.isFieldAvailable(userData, "BALANCE"));

		assertFalse(templateNullHelper.isFieldAvailable(userData, "first"));
		assertFalse(templateNullHelper.isFieldAvailable(userData, "last"));
		assertFalse(templateNullHelper.isFieldAvailable(userData, "mail"));
		assertFalse(templateNullHelper.isFieldAvailable(userData, "bal"));
	}

	@Test
	public void isNodeByNamePopulatedTest() {
		assertTrue(templateNullHelper.isNodeByNamePopulated(userData, "firstName"));
		assertTrue(templateNullHelper.isNodeByNamePopulated(userData, "FIRSTNAME"));
		assertFalse(templateNullHelper.isNodeByNamePopulated(userData, "first"));
	}

	@Test
	public void isOneOfListPopulatedTest() {
		Map<Integer, String> map = new HashMap<>();
		map.put(1, "Value1");
		map.put(2, "Value2");
		map.put(3, "Value3");

		Collection<String> collection = new ArrayList<>();
		collection.add("ABC");
		collection.add("DEF");
		collection.add("GHI");

		Map<String, Double> mapStrDouble = new HashMap<>();
		mapStrDouble.put("balance", 1.1);

		Collection<Long> collLong = new ArrayList<>();
		collLong.add(60L);

		Map<Integer, String> mapEmpty = new HashMap<>();

		Collection<String> collEmpty = new ArrayList<>();

		assertTrue(templateNullHelper.isOneOfListPopulated("Test", "ABC", 5));
		assertTrue(templateNullHelper.isOneOfListPopulated(20));
		assertTrue(templateNullHelper.isOneOfListPopulated(map));
		assertTrue(templateNullHelper.isOneOfListPopulated(collection));
		assertTrue(templateNullHelper.isOneOfListPopulated(mapStrDouble)); // StackOverFlowError
		assertTrue(templateNullHelper.isOneOfListPopulated(5.5)); // StackOverFlowError
		assertTrue(templateNullHelper.isOneOfListPopulated(userData, userData1)); // StackOverFlowError
		assertTrue(templateNullHelper.isOneOfListPopulated(collLong)); // StackOverFlowError

		assertFalse(templateNullHelper.isOneOfListPopulated(mapEmpty));
		assertFalse(templateNullHelper.isOneOfListPopulated(collEmpty));
		assertFalse(templateNullHelper.isOneOfListPopulated(userData1)); // StackOverFlowError
	}

}