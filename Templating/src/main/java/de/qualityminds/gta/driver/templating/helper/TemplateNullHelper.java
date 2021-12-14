package de.qualityminds.gta.driver.templating.helper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import org.apache.commons.lang3.ArrayUtils;


@Component
public class TemplateNullHelper implements TemplateHelper, Serializable {

	@Override
	public String getIdentifier() {
		return "nullHelper";
	}

	public boolean isFieldAvailable(Object o, String s) {
		Field[] fields = getAllFields(o);
		return Arrays.stream(fields).anyMatch(f -> f.getName().equalsIgnoreCase(s));
	}

	public boolean isNodeByNamePopulated(Object o, String s) {
		if (isFieldAvailable(o, s)) {
			Field[] allFields = getAllFields(o);
			Object o1 = Arrays.stream(allFields).filter(f -> f.getName().equalsIgnoreCase(s)).collect(Collectors.toList()).get(0);
			return isNodePopulated(o1);
		} else {
			return false;
		}
	}

	public boolean isOneOfListPopulated(Object... objects) {
		if (objects == null || objects.length == 0) {
			return false;
		} else {
			boolean populated = false;
			int index = 0;
			while (!populated && index < objects.length) {
				populated = isNodePopulated(objects[index]);
				index++;
			}
			return populated;
		}
	}

	public boolean isNodePopulated(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof String || o instanceof Integer || o instanceof Double || o instanceof Long) {
			return true;
		}
		if (o instanceof Collection<?>) {
			return isCollectionPopulated((Collection<?>) o);
		}
		if (o instanceof Map<?, ?>) {
			return isMapPopulated((Map<?, ?>) o);
		}
		return areObjectFieldsPopulated(o);
	}

	private boolean areObjectFieldsPopulated(Object o) {
		Field[] allFields = getAllFields(o);
		for (Field f : allFields) {
			String fieldName = f.getName();
			if ("fieldAliases".equalsIgnoreCase(fieldName) || "this$0".equalsIgnoreCase(fieldName) || f.isAnnotationPresent(TemplateNullHelperIgnored.class)) {
				continue;
			}
			f.setAccessible(true);
			try {
				Object fieldValue = f.get(o);
				if (isNodePopulated(fieldValue)) {
					return true;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// We assume it's not populated
			}
		}

		return false;
	}

	private boolean isCollectionPopulated(Collection<?> c) {
		if (!c.isEmpty()) {
			for (Object listobject : c) {
				if (isNodePopulated(listobject)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isMapPopulated(Map<?, ?> map) {

		if (!map.isEmpty()) {
			for (Object entry : map.values()) {
				if (isNodePopulated(entry)) {
					return true;
				}
			}
		}
		return false;
	}


	private Field[] getAllFields(Object o) {
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
}
