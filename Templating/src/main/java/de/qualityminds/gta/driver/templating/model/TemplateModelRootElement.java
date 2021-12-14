package de.qualityminds.gta.driver.templating.model;

import java.io.Serializable;
import java.util.Locale;

import lombok.Getter;

public abstract class TemplateModelRootElement implements Serializable {
	@Getter
	protected static final Locale locale = Locale.GERMANY;
	@Getter
	protected boolean getTemplateModelWithModelAsRoot = false;

	public abstract String getTemplateName();
	public abstract String getTemplatePath();
}
