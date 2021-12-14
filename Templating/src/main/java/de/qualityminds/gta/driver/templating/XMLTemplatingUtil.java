package de.qualityminds.gta.driver.templating;

import java.io.*;
import java.util.Arrays;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Component;

import net.serenitybdd.screenplay.facts.Fact;
import org.apache.commons.lang3.RegExUtils;
import org.jetbrains.annotations.NotNull;
import org.thymeleaf.templatemode.TemplateMode;

import de.qualityminds.gta.driver.templating.model.TemplateModel;

@Component
public class XMLTemplatingUtil extends TemplatingUtil {

	@Override
	TemplateMode getTemplateMode() {
		return TemplateMode.XML;
	}

	@Override
	String getDefaultTemplateSuffix() {
		return ".xml";
	}

	@Override
	public Reader processDocumentToReader(TemplateModel<?> templateModel) throws IOException {
		return this.processDocumentToReader(templateModel, true);
	}

	public Reader processDocumentToReader(TemplateModel<?> templateModel, boolean prettyprint) throws IOException {
		Reader raw = super.processDocumentToReader(templateModel);
		if (prettyprint) {
			Reader readerNoWhiteSpaces = new RemoveWhitespaceReader(raw);
			PipedReader readerResult = new PipedReader();
			PipedWriter writer = new PipedWriter(readerResult);
			new Thread(new PrettyPrintXML(readerNoWhiteSpaces, writer)).start();
			return readerResult;
		} else {
			return raw;
		}
	}


	static class RemoveWhitespaceReader extends Reader {
		private final Reader raw;

		public RemoveWhitespaceReader(Reader raw) {
			this.raw = raw;
		}

		@Override
		public int read(@NotNull char[] cbuf, int off, int len) throws IOException {
			char[] a = new char[len];
			int read = raw.read(a, 0, len);
			if (read > 0) {
				char[] copy = Arrays.copyOfRange(a, 0, read);
				String str = new String(copy);
				str = RegExUtils.replaceAll(str, "(?:>)(\\s*)<", "><");
				for (int i = 0; i < str.length(); i++) {
					cbuf[off + i] = str.charAt(i);
				}

				return str.length();
			}
			return read;
		}

		@Override
		public void close() throws IOException {
			raw.close();
		}
	}

	static class PrettyPrintXML implements Runnable {
		private final Reader reader;
		private final Writer writer;

		public PrettyPrintXML(Reader reader, Writer writer) {
			this.reader = reader;
			this.writer = writer;
		}

		public void run() {
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

				Transformer transformer = factory.newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

				StreamResult xmlOutput = new StreamResult(writer);

				Source xmlInput = new StreamSource(reader);
				transformer.transform(xmlInput, xmlOutput);
			} catch (TransformerException e) {
				logger.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						logger.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
					}
				}
			}
		}
	}
}
