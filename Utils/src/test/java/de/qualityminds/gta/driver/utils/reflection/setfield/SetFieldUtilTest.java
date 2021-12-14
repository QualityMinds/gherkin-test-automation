package de.qualityminds.gta.driver.utils.reflection.setfield;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.qualityminds.gta.config.GherkinProperties;
import de.qualityminds.gta.config.SpringConfig;
import de.qualityminds.gta.driver.utils.exceptions.ResolveException;
import de.qualityminds.gta.driver.utils.exceptions.TemplateDataMappingException;
import de.qualityminds.gta.driver.utils.reflection.expressions.ExpressionUtil;
import de.qualityminds.gta.driver.utils.reflection.expressions.GherkinMethod;
import de.qualityminds.gta.driver.utils.reflection.setfield.SetFieldUtil.SetFieldResult;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.FieldAliases;
import net.serenitybdd.core.Serenity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@SpringBootTest(classes = {GherkinProperties.class, SpringConfig.class})
class SetFieldUtilTest {
	@Autowired
	SetFieldUtil setFieldUtil;

	@Autowired
	ExpressionUtil expressionUtil;

	@AfterEach
	void tearDown() {
		Serenity.clearCurrentSession();
	}

	@Test
	void testSetFieldDefaultWithNullValue() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		setFieldUtil.setField(d, "value", null);
		assertNull(d.value);
	}

	@Test
	void testSetFieldStringFieldWithIntegerTypeValue() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		int targetVal = 12345;
		setFieldUtil.setField(d, "value", targetVal);
		assertEquals(String.valueOf(targetVal), d.value);
	}


	@Test
	void testSetFieldDefaultWithWrongTypeValue() {
		Dummy d = new Dummy();
		Dummy targetVal = new Dummy();
		assertThrows(IllegalArgumentException.class, () -> setFieldUtil.setField(d, "value", targetVal));
	}

	@Test
	void testSetFieldDefaultWithWrongFieldname() {
		Dummy d = new Dummy();
		String targetVal = "12345";
		assertThrows(TemplateDataMappingException.class, () -> setFieldUtil.setField(d, "valuevalue", targetVal));
	}

	@Test
	void testSetFieldDefaultWithValidValueFromExpression()
			throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		d.value = targetVal;
		String expression = "${value}";
		expressionUtil.setParameter("value", "value");
		setFieldUtil.setField(d, "childVal.value", expression);
		assertEquals(targetVal, d.getValue());
	}

	@Test
	void testSetFieldDefaultWithValidValue() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		setFieldUtil.setField(d, "value", targetVal);
		assertEquals(targetVal, d.value);
	}

	@Test
	void testSetFieldDefaultWithValidValueOnChildViaMap() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		Map<String, String> toReplace = new HashMap<>();
		toReplace.put("value", targetVal);
		setFieldUtil.setField(d, "childVal", toReplace);
		assertEquals(targetVal, d.childVal.getValue());
	}

	@Test
	void testSetFieldDefaultWithMultipleValidValuesViaMap() {
		Dummy d = new Dummy();
		String targetValOne = "12345";
		String targetValTwo = "67890";
		String nonExistingFieldName = "notExistingField";
		Map<String, String> toReplace = new HashMap<>();
		toReplace.put("value", targetValOne);
		toReplace.put("secondVal", targetValTwo);
		toReplace.put(nonExistingFieldName, "someValue");
		SetFieldResult result = setFieldUtil.setField(d, toReplace);
		assertEquals(targetValOne, d.getValue());
		assertEquals(targetValTwo, d.getSecondValue());
		Map<String, Throwable> skippedFields = result.getSkippedFields();
		assertNotNull(skippedFields);
		assertEquals(1, skippedFields.size());
		assertTrue(skippedFields.containsKey(nonExistingFieldName));
	}

	@Test
	void testSetFieldDefaultOnChildWithValidValue() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		setFieldUtil.setField(d, "childVal.value", targetVal);
		assertEquals(targetVal, d.childVal.getValue());
	}

	@Test
	void testSetFieldDefaultOnChildViaFieldAlias() throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		setFieldUtil.setField(d, "otherChildVal.wert", targetVal);
		assertEquals(targetVal, d.otherChildVal.getValue());
		assertNull(d.getValue());
		assertNull(d.childVal.getValue());
	}

	@Test
	void testSetFieldDefaultOnChildViaFieldAliasInParent()
			throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		setFieldUtil.setField(d, "kindwert", targetVal);
		assertEquals(targetVal, d.childVal.getValue());
		assertNull(d.getValue());
		assertNull(d.otherChildVal.getValue());
	}

	@Test
	void testSetFieldDefaultOnChildViaFieldAliasMapInParent()
			throws TemplateDataMappingException, ResolveException {
		Dummy d = new Dummy();
		String targetVal = "12345";
		Map<String, String> toReplace = new HashMap<>();
		toReplace.put("wert", targetVal);
		setFieldUtil.setField(d, "kind2", toReplace);
		assertEquals(targetVal, d.otherChildVal.getValue());
		assertNull(d.getValue());
		assertNull(d.childVal.getValue());
	}

	static class Dummy implements FieldAliases, Serializable {
		private String value;
		private String secondVal;
		private Map<String, Object> valMap;

		ChildClass childVal = new ChildClass();
		OtherChildClass otherChildVal = new OtherChildClass();

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}

		@GherkinMethod("getSecondValue()")
		String getSecondValue() {
			return secondVal;
		}

		@GherkinMethod("getValMap()")
		Map<String, Object> getValueMap() {
			return valMap;
		}

		@Override
		public Map<String, String> getFieldAliases() {

			return new HashMap<String, String>() {
				{
					put("kindwert", "childVal.value");
					put("kind1", "childVal");
					put("kind2", "otherChildVal");
				}
			};
		}
	}

	static class ChildClass {
		private String value;

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}

	}


	static class OtherChildClass implements FieldAliases, Serializable {
		private String value;

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}

		@Override
		public Map<String, String> getFieldAliases() {

			return new HashMap<String, String>() {
				{
					put("wert", "value");
				}
			};
		}

	}
}
