package de.qualityminds.gta.driver.validation.xml;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

@Accessors(chain = true)
@Setter
@Component
public class XMLSchemaValidationUtil {
	@Autowired
	private ResourceLoader resourceLoader;

	public void validateAgainstXSD(Reader xmlReader, Source[] xsds) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(xsds);
		schema.newValidator().validate(new StreamSource(xmlReader));
	}

	public void validateAgainstXSD(Reader xmlReader, String xsdDirectory) throws SAXException, IOException {
		validateAgainstXSD(xmlReader, makeSourceArray(xsdDirectory));
	}

	private Source[] makeSourceArray(String xsdDirectory) throws IOException {
		File dir = resourceLoader.getResource("classpath:" + xsdDirectory).getFile();
		List<File> xsdFiles = (List<File>) FileUtils.listFiles(dir, new String[] { "xsd" }, true);
		return xsdFiles.stream().map(StreamSource::new).toArray(Source[]::new);
	}
}
