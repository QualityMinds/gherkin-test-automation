package de.qualityminds.gta.driver.templating.model;

import java.io.Serializable;

public abstract class TemplateModelRootElementWithXSD extends TemplateModelRootElement implements Serializable {
	public abstract String getXSDPath();
}
