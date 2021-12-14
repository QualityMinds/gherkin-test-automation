package de.qualityminds.gta.driver.utils.reflection;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.springframework.stereotype.Component;
import lombok.Data;

import de.qualityminds.gta.driver.utils.reflection.expressions.GherkinMethod;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.FieldAliases;

import org.apache.commons.lang3.ArrayUtils;

@Component
public class ObjectInspectionUtil implements Serializable {
	public static final String SUFFIX_VARARGS = "_varargs";
	private final Map<Class<?>, ObjectReflectionInfo> objectReflectionInfo = new HashMap<>();

	public List<Class<?>> getInterfaces(Class<?> clazz) {
		return getObjectReflectionInfo(clazz).getInterfaces();
	}

	public List<Class<?>> getSuperclasses(Class<?> clazz) {
		return getObjectReflectionInfo(clazz).getSuperclasses();
	}

	public boolean hasDeclaredField(Class<?> clazz, String name) {
		name = name.toLowerCase(); // keep it low
		Map<String, Field> fields = getObjectReflectionInfo(clazz).getFields();
		return fields.containsKey(name);
	}

	public Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
		name = name.toLowerCase(); // keep it low
		Map<String, Field> fields = getObjectReflectionInfo(clazz).getFields();
		if (fields.containsKey(name)) {
			return fields.get(name);
		} else {
			throw new NoSuchFieldException("No such field \"" + name + "\" in class " + (clazz != null ? clazz.getSimpleName() : "NULL"));
		}
	}

	public Method getDeclaredMethod(Class<?> clazz, String name) throws NoSuchMethodException {
		String nameLower = name.toLowerCase(); // keep it low
		Map<String, Method> gherkinMethods = getObjectReflectionInfo(clazz).getGherkinMethods();
		if (gherkinMethods.containsKey(nameLower)) {
			return gherkinMethods.get(nameLower);
		} else {
			throw new NoSuchMethodException("No such @GherkinMethod annotated method: " + name);
		}
	}


	public static Field[] getAllFields(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		Field[] parentFields = {};

		Class<?> c = o.getClass();
		while (c != Object.class && c != null) {
			Class<?> superclass = c.getSuperclass();
			parentFields = ArrayUtils.addAll(parentFields, superclass.getDeclaredFields());
			c = c.getSuperclass();
		}

		return ArrayUtils.addAll(fields, parentFields);
	}

	public boolean implementsInterface(Class<?> clazz, Class<?> targetInterface) {
		if (clazz == null) {
			return false;
		}
		for (Class<?> i : getInterfaces(clazz)) {
			if (i.isAssignableFrom(targetInterface)) {
				return true;
			}
		}
		return false;
	}

	private ObjectReflectionInfo getObjectReflectionInfo(Class<?> clazz) {
		ensureInitializedObjectReflectionInfo(clazz);
		return objectReflectionInfo.get(clazz);
	}

	private void ensureInitializedObjectReflectionInfo(Class<?> clazz) {
		if (objectReflectionInfo.containsKey(clazz)) {
			return;
		}

		ObjectReflectionInfo info = new ObjectReflectionInfo();
		Class<?> currentClass = clazz;
		while (currentClass != null) {
			info.getInterfaces().addAll(extractInterfaces(currentClass));
			info.getSuperclasses().addAll(extractSuperclasses(currentClass));
			info.getFields().putAll(extractFields(currentClass));
			info.getGherkinMethods().putAll(extractGherkinMethods(currentClass));
			currentClass = currentClass.getSuperclass();
		}

		objectReflectionInfo.put(clazz, info);
	}

	private List<Class<?>> extractInterfaces(Class<?> clazz) {
		List<Class<?>> interfaces = new ArrayList<>();
		Collections.addAll(interfaces, clazz.getInterfaces());
		return interfaces;
	}

	private List<Class<?>> extractSuperclasses(Class<?> clazz) {
		List<Class<?>> superclasses = new ArrayList<>();

		Class<?> toCheck = clazz;
		while (!toCheck.getSimpleName().equalsIgnoreCase("Object")) {
			Class<?> superclass = toCheck.getSuperclass();
			if (superclass == null) break;

			superclasses.add(superclass);
			toCheck = superclass;
		}
		return superclasses;
	}

	private Map<String, Field> extractFields(Class<?> clazz) {
		Map<String, Field> fields = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			fields.put(field.getName().toLowerCase(), field);
		}

		Class<?> c = clazz;
		while (c != Object.class && c != null && c.getSuperclass() != null) {
			Class<?> superclass = c.getSuperclass();
			for (Field field : superclass.getDeclaredFields()) {
				field.setAccessible(true);
				fields.put(field.getName().toLowerCase(), field);
			}
			c = c.getSuperclass();
		}

		return fields;
	}

	private Map<String, Method> extractGherkinMethods(Class<?> clazz) {
		Map<String, Method> gherkinMethods = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			GherkinMethod[] gMethods = method.getAnnotationsByType(GherkinMethod.class);
			for (GherkinMethod gMethod : gMethods) {
				method.setAccessible(true);
				String name = gMethod.value().split("\\(")[0].toLowerCase();
				int numParams = method.getParameterTypes().length;
				if (numParams == 1 && method.getParameterTypes()[0].isArray()) {
					gherkinMethods.put(name + SUFFIX_VARARGS, method);
				} else {
					gherkinMethods.put(name + "_" + numParams, method);
				}

			}
		}
		return gherkinMethods;
	}

	public Object getFieldValue(Object target, String fieldName)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (target == null) {
			throw new IllegalArgumentException("Cannot get field value of null object");
		}
		if (fieldName == null) throw new IllegalArgumentException("Cannot get field value if field name is null");

		String fieldNameToGet = fieldName.trim();
		if (implementsInterface(target.getClass(), FieldAliases.class)) {
			fieldNameToGet = ((FieldAliases) target).getFieldAliases().getOrDefault(fieldNameToGet, fieldNameToGet);
		}
		return getDeclaredField(target.getClass(), fieldNameToGet).get(target);
	}

	public Object getOrInstantiateField(Object target, String fieldName)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			throw new IllegalArgumentException("Cannot get or instantiate field on null object");
		}
		if (target instanceof List) {
			try {
				int listElementIndex = Integer.parseInt(fieldName);
				List<?> targetList = (List<?>) target;
				if (targetList.size() > listElementIndex) {
					return targetList.get(listElementIndex);
				}
			} catch (NumberFormatException e) {
				throw new NoSuchFieldException("List did not contain element " + fieldName);
			}
			throw new NoSuchFieldException("List did not contain element " + fieldName);
		}
		Field declaredField = getDeclaredField(target.getClass(), fieldName);
		Object objValue = declaredField.get(target);
		if (objValue == null) {
			objValue = declaredField.getType().getDeclaredConstructor().newInstance();
			declaredField.set(target, objValue);
		}
		return objValue;
	}

	@Data
	private static class ObjectReflectionInfo {
		List<Class<?>> interfaces = new ArrayList<>();
		List<Class<?>> superclasses = new ArrayList<>();
		Map<String, Field> fields = new HashMap<>();
		Map<String, Method> gherkinMethods = new HashMap<>();
	}
}
