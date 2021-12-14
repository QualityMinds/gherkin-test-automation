package de.qualityminds.gta.driver.utils.reflection.setfield.override;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.qualityminds.gta.driver.utils.exceptions.TemplateDataMappingException;

public interface TestDataTableMappingWithFieldAliases extends SetFieldOverride {
	@Override
	default OverrideRequest setFieldOverride(String name, Object value) throws TemplateDataMappingException {
		name = name.toLowerCase(); // keep it low
		if (value instanceof String){

			Map<String, String> fieldAliases = getFieldAliases().entrySet().parallelStream()
					.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

			if (fieldAliases.containsKey(name)) {
				return new OverrideRequest().setMustCall(true).setNewFieldName(fieldAliases.get(name)).setNewValue(value);
			}
		} else {
			List<Mapping> mappings = getTestDataTableMappings();
			for (Mapping mapping : mappings) {
				if (name.equalsIgnoreCase(mapping.getKey())) {
					return setFieldRemapped(name, mapping.getMappings(), value);
				}
			}
		}

		return new OverrideRequest();
	}

	default OverrideRequest setFieldRemapped(String name, Map<String, String> mapping, Object value)
			throws TemplateDataMappingException {
		if (value instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) value;
			Map<String, String> newValue = mapping.entrySet().parallelStream()
					.filter(e -> dataMap.containsKey(e.getKey()))
					.collect(Collectors.toMap(Map.Entry::getValue, e -> dataMap.get(e.getKey())));
			return new OverrideRequest().setMustCall(true).setNewFieldName(null).setNewValue(newValue);
		} else {
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
	Map<String, String> getFieldAliases();

}
