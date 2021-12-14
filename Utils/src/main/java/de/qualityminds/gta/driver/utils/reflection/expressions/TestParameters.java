package de.qualityminds.gta.driver.utils.reflection.expressions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.Getter;

import de.qualityminds.gta.config.GherkinProperties;
import net.serenitybdd.core.Serenity;


@Component
public abstract class TestParameters {
	public static final String KEY_PARAMETERS = "GHERKIN_TEST_PARAMETERS";

	@Autowired
	@Getter
	protected GherkinProperties properties;

	public void setParameter(String key, Object value) {
		getParameterMap().put(key, value);
	}

	public Object getParameterValue(String key) {
		return getParameterMap().get(key);
	}

	public boolean hasParameter(String key) {
		return getParameterMap().containsKey(key);
	}

	public void removeParameter(String key){
		getParameterMap().remove(key);
	}

	public void removeAllWithPrefix(String key){
		getParameterMap().entrySet().removeIf(e -> e.getKey().startsWith(key));
	}

	private Map<String, Object> getParameterMap() {
		ensureInitializedParameterMap();
		return Serenity.sessionVariableCalled(KEY_PARAMETERS);
	}

	private void ensureInitializedParameterMap() {
		if (!Serenity.hasASessionVariableCalled(KEY_PARAMETERS)) {
			initializeParameterMap(properties.getNullParameter(), properties.getDefaultParams());
		}
	}

	// init HashMap in static function to avoid "Unknown Source" issues upon serialization
	// default values can be configured via yaml from gherkinProperties.defaultParams
	private static void initializeParameterMap(String nullParameterName, Map<String, String> defaultParams) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put(nullParameterName, null);
		if(defaultParams!=null){
			parameters.putAll(defaultParams);
		}
		Serenity.setSessionVariable(KEY_PARAMETERS).to(parameters);
	}
}