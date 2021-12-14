package de.qualityminds.gta.driver.validation.xml.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MatchValueAtXPath {
	String VAR_PARENT_NAMESPACE = "myParentsNamespace";
	String VAR_NO_NAMESPACE = "thisIsABlankNamespacePlaceholder";
	String value();

	/**
	 * ID true means that the field should be unique for all instances of the searched object.
	 * With that, the XPaths are built differently: BasePath + [ID-field = '..']/non-id-fields
	 * This allows us to search for a single object and show all differences from non-id-fields if we find an object with the correct id-field.
	 *
	 * If no ID field is used in a search, we are looking for BasePath + [field1 = '' & field2 = '' &...].
	 * In that case, we do not have a "correct" hit, and we can only say if we find an object with these properties or not.
	 **/
	boolean id() default false;
}
