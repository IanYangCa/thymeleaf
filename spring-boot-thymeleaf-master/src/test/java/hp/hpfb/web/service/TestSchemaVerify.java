package hp.hpfb.web.service;

import java.util.List;

import org.junit.Test;

import hp.hpfb.web.service.impl.XmlSchemaValidatingServiceImpl;

public class TestSchemaVerify {
	
	@Test
	public void testVerifyXml() {
		XmlSchemaValidatingService service = new XmlSchemaValidatingServiceImpl();
		List<String> errors = service.verifyXml("src/test/resources/1.xml");
		for(String err : errors) {
			System.out.println(err);
		}
//		service.verifyXml("src/test/resources/testFile.xml");
		errors = service.verifyXmlBySchema("c:/temp/schema/SPL.xsd", "src/test/resources/testFile.xml");
		for(String err : errors) {
			System.out.println(err);
		}
	}

}
