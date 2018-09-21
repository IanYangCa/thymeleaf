package hp.hpfb.web.service.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;
import hp.hpfb.web.exception.SplException;
import hp.hpfb.web.model.Errors;
import hp.hpfb.web.model.FailedAssert;
import hp.hpfb.web.model.Parameters;
import hp.hpfb.web.model.Report;
import hp.hpfb.web.model.ReportMessage;

public class TestFiles extends AbstractMockMvcTest {

	@Autowired
	private Utilities utilities;
	
	String dir = "";
//	@Test
	public void findFile() {
		try {
			File file = utilities.findXmlFile(utilities.UPLOADED_FOLDER);
			System.out.println("file name: " + file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public void checkXmlChildren() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(utilities.UPLOADED_FOLDER + "temp.xsl"));
			NodeList nodes = document.getChildNodes();
			for(int i = 0; i < nodes.getLength(); i++) {
				if(nodes.item(i).getNodeType() == 1 ) {
					NodeList children = nodes.item(i).getChildNodes();
					for(int j= 0; j < children.getLength(); j++) {
						System.out.println("child Type: " + children.item(j).getNodeType());
						System.out.println("child Name: " + children.item(j).getNodeName());
						System.out.println("child Value: " + children.item(j).getNodeValue());
						if(children.item(j).getNodeName().equals("xsl:import")) {
							System.out.println("import:" + children.item(j).getAttributes().getNamedItem("href"));
						}
					}
				}
				System.out.println("Node Type: " + nodes.item(i).getNodeType());
				System.out.println("Node Name: " + nodes.item(i).getNodeName());
				System.out.println("Node Value: " + nodes.item(i).getNodeValue());
			}
			System.out.println("Size: " + nodes.getLength());
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
//	@Test
	public void testRenderXml() throws URISyntaxException, SplException {
		String xmlFile = "e:/1.xml";
		String xsltFileUrl = utilities.getXmlStylesheet(xmlFile);
		String targetFilename = xsltFileUrl.substring(xsltFileUrl.lastIndexOf('/') + 1);
		if(StringUtils.isNotBlank(xsltFileUrl)) {
			try {
				String rootUrl = xsltFileUrl.substring(0, xsltFileUrl.lastIndexOf('/') + 1);
				utilities.copyURLtoFile(xsltFileUrl, utilities.UPLOADED_FOLDER);
				String importFilename = utilities.getImportFile(utilities.UPLOADED_FOLDER + targetFilename);
				if(StringUtils.isNotBlank(importFilename)) {
					utilities.copyURLtoFile(rootUrl + importFilename, utilities.UPLOADED_FOLDER);
					importFilename = utilities.getIncludeFile(utilities.UPLOADED_FOLDER + importFilename);
					if(StringUtils.isNotBlank(importFilename)) {
						utilities.copyURLtoFile(rootUrl + importFilename, utilities.UPLOADED_FOLDER);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			utilities.renderXml(utilities.UPLOADED_FOLDER + targetFilename, xmlFile, utilities.UPLOADED_FOLDER + "1Render.htm", null);
		}
	}
//	@Test
	public void testPathJoin() {
		Path path = Paths.get(utilities.UPLOADED_FOLDER, Utilities.FILE_SEPARATOR, "1Render.htm");
		File f = path.toFile();
		System.out.println("Path: " + path.toString() + "file exists: " + f.exists());
	}
//	@Test
	public void testWriteParameters() {
		Parameters p = new Parameters();
		p.setContentStatus(BigInteger.valueOf(10));
		p.setDisplayLanguage("name-eng");
		p.setDoctype(BigInteger.valueOf(1));
		p.setId("test id");
		p.setLanguage("eng");
		p.setTemplate(BigInteger.valueOf(2));
		utilities.writeParameters(p);
	}
//	@Test
	public void getParameters() {
		Parameters p = utilities.getParameters(utilities.UPLOADED_FOLDER);
		System.out.println("Display language: " + p.getDisplayLanguage());
		System.out.println("Id: " + p.getId());
		System.out.println("Language: " + p.getLanguage());
		System.out.println("Content status: " + p.getContentStatus());
		System.out.println("Doctype: " + p.getDoctype());
		System.out.println("Template: " + p.getTemplate());
		
	}
//	@Test
	public void testReadWriteErrorXml() {
		Errors errors = new Errors();
		errors.setFailedAssert(new FailedAssert());
		errors.getFailedAssert().setFlag("SYSTEM-1");
		errors.getFailedAssert().setId("SPL-2-003");
		errors.getFailedAssert().setTest("Check Schema File existed?");
		utilities.writeSchemaErrorToReport0(utilities.UPLOADED_FOLDER, errors);
		errors = utilities.readSchemaErrorFromReport0(utilities.UPLOADED_FOLDER);
		System.out.println("Finished test!");
	}
	@Test
	public void testReadReportXML() {
		Report report = utilities.getReportMsgs(utilities.UPLOADED_FOLDER);
		if(report != null && report.getReportMessage() != null) {
			for(ReportMessage item : report.getReportMessage()) {
				System.out.println("category: " + item.getCategory() + "  details: " + item.getDetails());
			}
		}
		
	}
}
