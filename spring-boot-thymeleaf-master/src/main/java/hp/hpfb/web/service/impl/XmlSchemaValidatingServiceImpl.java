package hp.hpfb.web.service.impl;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import hp.hpfb.web.handler.LocalErrorHandler;
import hp.hpfb.web.handler.Resolver;
import hp.hpfb.web.service.XmlSchemaValidatingService;
import hp.hpfb.web.service.utils.Utilities;

@Component
public class XmlSchemaValidatingServiceImpl implements XmlSchemaValidatingService {
	private SchemaFactory schemaFactory;
	@Autowired
	private Utilities utilities;

	public List<String> verifyXml(String xmlFile) throws SAXException {
		return verifyXmlBySchema(utilities.getXSD(xmlFile), xmlFile);
	}
	public List<String> verifyXmlBySchema(String schemaFile, String xmlName) {
		LocalErrorHandler errorHandler = new LocalErrorHandler();
		try {
			schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			System.out.println("Loaded schema validation provider " + schemaFactory.getClass().getName());
			System.err.println("Loaded schema validation provider " + schemaFactory.getClass().getName());
			schemaFactory.setErrorHandler(errorHandler);
			// create a grammar object.
			Schema schemaGrammar;
			schemaGrammar = StringUtils.startsWith(schemaFile, "http")? schemaFactory.newSchema(new URL(schemaFile)) : schemaFactory.newSchema(new File(schemaFile));
			if (schemaGrammar == null) {
				System.out.println("schemaGrammar==null");
				errorHandler.getErrors().add(0, "Schema Grammar File not found!");
				return errorHandler.getErrors();
			}

			Resolver resolver = new Resolver();
			// create a validator to validate against the schema.
			ValidatorHandler schemaValidator = schemaGrammar.newValidatorHandler();
			schemaValidator.setResourceResolver(resolver);
			schemaValidator.setErrorHandler(errorHandler);

			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			SAXParser parser = parserFactory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(schemaValidator);

			File xmlFile = new File(xmlName);
			if (!xmlFile.exists()) {
				System.out.println("xmlFile==null");
				errorHandler.getErrors().add(0, "XML File not found!");
				return errorHandler.getErrors();
			}

			String xmlFileToUri = xmlFile.toURI().toString();
			xmlFile = null;
			reader.parse(new InputSource(xmlFileToUri));
			// Note: It appears Xerces exits normally if validation errors were found. Saxon
			// throws an exception.
			int errorCount = errorHandler.getErrorCount();
			if (errorCount == 0) {
				System.err.println("Schema Validation successful");
//				errorHandler.getErrors().add(0, "Schema Validation successful!");
				return errorHandler.getErrors();
			} else {
//				String errorFileName = xmlFile.getPath().substring(0, xmlFile.getPath().lastIndexOf(File.separator)) + File.separator + LocalErrorHandler.SCHEMA_ERROR_FILE;
//				FileWriter errorFile = new FileWriter(errorFileName);
//				for(String item : errorHandler.getErrors()) {
//					errorFile.write(item);
//				}
//				errorFile.flush();
//				errorFile.close();
				System.err.println("Schema Validation complete: found " + errorCount + " error" + (errorCount == 1 ? "" : "s"));
//				errorHandler.getErrors().add(0, "Total found " + errorHandler.getErrorCount() + " error(s)");
				return errorHandler.getErrors();
			}
		} catch (SAXException saxe) {
			exit(1, "Error: " + saxe.getMessage());
			errorHandler.getErrors().add(0, "SAXException was found!");
			return errorHandler.getErrors();
		} catch (Exception e) {
			e.printStackTrace();
			exit(2, "Fatal Error: " + e);
			errorHandler.getErrors().add(0, "Other Exception!");
			return errorHandler.getErrors();
		}
	}
	private void exit(int errCode, String msg) {
		System.err.println("Error " + errCode + ": " + msg);
	}

}
