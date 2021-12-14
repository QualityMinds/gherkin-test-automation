package de.qualityminds.gta.driver.templating;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import de.qualityminds.gta.driver.templating.exceptions.TemplatingException;
import de.qualityminds.gta.driver.templating.model.TemplateModel;

public abstract class TemplatingUtil {
	protected static final Logger logger = LoggerFactory.getLogger(TemplatingUtil.class);
	private final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
	private final TemplateEngine templateEngine = new TemplateEngine();

	private Thread t;
	private Boolean creationWorked;
	private String creationErrorMessage;

	abstract TemplateMode getTemplateMode();

	abstract String getDefaultTemplateSuffix();

	protected TemplatingUtil(TemplatingUtilSettings settings) {
		initTemplateEngine(settings);
	}

	protected TemplatingUtil() {
		initTemplateEngine(new TemplatingUtilSettings());
	}

	private void initTemplateEngine(TemplatingUtilSettings settings) {
		templateResolver.setForceTemplateMode(true);
		templateResolver.setTemplateMode(getTemplateMode());
		templateResolver.setSuffix(settings.getTemplateSuffixOverride() != null ? settings.getTemplateSuffixOverride() : getDefaultTemplateSuffix());
		templateResolver.setCharacterEncoding(settings.getCharacterEncoding());
		templateResolver.setCacheable(settings.isCacheable());
		templateEngine.setTemplateResolver(templateResolver);
	}

	public String processDocumentToString(TemplateModel<?> templateModel) throws IOException, TemplatingException {
		Reader reader = processDocumentToReader(templateModel);

		String string = IOUtils.toString(reader);
		verifySuccessfulTemplating();
		return string;
	}

	public Reader processDocumentToReader(TemplateModel<?> templateModel) throws IOException {
		PipedReader reader = new PipedReader();
		PipedWriter writer = new PipedWriter(reader);

		t = new Thread(() -> {
			try {
				processDocument(templateModel, writer);
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
				}
			}
		});

		t.start();

		return reader;
	}

	public void processDocument(TemplateModel<?> templateModel, Writer writer) {
		templateResolver.setPrefix(templateModel.getTemplatePath());
		IContext templateContext = templateModel.getTemplateContext();
		try {
			templateEngine.process(templateModel.getTemplateName(), templateContext, writer);
			creationWorked = true;
		} catch (Exception e) {
			creationWorked = false;
			creationErrorMessage = e.getMessage() + "\n Current templating-context was:\n" + templateContext.getVariableNames() + "\n";
		}
	}

	private void verifySuccessfulTemplating() throws TemplatingException {
		try {
			t.join();
		} catch (InterruptedException ignored) {
		}
		if (!creationWorked) {
			throw new TemplatingException(creationErrorMessage);
		}

	}

	@Data
	@Accessors(chain = true)
	public static class TemplatingUtilSettings {
		private String templateSuffixOverride;
		private String characterEncoding = "UTF-8";
		private boolean cacheable = true;
	}
}
