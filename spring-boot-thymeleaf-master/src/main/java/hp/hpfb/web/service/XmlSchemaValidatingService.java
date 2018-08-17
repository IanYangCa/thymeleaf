package hp.hpfb.web.service;

import java.util.List;

import org.xml.sax.SAXException;

public interface XmlSchemaValidatingService {
	public List<String> verifyXml(String xmlFile) throws SAXException;
	public List<String> verifyXmlBySchema(String schemaFile, String xmlName);
}
