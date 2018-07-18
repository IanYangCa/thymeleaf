package hp.hpfb.web.service;

import java.util.List;

public interface XmlSchemaValidatingService {
	public List<String> verifyXml(String xmlFile);
	public List<String> verifyXmlBySchema(String schemaFile, String xmlName);
}
