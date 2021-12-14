package de.qualityminds.gta.driver.templating;

import org.thymeleaf.templatemode.TemplateMode;

public class JSONTemplatingUtil extends TemplatingUtil {
    @Override
    TemplateMode getTemplateMode() {
        return TemplateMode.HTML;
    }

    @Override
    String getDefaultTemplateSuffix() {
        return ".json";
    }
}
