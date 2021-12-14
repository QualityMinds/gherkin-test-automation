package de.qualityminds.gta.driver.utils.reflection.expressions;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import de.qualityminds.gta.config.GherkinProperties;
import de.qualityminds.gta.config.SpringConfig;
import de.qualityminds.gta.driver.utils.exceptions.ResolveException;
import de.qualityminds.gta.driver.utils.reflection.expressions.ExpressionUtil.ResolveResult;
import net.serenitybdd.core.Serenity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@SpringBootTest(classes = {GherkinProperties.class, SpringConfig.class})
class ExpressionUtilTest {
	private static final String FIELD_GHERKIN_PROPERTIES = "properties";

	@Autowired
	ExpressionUtil expressionUtil;

	@Autowired
	GherkinProperties properties;

	@AfterEach
	void tearDown() {
		Serenity.clearCurrentSession();
	}

	@Test
	void testexpressionUtilInjection() {
		assertNotNull(expressionUtil);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testResolvePathWithOnlyNULLdata(String path) throws ResolveException {
		assertNull(expressionUtil.resolvePath(null, path));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testResolvePathWithValidPathOnNullObj(String obj) {
		assertThrows(ResolveException.class, () -> expressionUtil.resolvePath(obj, "value"));
	}

	@Test
	void testResolvePathWithEmptyPathOnEmptyObj() throws ResolveException {
		assertEquals("", expressionUtil.resolvePath("", ""));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"."})
	void testResolvePathWithEmptyPathOnValidObj1(String value) throws ResolveException {
		Dummy dummy = new Dummy();
		assertEquals(dummy, expressionUtil.resolvePath(dummy, value));
	}

	@Test
	void testResolveValidDirectPathOnValidObj() throws ResolveException {
		Dummy d = new Dummy();
		Object resolved = expressionUtil.resolvePath(d, "value");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(d.getValue(), resolved);
	}

	@Test
	void testResolveValidDirectPathOnValidObjCaseInvariant() throws ResolveException {
		Dummy d = new Dummy();
		Object resolved = expressionUtil.resolvePath(d, "VaLuE");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(d.getValue(), resolved);
	}

	@Test
	void testResolveValidIndirectPathOnValidObj() throws ResolveException {
		Dummy d = new Dummy();
		Object resolved = expressionUtil.resolvePath(d, "childVal.value");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(d.childVal.getValue(), resolved);
	}

	@Test
	void testResolveValidPathOnMapWithObject() throws ResolveException {
		Dummy d = new Dummy();
		Map<String, Object> map = new HashMap<>();
		map.put("child", d.childVal);
		Object resolved = expressionUtil.resolvePath(map, "childvalue");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(d.childVal.value, resolved);
	}

	static Stream<Arguments> validPathMaps() {
		Map<String, String> map = new HashMap<String, String>() {
			{
				put("AB", "correct");
			}
		};
		Map<String, String> map1 = new HashMap<String, String>() {
			{
				put("aB", "correct");
			}
		};
		Map<String, String> map2 = new HashMap<String, String>() {
			{
				put("AB.CD", "correct");
			}
		};
		List<String> list = new ArrayList<String>() {
			{
				add("wrong");
				add("correct");
			}
		};
		Map<String, List<String>> map3 = new HashMap<String, List<String>>() {
			{
				put("AB", list);
			}
		};
		List<List<String>> listInList = new ArrayList<List<String>>() {
			{
				add(list);
			}
		};
		return Stream.of(Arguments.of("AB", map),
				Arguments.of("Ab", map1),
				Arguments.of("AB.CD", map2),
				Arguments.of("AB[2]", map3),
				Arguments.of("[1][2]", listInList));
	}

	@ParameterizedTest
	@MethodSource("validPathMaps")
	void testResolveValidPathOnMapWithString(String str, Object map)
			throws ResolveException {
		Object resolved = expressionUtil.resolvePath(map, str);
		assertNotNull(resolved, "resolved path is null");
		assertEquals("correct", resolved);
	}

	@Test
	void testResolveValidPathOnObjectInListInList() throws ResolveException {
		List<Dummy> list = new ArrayList<Dummy>() {
			{
				add(new Dummy());
			}
		};
		List<Dummy> nullList = new ArrayList<Dummy>() {
			{
				add(null);
			}
		};
		List<List<Dummy>> listInList = new ArrayList<List<Dummy>>() {
			{
				add(list);
				add(nullList);
			}
		};

		Object resolvedValue = expressionUtil.resolvePath(listInList, "[1][1].value");
		assertNotNull(resolvedValue, "resolved path is null");
		assertEquals(Dummy.DUMMY_VALUE, resolvedValue);

		Object resolvedNull = expressionUtil.resolvePath(listInList, "[2][1]");
		assertNull(resolvedNull, "resolved path is not null");
	}

	@Test
	void testResolveValidPathOnListInObjectInMap() throws ResolveException {
		ListDummy listDummy = new ListDummy();
		Map<String, ListDummy> map = new HashMap<String, ListDummy>() {
			{
				put("qed", new ListDummy());
			}
		};

		Object resolved = expressionUtil.resolvePath(map, "qed.childList[1]");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(listDummy.childList.get(0), resolved);
	}

	@Test
	void testResolveValidPathOnObjectInList() throws ResolveException {
		List<Dummy> list = new ArrayList<Dummy>() {
			{
				add(new Dummy());
			}
		};
		Object resolved = expressionUtil.resolvePath(list, "[1].value");
		assertNotNull(resolved, "resolved path is null");
		assertEquals(Dummy.DUMMY_VALUE, resolved);
	}

	@Test
	void testGetProperties()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field propertyField = TestParameters.class.getDeclaredField(FIELD_GHERKIN_PROPERTIES);
		propertyField.setAccessible(true);
		GherkinProperties properties = (GherkinProperties) propertyField.get(expressionUtil);
		assertNotNull(properties, "Properties should have been set");
	}

	@Test
	void testHasParameterBeforeSet() {
		assertFalse(expressionUtil.hasParameter(""));
	}

	@Test
	void testGetParameterBeforeSet() {
		assertNull(expressionUtil.getParameterValue(""));
	}

	@Test
	void testSetParameter() {
		expressionUtil.setParameter("key", "value");
		assertEquals("value", expressionUtil.getParameterValue("key"));
	}

	@Test
	void testSetParameterResolvesExpressions() {
		expressionUtil.setParameter("key", "${" + properties.getNullParameter() + "}");
		assertNull(expressionUtil.getParameterValue("key"));
	}

	@Test
	void testHasParameterEmptySerenitySession() {
		Serenity.clearCurrentSession();
		testHasParameterBeforeSet();
	}

	@Test
	void testGetParameterEmptySerenitySession() {
		Serenity.clearCurrentSession();
		testGetParameterBeforeSet();
	}

	@Test
	void testSetParameterEmptySerenitySession() {
		Serenity.clearCurrentSession();
		testSetParameter();
	}

	@Test
	void testHasParameterAfterSet() {
		String key = "key";
		String value = "value";
		expressionUtil.setParameter(key, value);
		assertTrue(expressionUtil.hasParameter(key));
	}

	@Test
	void testGetParameterAfterSet() {
		String key = "key";
		String value = "value";
		expressionUtil.setParameter(key, value);
		assertEquals(value, expressionUtil.getParameterValue(key), "Could not get correct value from Expressions");
	}

	@Test
	void testResolveExpressionPhasesString() throws ResolveException {
		ResolveResult resolveResult = expressionUtil.resolveExpressions("1,2,3,8+1");

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertFalse(resolveResult.isExpression());
	}

	@Test
	void testResolveExpressionWithRandomExpression() throws ResolveException {
		ResolveResult resolveResult = expressionUtil.resolveExpressions("String a = \"b\"");

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertFalse(resolveResult.isExpression());
	}

	@Test
	void testResolveExpressionWithValidExpressionNoConcat() throws ResolveException {
		expressionUtil.setParameter("dummy", null);
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${dummy}");

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertTrue(resolveResult.isExpression());
		assertNull(resolveResult.getResolvedObject(), "resolveObject should be null");
	}

	@Test
	void testResolveExpressionWithValidExpressionConcatNullObject() throws ResolveException {
		Dummy dummy = new Dummy();
		expressionUtil.setParameter("dummy", dummy);
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${dummy.value}post");

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertTrue(resolveResult.isExpression());
		assertEquals(Dummy.DUMMY_VALUE + "post", resolveResult.getResolvedObject());
	}

	@Test
	void testResolveExpressionWithValidExpressionConcatWithObject() throws ResolveException {
		expressionUtil.setParameter("dummy", new Dummy());
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${dummy.value}post", new Dummy());

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertTrue(resolveResult.isExpression());
		assertEquals(Dummy.DUMMY_VALUE + "post", resolveResult.getResolvedObject(),
				"unexpected concatenation result\n");
	}

	@Test
	void testResolveExpressionWithValidExpressionConcatWithObject2() throws ResolveException {
		expressionUtil.setParameter("dummy", new Dummy());
		ResolveResult resolveResult = expressionUtil.resolveExpressions("post${dummy.value}", new Dummy());

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertTrue(resolveResult.isExpression());
		assertEquals("post" + Dummy.DUMMY_VALUE, resolveResult.getResolvedObject(),
				"unexpected concatenation result\n");
	}

	@Test
	void testResolveExpressionWithValidExpressionOnObject() throws ResolveException {
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${getvalue()}", new Dummy());

		assertNotNull(resolveResult, "resolveResult should not be null");
		assertTrue(resolveResult.isExpression());
		assertEquals(Dummy.DUMMY_VALUE, resolveResult.getResolvedObject(), "unexpected dummy value");
	}

	@Test
	void testResolveGlobalMethodStringWithoutMethodIndicationAndNoFallback() {
		expressionUtil.properties.setFallBackFromUnknownParamToMethodEnabled(false);
		assertThrows(ResolveException.class, () -> expressionUtil.resolveExpressions("${getOne}"));
	}

	@Test
	void testResolveGlobalMethodNotExistent() {
		assertThrows(ResolveException.class, () -> expressionUtil.resolveExpressions("${getOneAsString()}"));
	}

	@Test
	void testResolveGlobalMethodNotExistentWithoutMethodIndication() {
		expressionUtil.properties.setFallBackFromUnknownParamToMethodEnabled(true);
		assertThrows(ResolveException.class, () -> expressionUtil.resolveExpressions("${getOneAsString}"));
	}

	@Test
	void testGlobalMethodWithParameter() throws ResolveException {
		String input = "teststring";
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${mirrorInput(" + input + ")}");
		assertNotNull(resolveResult);
		assertTrue(resolveResult.isExpression());
		Object resolvedObject = resolveResult.getResolvedObject();
		assertNotNull(resolvedObject);
		assertTrue(resolvedObject instanceof String);
		assertEquals(input, resolvedObject);
	}

	@Test
	void testGlobalMethodWithVarargs() throws ResolveException {
		String a = "a";
		String b = "b";
		String c = "c";
		String expected = String.join(",", a, b, c);

		ResolveResult resolveResult = expressionUtil.resolveExpressions("${concat(" + a + ", " + b + "," + c + ")}");
		assertNotNull(resolveResult);
		assertTrue(resolveResult.isExpression());
		Object resolvedObject = resolveResult.getResolvedObject();
		assertNotNull(resolvedObject);
		assertTrue(resolvedObject instanceof String);
		assertEquals(expected, resolvedObject);
	}

	static Stream<Arguments> methodsAndParameters() {
		return Stream.of(
				Arguments.of("${getOne()}", "One"),
				Arguments.of("${getTwo()}", 2),
				Arguments.of("${getInputBackNtimes(${getInputBackNtimes(a,2)},3)}", "aaaaaa"),
				Arguments.of("${getInputBackNtimes(\"${getInputBackNtimes(a,2)}\",3)}",
						"${getInputBackNtimes(a,2)}${getInputBackNtimes(a,2)}${getInputBackNtimes(a,2)}"),
				Arguments.of("${getInputBackNtimes(${getInputBackNtimes(${getInputBackNtimes(a,2)},2)},3)}",
						"aaaaaaaaaaaa"),
				Arguments.of("${getInputBackNtimes(getInputBackNtimes(${getInputBackNtimes(a,2)},2),3)}",
						"aaaaaaaaaaaa")
		);
	}

	@ParameterizedTest
	@MethodSource("methodsAndParameters")
	void testResolveGlobalMethodString(String value, Object expected) throws ResolveException {
		ResolveResult resolveResult = expressionUtil.resolveExpressions(value);
		assertNotNull(resolveResult);
		assertTrue(resolveResult.isExpression());
		Object resolvedObject = resolveResult.getResolvedObject();
		assertNotNull(resolvedObject);
		assertEquals(expected, resolvedObject);
	}

	@Test
	void testResolveGlobalMethodStringWithoutMethodIndicationButWithFallback() throws ResolveException {
		expressionUtil.properties.setFallBackFromUnknownParamToMethodEnabled(true);
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${getOne}");
		assertNotNull(resolveResult);
		assertTrue(resolveResult.isExpression());
		Object resolvedObject = resolveResult.getResolvedObject();
		assertNotNull(resolvedObject);
		assertTrue(resolvedObject instanceof String);
		assertEquals("One", resolvedObject);
	}

	@Test
	void testResolveGlobalMethodDummyClass() throws ResolveException {
		ResolveResult resolveResult = expressionUtil.resolveExpressions("${getNewDummy()}");
		assertNotNull(resolveResult);
		assertTrue(resolveResult.isExpression());
		Object resolvedObject = resolveResult.getResolvedObject();
		assertNotNull(resolvedObject);
		assertTrue(resolvedObject instanceof Dummy);
	}

	@Test
	void testSplitter() {
		String[] splits = expressionUtil.splitGherkin("a${b}c");
		assertEquals("a", splits[0]);
		assertEquals("${b}", splits[1]);
		assertEquals("c", splits[2]);
		assertEquals(3, splits.length);

		splits = expressionUtil.splitGherkin("a${b${c}}d");
		assertEquals("a", splits[0]);
		assertEquals("${b${c}}", splits[1]);
		assertEquals("d", splits[2]);
		assertEquals(3, splits.length);
	}

	static class Dummy {
		static String DUMMY_VALUE = "123456789";
		private String value = DUMMY_VALUE;

		ChildClass childVal = new ChildClass();

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}
	}

	static class ListDummy {
		List<String> childList = new ArrayList<String>() {
			{
				add("3443414f");
			}
		};
	}

	static class ChildClass {
		private String value = "987654321";

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}
	}

	@Component
	static class MethodProvider implements GlobalGherkinMethodProvider {

		@GherkinMethod("getOne()")
		String getOneAsString() {
			return "One";
		}

		@GherkinMethod("getTwo()")
		int getTwoAsInt() {
			return 2;
		}

		@GherkinMethod("getNewDummy()")
		Dummy getNewDummy() {
			return new Dummy();
		}

		@GherkinMethod("mirrorInput({just some description that a string is expected})")
		String getInputBack(String input) {
			return input;
		}

		@GherkinMethod("concat(someStrings)")
		String concatSomeStrings(Object... values) {
			String[] stringArray = Arrays.copyOf(values, values.length, String[].class);
			return String.join(",", stringArray);
		}

		@GherkinMethod("getInputBackNtimes(input, multiplier)")
		String getInputBackNtimes(String input, String number) {
			int n = Integer.parseInt(number);
			StringBuilder toReturn = new StringBuilder();
			for (int i = 0; i < n; i++) {
				toReturn.append(input);
			}
			return toReturn.toString();
		}
	}
}
