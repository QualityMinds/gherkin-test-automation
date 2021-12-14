package de.qualityminds.gta.driver.utils.reflection.setfield.override;

import java.util.Map;

import lombok.Data;
import lombok.NonNull;

@Data
public class Mapping {

	private String key;

	@NonNull
	private Map<String, String> mappings;

	public Mapping forKey(String key) {
		setKey(key);
		return this;
	}
}
