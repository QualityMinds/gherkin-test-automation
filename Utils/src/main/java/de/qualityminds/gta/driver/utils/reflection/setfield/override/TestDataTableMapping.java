package de.qualityminds.gta.driver.utils.reflection.setfield.override;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.qualityminds.gta.driver.utils.exceptions.TemplateDataMappingException;

public interface TestDataTableMapping extends SetFieldOverride {
	@Override
	default OverrideRequest setFieldOverride(String name, Object value) throws TemplateDataMappingException {
		List<Mapping> mappings = getTestDataTableMappings();
		for (Mapping mapping : mappings) {
			if (name.equalsIgnoreCase(mapping.getKey())) {
				return setFieldRemapped(name, mapping.getMappings(), value);
			}
		}
		return new OverrideRequest();
	}

	default OverrideRequest setFieldRemapped(String name, Map<String, String> mapping, Object value)
			throws TemplateDataMappingException {
		if (value instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) value;
			Map<String, String> newValue = new HashMap<>();
			for (Map.Entry<String, String> e : mapping.entrySet()) {
				String entryKey = e.getKey();
				if (dataMap.containsKey(entryKey)) {
					newValue.put(e.getValue(), dataMap.get(entryKey));
				}
			}

			return new OverrideRequest().setMustCall(true).setNewFieldName(null).setNewValue(newValue);
		} else {
			if (value == null && mapping != null && !mapping.isEmpty()) {
				Map<String, String> newValue = new HashMap<>();
				mapping.values().forEach(v -> newValue.put(v, null));
				return new OverrideRequest().setMustCall(false).setNewFieldName(name).setNewValue(newValue);
			}
			if (value == null || mapping == null) {
				throw new TemplateDataMappingException(
						"Error while mapping input data! Could not parse value from column " + name);
			} else {
				throw new TemplateDataMappingException(
						"Error while mapping input data! Column >" + name + "< with value >" + value.toString()
								+ "< does not provide one of the following properties: " + mapping.keySet().toString());
			}
		}
	}

	List<Mapping> getTestDataTableMappings();

}
