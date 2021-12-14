package de.qualityminds.gta.driver.utils.reflection.setfield.override;

import java.util.Map;
import java.util.stream.Collectors;

public interface FieldAliasesWithForwarding extends SetFieldOverride {

	@Override
	default OverrideRequest setFieldOverride(String name, Object value) {
		name = name.toLowerCase(); // keep it low
		Map<String, String> fieldAliases = getFieldAliases().entrySet().parallelStream()
				.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

		if (fieldAliases.containsKey(name)) {
			return new OverrideRequest().setMustCall(true).setNewFieldName(fieldAliases.get(name)).setNewValue(value);
		} else {
			for (Map.Entry<String, String> entry : getForwardingRules().entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if (name.startsWith(k.toLowerCase())) {
					return new OverrideRequest().setMustCall(true).setNewFieldName(v + "." + name).setNewValue(value);
				}
			}
		}

		return new OverrideRequest();
	}

	Map<String, String> getFieldAliases();

	Map<String, String> getForwardingRules();
}
