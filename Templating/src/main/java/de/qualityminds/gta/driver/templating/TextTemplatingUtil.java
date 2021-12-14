package de.qualityminds.gta.driver.templating;

import org.springframework.stereotype.Component;
import org.thymeleaf.templatemode.TemplateMode;

@Component
public class TextTemplatingUtil extends TemplatingUtil {
	@Override
	TemplateMode getTemplateMode() {
		return TemplateMode.TEXT;
	}

	@Override
	String getDefaultTemplateSuffix() {
		return ".txt";
	}
}
