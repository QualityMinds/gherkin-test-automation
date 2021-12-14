package de.qualityminds.gta.driver.templating.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When the TemplateNullHelper checks fields if they are not null or empty, fields with this annotation will be skipped.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TemplateNullHelperIgnored {
}
