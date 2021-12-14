package de.qualityminds.gta.driver.utils.reflection.setfield.override;

import lombok.Data;
import lombok.experimental.Accessors;

public interface SetFieldOverride {
	<U extends Throwable> OverrideRequest setFieldOverride(String fieldName, Object value) throws U;

	/**
	 * Defines a custom behavior when setField is called with fieldName if mustCall
	 * = true, setField is called again: if newFieldName is non-null, null-empty,
	 * setField(target, newFieldName, newValue) is called otherwise,
	 * setField(target, (Map) newValue) is called to set fields with the contents of
	 * the newValue Map
	 */
	@Data
	@Accessors(chain = true)
	class OverrideRequest {
		private boolean mustCall = false;
		private String newFieldName;
		private Object newValue;
	}
}
