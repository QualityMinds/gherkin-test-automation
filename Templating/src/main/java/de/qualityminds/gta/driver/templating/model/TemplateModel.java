package de.qualityminds.gta.driver.templating.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import de.qualityminds.gta.driver.templating.helper.TemplateNullHelper;

@Data
public class TemplateModel<T extends TemplateModelRootElement> implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(TemplateModel.class);
	private static final String ROOT_MODEL_KEY = "model";
	private Map<String, Object> templateModel = new HashMap<>();

	private TemplateNullHelper helper = new TemplateNullHelper();

	public TemplateModel(T rootModel) {
		Map<String, Object> templateModel = getTemplateModel();
		templateModel.put(ROOT_MODEL_KEY, rootModel);
		templateModel.put(helper.getIdentifier(), helper);
	}

	@SuppressWarnings("unchecked")
	public T getModelObject() {
		return (T) (templateModel.get(ROOT_MODEL_KEY));
	}

	public String getTemplateName() {
		return getModelObject().getTemplateName();
	}

	public IContext getTemplateContext() {
		return new Context(TemplateModelRootElement.locale, getTemplateModel());
	}

	public String getTemplatePath() {
		return getModelObject().getTemplatePath();
	}

	public Map<String, Object> getTemplateModel() {
		T modelObject = getModelObject();
		if (modelObject != null && modelObject.isGetTemplateModelWithModelAsRoot()) {
			try {
				return introspect(modelObject);
			} catch (Exception e) {
				logger.error("Get template model with model as root (by introspection) failed");
				logger.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
			}
		}
		return templateModel;
	}

	private Map<String, Object> introspect(Object obj)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		Map<String, Object> result = new HashMap<>();
		BeanInfo info = Introspector.getBeanInfo(obj.getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			Method reader = pd.getReadMethod();
			if (reader != null)
				result.put(pd.getName(), reader.invoke(obj));
		}
		return result;
	}
}
