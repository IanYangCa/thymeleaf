package hp.hpfb.web.service;

import java.util.List;

import org.xml.sax.SAXException;

import hp.hpfb.web.exception.SplException;

public interface XmlSchemaValidatingService {
	public List<String> verifyXml(String xmlFile) throws SAXException, SplException;
	public List<String> verifyXmlBySchema(String schemaFile, String xmlName) throws SplException;
}
