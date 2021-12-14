package de.qualityminds.gta.driver.utils.reflection.setfield;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.NoArgsConstructor;

import de.qualityminds.gta.driver.utils.exceptions.ResolveException;
import de.qualityminds.gta.driver.utils.exceptions.TemplateDataMappingException;
import de.qualityminds.gta.driver.utils.reflection.ObjectInspectionUtil;
import de.qualityminds.gta.driver.utils.reflection.expressions.ExpressionUtil;
import de.qualityminds.gta.driver.utils.reflection.expressions.ExpressionUtil.ResolveResult;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.SetFieldOverride;
import de.qualityminds.gta.driver.utils.reflection.setfield.override.SetFieldOverride.OverrideRequest;

import org.apache.commons.lang3.StringUtils;

@Component
public class SetFieldUtil {

	private final ExpressionUtil expressionUtil;
	private final ObjectInspectionUtil objectInspectionUtil;

	public SetFieldUtil(ExpressionUtil expressionUtil, ObjectInspectionUtil objectInspectionUtil) {
		this.expressionUtil = expressionUtil;
		this.objectInspectionUtil = objectInspectionUtil;
	}

	public <T> SetFieldResult setField(Object target, Map<String, T> map) {
		SetFieldResult results = new SetFieldResult();
		map.forEach((key, value) -> {
			try {
				results.addSetFieldResult(setField(target, key, value));
			} catch (Exception ex) {
				results.addSkippedField(key, ex);
			}
		});
		return results;
	}

	public <T> SetFieldResult setField(Object target, String fieldName, T value) throws TemplateDataMappingException, ResolveException {

		checkFieldnameIsNotBlank(fieldName);
		try {
			SetFieldResult result = null;

			ResolveResult resolve = expressionUtil.resolveExpressions(value, target);
			Object resolved = resolve.getResolvedObject();
			if (resolve.getResolvedObject() != null) {
				result = setField(target, fieldName, resolved).addResolvedExpression(value, resolved);
			}

			if (result != null) {
				return result;
			}

			result = applyIfTargetImplementsASetfieldOverride(target, fieldName, value);
			if (result != null) {
				return result;
			}

			result = createFromJsonWithSetField(target, fieldName, value);
			if (result != null) {
				return result;
			}

			result = setFieldIfValueIsMap(target, fieldName, value);
			if (result != null) {
				return result;
			}

			setFieldOfFieldName(target, fieldName, value);

			return new SetFieldResult(fieldName);

		} catch (IllegalAccessException | NoSuchFieldException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
			String errormsg = "Fehler beim Zugriff auf das Feld " + fieldName + ": " + e.getMessage();
			throw new TemplateDataMappingException(errormsg, e);
		}
	}

	private void checkFieldnameIsNotBlank(String fieldName) throws TemplateDataMappingException {
		if (StringUtils.isBlank(fieldName)) {
			throw new TemplateDataMappingException("blank fieldname is not allowed.");
		}
	}


	/***
	 * @return if fieldName contains a JSON path (e.g. foo.bar), get or create foo to
	 * call setField(foo, bar, value)
	 */
	private <T> SetFieldResult createFromJsonWithSetField(Object target, String fieldName, T value)
			throws ResolveException, TemplateDataMappingException, IllegalAccessException, NoSuchFieldException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Matcher matcher = ExpressionUtil.PATTERN_ACCESS_CHILD.matcher(fieldName);
		if (matcher.matches()) {
			return setField(objectInspectionUtil.getOrInstantiateField(target, matcher.group(1)), matcher.group(2), value).addPathPrefix(matcher.group(1));
		}
		return null;
	}

	private <T> SetFieldResult setFieldIfValueIsMap(Object target, String fieldName, T value)
			throws IllegalAccessException, NoSuchFieldException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		if (value instanceof Map) {
			return setField(objectInspectionUtil.getOrInstantiateField(target, fieldName), ((Map<String, Object>) value)).addPathPrefix(fieldName);
		}
		return null;
	}

	private <T> void setFieldOfFieldName(Object target, String fieldName, T value)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = objectInspectionUtil.getDeclaredField(target.getClass(), fieldName);
		if (value instanceof Integer && field.getType() == String.class) {
			field.set(target, value.toString());
		} else {
			field.set(target, value);
		}
	}

	private <T> SetFieldResult applyIfTargetImplementsASetfieldOverride(Object target, String fieldName, T value)
			throws ResolveException, TemplateDataMappingException {
		for (Class<?> interfaceType : objectInspectionUtil.getInterfaces(target.getClass())) {
			if (SetFieldOverride.class.isAssignableFrom(interfaceType)) {
				OverrideRequest request = ((SetFieldOverride) target).setFieldOverride(fieldName, value);
				if (request.isMustCall()) {
					String newName = request.getNewFieldName();
					if (newName == null || newName.isEmpty()) {
						return setField(target, (Map<String, Object>) request.getNewValue());
					} else {
						return setField(target, request.getNewFieldName(), request.getNewValue());
					}
				}
			}
		}
		return null;
	}

	@NoArgsConstructor
	public static class SetFieldResult {
		@Getter
		private final List<String> fields = new ArrayList<>();
		@Getter
		private Map<String, Throwable> skippedFields = new HashMap<>();
		@Getter
		private final Map<Object, Object> resolvedExpressions = new HashMap<>();

		public SetFieldResult(String fieldName) {
			addField(fieldName);
		}

		public void addSetFieldResult(SetFieldResult result) {
			fields.addAll(result.getFields());
			skippedFields.putAll(result.getSkippedFields());
			resolvedExpressions.putAll(result.getResolvedExpressions());
		}

		public void addField(String fieldName) {
			fields.add(fieldName);
		}

		public void addSkippedField(String fieldName, Throwable ex) {
			skippedFields.put(fieldName, ex);
		}

		public SetFieldResult addResolvedExpression(Object expression, Object object) {
			resolvedExpressions.put(expression, object);
			return this;
		}

		public SetFieldResult addPathPrefix(String prefix) {
			fields.replaceAll(field -> (prefix + "." + field));
			skippedFields = skippedFields.entrySet().parallelStream().collect(Collectors.toMap(e -> prefix + "." + e.getKey(), Map.Entry::getValue));
			return this;
		}

		public boolean fieldIs(String expectation) {
			return fields.size() == 1 && fields.get(0).equals(expectation);
		}
	}
}
