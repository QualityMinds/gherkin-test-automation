package de.qualityminds.gta.driver.utils.reflection;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.qualityminds.gta.driver.utils.reflection.config.SpringTestConfig;
import de.qualityminds.gta.driver.utils.reflection.expressions.GherkinMethod;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.FieldAliases;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
public class ObjectInspectionUtilTest {

	@Autowired
	ObjectInspectionUtil inspector;

	/*
	 * getInterfaces
	 */
	@Test
	public void getInterfacesOnNullObjTest() {
		List<Class<?>> interfaces = inspector.getInterfaces(null);
		assertEquals(0, interfaces.size());
	}

	@Test
	public void getInterfacesOnClassWithoutInterfacesTest() {
		List<Class<?>> interfaces = inspector.getInterfaces(this.getClass());
		assertEquals(0, interfaces.size());
	}

	@Test
	public void getInterfacesOnClassWithOneInterfaceTest() {
		List<Class<?>> interfaces = inspector.getInterfaces(EmptyChildClass.class);
		assertEquals(1, interfaces.size());
	}

	@Test
	public void getInterfacesOnClassWithTwoInterfacesTest() {
		List<Class<?>> interfaces = inspector.getInterfaces(OtherChildClass.class);
		assertEquals(2, interfaces.size());
	}

	/*
	 * hasDeclaredField
	 */

	@Test
	public void hasDeclaredFieldOnNullObjTest() {
		assertFalse(inspector.hasDeclaredField(null, "fieldName"));
	}

	@Test
	public void hasDeclaredFieldOnObjButNonexistantFieldname() {
		assertFalse(inspector.hasDeclaredField(Dummy.class, "fieldName"));
	}

	@Test
	public void hasDeclaredFieldOnObjWithValidFieldName() {
		assertTrue(inspector.hasDeclaredField(Dummy.class, "value"));
	}

	/*
	 * getDeclaredField
	 */

	@Test
	public void getDeclaredFieldOnNullObjTest() {
		assertThrows(NoSuchFieldException.class, () -> inspector.getDeclaredField(null, "fieldName"));
	}

	@Test
	public void getDeclaredFieldOnObjButNonexistantFieldname() {
		assertThrows(NoSuchFieldException.class, () -> inspector.getDeclaredField(Dummy.class, "fieldName"));
	}

	@Test
	public void getDeclaredFieldOnObjWithValidFieldName() throws NoSuchFieldException {
		String fieldName = "value";
		Field field = inspector.getDeclaredField(Dummy.class, fieldName);
		assertNotNull(field);
		assertEquals(fieldName, field.getName());
	}

	/*
	 * getDeclaredMethods
	 */

	@Test
	public void getDeclaredMethodOnNullObjTest() {
		assertThrows(NoSuchMethodException.class, () -> inspector.getDeclaredMethod(null, "getSomething"));
	}

	@Test
	public void getDeclaredMethodOnObjButNonexistantmethodname() {
		assertThrows(NoSuchMethodException.class, () -> inspector.getDeclaredMethod(Dummy.class, "getSomething"));
	}

	@Test
	public void getDeclaredMethodOnObjWithValidMethodName() throws NoSuchMethodException {
		int numParams = 0;
		String methodName = "getValue";
		Method method = inspector.getDeclaredMethod(Dummy.class, methodName + "_" + numParams);
		assertNotNull(method);
		assertEquals(methodName, method.getName());
	}

	/*
	 * getFieldValue
	 */

	@Test
	public void getFieldValueOnNullObjTest()
			throws IllegalArgumentException {
		assertThrows(IllegalArgumentException.class, () -> inspector.getFieldValue(null, "fieldName"));
	}

	@Test
	public void getFieldValueOnClassButNonexistantfieldname()
			throws IllegalArgumentException {
		assertThrows(NoSuchFieldException.class, () -> inspector.getFieldValue(Dummy.class, "fieldName"));
	}

	@Test
	public void getFieldValueOnObjButNonexistantfieldname() throws IllegalArgumentException {
		assertThrows(NoSuchFieldException.class, () -> inspector.getFieldValue(new Dummy(), "fieldName"));
	}

	@Test
	public void getFieldValueOnClassInsteadOfObject() throws IllegalArgumentException {
		String fieldName = "value";
		assertThrows(NoSuchFieldException.class, () -> inspector.getFieldValue(Dummy.class, fieldName));
	}

	@Test
	public void getFieldValueOnObjWithValidFieldNameAndInitializedValue()
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Dummy d = new Dummy();
		String fieldName = "value";
		Object field = inspector.getFieldValue(d, fieldName);
		assertNotNull(field);
		assertEquals(d.getValue(), field);
	}

	@Test
	public void getFieldValueOnObjWithValidFieldNameAndNotInitializedValue()
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		String fieldName = "secondVal";
		Object field = inspector.getFieldValue(new Dummy(), fieldName);
		assertNull(field);
	}

	/*
	 * getOrInstantiateField
	 */

	@Test
	public void getOrInstantiateFieldOnNullObjTest() {
		assertThrows(IllegalArgumentException.class, () -> inspector.getOrInstantiateField(null, "fieldName"));
	}

	@Test
	public void getOrInstantiateFieldOnClassButNonexistantFieldname() {
		assertThrows(NoSuchFieldException.class, () -> inspector.getOrInstantiateField(Dummy.class, "fieldName"));
	}

	@Test
	public void getOrInstantiateFieldOnObjButNonexistantFieldname() {
		assertThrows(NoSuchFieldException.class, () -> inspector.getOrInstantiateField(new Dummy(), "fieldName"));
	}

	@Test
	public void getOrInstantiateFieldOnClassWithValidFieldName() {
		String fieldName = "value";
		assertThrows(NoSuchFieldException.class, () -> inspector.getOrInstantiateField(Dummy.class, fieldName));
	}

	@Test
	public void getOrInstantiateFieldOnObjWithValidFieldNameAndInitializedValue()
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Dummy d = new Dummy();
		String fieldName = "value";
		Object field = inspector.getOrInstantiateField(d, fieldName);
		assertNotNull(field);
		assertEquals(d.getValue(), field);
	}

	@Test
	public void getOrInstantiateFieldOnObjWithValidFieldNameAndNotinitializedValue()
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Dummy d = new Dummy();
		String fieldName = "secondVal";
		Object field = inspector.getOrInstantiateField(d, fieldName);
		assertNotNull(field);
		assertEquals("", field);
	}

	@Test
	public void getOrInstantiateFieldOnEmptyListInvalidIndex() {
		Dummy d = new Dummy();
		d.setValList(new ArrayList<>());
		String fieldName = "NotAnIndex";
		assertThrows(NoSuchFieldException.class, () -> inspector.getOrInstantiateField(d.getValList(), fieldName));
	}

	@Test
	public void getOrInstantiateFieldOnEmptyListValidIndex() {
		Dummy d = new Dummy();
		d.setValList(new ArrayList<>());
		String fieldName = "0";
		assertThrows(NoSuchFieldException.class, () -> inspector.getOrInstantiateField(d.getValList(), fieldName));
	}

	@Test
	public void getOrInstantiateFieldOnFilledListValidIndex()
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Dummy d = new Dummy();
		ArrayList<String> list = new ArrayList<>();
		String listVal = "asdf";
		list.add(listVal);
		d.setValList(list);
		String fieldName = "0";
		Object object = inspector.getOrInstantiateField(d.getValList(), fieldName);
		assertEquals(listVal, object);
	}


	static class Dummy implements Serializable {
		private String value = "abcd";
		private String secondVal;
		private List<String> valList;

		EmptyChildClass childVal = new EmptyChildClass();
		OtherChildClass otherChildVal = new OtherChildClass();

		@GherkinMethod("getValue()")
		String getValue() {
			return value;
		}

		@GherkinMethod("getSecondValue()")
		String getSecondValue() {
			return secondVal;
		}

		@GherkinMethod("getValList()")
		List<String> getValList() {
			return this.valList;
		}

		public void setValList(List<String> l) {
			this.valList = l;
		}

	}


	static class EmptyChildClass implements Serializable {
	}


	static class OtherChildClass implements FieldAliases, Serializable {
		private static Map<String, String> aliases = new HashMap<>();

		static {
			aliases.put("wert", "value");
		}

		@Override
		public Map<String, String> getFieldAliases() {
			return aliases;
		}
	}
}


