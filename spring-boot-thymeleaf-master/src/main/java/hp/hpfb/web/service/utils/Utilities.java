package hp.hpfb.web.service.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import hp.hpfb.web.exception.SplException;
import hp.hpfb.web.model.Parameters;
import hp.hpfb.web.model.ReportMessage;

@Component
public class Utilities {

	public static final String XML = ".xml";
	public static final String XSLT = ".xslt";
	public static final String REPORT_XML = "report.xml";
	public static final String FILE_SEPARATOR = "/";
	private static final String SEPARATOR = "\n";
	private static final String YES = "yes";
//	private static final String NO = "no";
	private static final String UTF_8 = "UTF-8";
	public static String TARGET_BUSINESS_RULE_FILE = "rule";
	public static Pattern zipPattern = Pattern.compile(".+\\.zip$");
	private static Logger logger = LogManager.getLogger(Utilities.class);
	public static String PROPERTITIES = "properties";

	@Autowired
	@Value("${file.directory}")
	public String UPLOADED_FOLDER;
	@Autowired
	@Value("${source.rules.directory}")
	public String SRC_RULES_DIR;
	@Autowired
	@Value("${destance.rule.directory}")
	public String DEST_RULE_DIR;
	@Autowired
	@Value("${business.rule.file}")
	public String BUSINESS_RULE;
	@Autowired
	@Value("${business.rule.xslt}")
	public String[] BUSINESS_RULE_XSLT;
	@Autowired
	@Value("${file.xslt.directory}")
	public String LOCAL_XSLT_DIR;
	@Autowired
	@Value("${file.oid.directory}")
	public String OIDS_DIR;

	public void removeFile(String filename) {
		File file = new File(filename);
		if (file != null && file.exists()) {
			file.delete();
		}
	}

	public void removeFiles(String directory) {
		File dir = new File(directory);
		removeFiles(dir);
	}

	public void removeFiles(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					file.delete();
				} else {
					removeFiles(file);
					file.delete();
				}
			}
		}
	}

	public void removeDir(String directory) {
		File dir = new File(directory);
		removeDir(dir);
	}

	public void removeDir(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			removeFiles(directory);
			if(!directory.delete()) {
				logger.info("Directory remove failed: " + directory.getPath());
			}
		}
	}

	public String getXSD(String filePath) throws SAXException, SplException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(filePath);

			// Create XPathFactory object
			XPathFactory xpathFactory = XPathFactory.newInstance();

			// Create XPath object
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile("//urn:hl7-org:v3:document");
			Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			System.out.println("Node:" + node);
			if(node == null) {
				String xmlfile = filePath.substring(filePath.lastIndexOf(FILE_SEPARATOR)+1);
				throw new SplException("SPL-3:SPL-3-001:Validation Report Overview:" + xmlfile + ":Data Issue: The data is incorrect.");
			}
			String attr = null;
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				attr = node.getAttributes().item(i).toString();
				if (attr.startsWith("xsi:schemaLocation")) {
					if(attr.indexOf("http") > -1) {
						attr = attr.substring(attr.indexOf("http"));
						return attr.substring(0, attr.length() - 1);
					} else {
						String[] attrs = StringUtils.split(attr, ' ');
						return attrs[attrs.length - 1];
					}
				}
			}
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			logger.error("Error:  " + e.getClass().getName() + "\n----\n" + StringUtils.join(e.getStackTrace(), SEPARATOR));
			if(SAXException.class.isInstance(e)) {
				throw new SAXException(e.getMessage());
			}
		}
		return "";
	}

	public synchronized void rebuildBusinessRule() {
		File ruleFile = new File(SRC_RULES_DIR + BUSINESS_RULE);
		File destFile = new File(DEST_RULE_DIR + TARGET_BUSINESS_RULE_FILE + Utilities.XSLT);
		if (destFile.exists() && ruleFile.lastModified() < destFile.lastModified()) {
			return;
		}
		String tempFileName = null;
		String ruleFileName = ruleFile.getPath();
		for (int i = 0; i < BUSINESS_RULE_XSLT.length; i++) {
			tempFileName = DEST_RULE_DIR + TARGET_BUSINESS_RULE_FILE + i + Utilities.XSLT;
			buildRule(SRC_RULES_DIR + BUSINESS_RULE_XSLT[i], ruleFileName, tempFileName, true);
			// if(i > 0) {
			// (new File(ruleFileName)).delete();
			// }
			ruleFileName = tempFileName;
		}
		if (destFile != null && destFile.exists()) {
			destFile.delete();
		}
		File f = new File(tempFileName);
		f.renameTo(destFile);
	}

	public void buildRule(String xsltFile, String ruleFile, String destFile, Boolean formatSource) {
		try {
			File stylesheet = new File(xsltFile);
			if (stylesheet == null || !stylesheet.exists()) {
				logger.info("Error: XSLT file --> " + xsltFile + " not existed!!!");
				return;
			}
			File rulefile = new File(ruleFile);
			if (rulefile == null || !rulefile.exists()) {
				logger.info("Error: Rule file --> " + rulefile + " not existed!!!");
				return;
			}
			File dstFile = new File(destFile);
			if (dstFile.exists() && rulefile.lastModified() < dstFile.lastModified()) {
				return;
			}
			logger.info("XSLT file: " + xsltFile);
			logger.info("Rule file: " + ruleFile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(rulefile);
			// ...
			StreamSource stylesource = new StreamSource(stylesheet);
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer transformer = transFact.newTransformer(stylesource);
			transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);

			DOMResult result = new DOMResult();
			Source xmlSource = new DOMSource(document);
			transformer.transform(xmlSource, result);
			Document doc = (Document) result.getNode();

			Document xml = builder.newDocument();
			LSSerializer serializer = ((DOMImplementationLS) xml.getImplementation()).createLSSerializer();
			LSOutput output = ((DOMImplementationLS) xml.getImplementation()).createLSOutput();
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFile), UTF_8));
			output.setEncoding(UTF_8);
			output.setCharacterStream(out);
			serializer.getDomConfig().canSetParameter("format-pretty-print", formatSource);
			serializer.write(doc, output);
			out.flush();
			out.close();
		} catch (Exception e) {
			logger.error("Error:  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
		}
	}

	public void renderXml(String xsltFile, String xmlFile, String targetFile, Map<String, String> params) throws SplException {
		try {
			File stylesheet = new File(xsltFile);
			if (stylesheet == null || !stylesheet.exists()) {
				logger.info("Error: XSLT file --> " + xsltFile + " not existed!!!");
				return;
			}
			logger.info("Stylesheet File: " + xsltFile);

			File xml = new File(xmlFile);
			if (xml == null || !xml.exists()) {
				logger.info("Error: XML file --> " + xml + " not existed!!!");
				return;
			}
			logger.info("XML File: " + xmlFile);
			logger.info("Output File: " + targetFile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xml);

			StreamSource stylesource = new StreamSource(stylesheet);
			TransformerFactory transFact = TransformerFactory.newInstance();
			logger.info("Loading Style sheet....");
			Transformer transformer = transFact.newTransformer(stylesource);
			logger.info("End loading Style sheet.");
			transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
			if (params != null) {
				Set<String> keys = params.keySet();
				for (String key : keys) {
					if(params.get(key) == null) {
						throw new SplException(key + " the value is missed");
					}
					transformer.setParameter(key, params.get(key));
				}
			}

			DOMResult result = new DOMResult();
			Source xmlSource = new DOMSource(document);
			logger.info("Start transfer ....");
			transformer.transform(xmlSource, result);
			logger.info("End of transfer.");
			Document doc = (Document) result.getNode();

			Document xmlDoc = builder.newDocument();
			LSSerializer serializer = ((DOMImplementationLS) xmlDoc.getImplementation()).createLSSerializer();
			LSOutput output = ((DOMImplementationLS) xmlDoc.getImplementation()).createLSOutput();
			File target = new File(targetFile);
			if (target != null && target.exists()) {
				target.delete();
			}
			FileOutputStream fout = new FileOutputStream(targetFile);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, UTF_8));
			output.setEncoding(UTF_8);
			output.setCharacterStream(out);
			serializer.write(doc, output);
			out.flush();
			out.close();
			fout.close();
			logger.info("Finished generated renderXml");
		} catch(IllegalArgumentException e) {
			logger.error("Error(Exception):  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
			throw new SplException("IllegalArgumentException throwed");
		} catch(LSException e) {
			logger.error("Error(Exception):  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
			throw new SplException("LSException throwed");
		} catch(TransformerException e) {
			logger.error("Error(Exception):  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
			throw new SplException("TransformerException throwed");
		} catch (FileNotFoundException e) {
			String schemaFile = e.getMessage().substring(xmlFile.lastIndexOf(FILE_SEPARATOR) + 1);
			throw new SplException("SPL-1:SPL-1-001:Validation Report Overview:" + schemaFile + ":Schema Issue: The schema location is incorrect.");
		} catch (Exception e) {
			logger.error("Error(Exception):  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
			throw new SplException("Other Exception throwed");
		}
	}

	public void generateProperties(String xmlFile, String xsltDir, String outputDir) throws SplException {
		renderXml(xsltDir + "properties.xslt", xmlFile, outputDir + "properties.xml", null);
	}

	public void unzipFile(String zipFile, String outputDir) {
		byte[] buffer = new byte[1024];
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				if (fileName.endsWith(File.separator) || fileName.endsWith(FILE_SEPARATOR)) {
					zipEntry = zis.getNextEntry();
					continue;
				}
				if (fileName.lastIndexOf(FILE_SEPARATOR) > -1) {
					fileName = fileName.substring(fileName.indexOf(FILE_SEPARATOR) + 1);
				}
				File newFile = new File(outputDir + fileName);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (Exception e) {
			logger.error("Error:  " + StringUtils.join(e.getStackTrace(), SEPARATOR));
		}
	}

	public boolean isZipFile(String fileName) {
		Matcher matcher = zipPattern.matcher(fileName);
		if (matcher.find() && matcher.start() == 0 && matcher.end() == fileName.length()) {
			return true;
		}
		return false;
	}

	public File findXmlFile(String directory) throws IOException {
		Path root = Paths.get(directory);
		PathMatcher filter = root.getFileSystem().getPathMatcher("glob:**/*.xml");
		List<Path> list = Files.list(root).filter(filter::matches).map(root::relativize).collect(Collectors.toList());
		return list != null && list.size() > 0 ? list.get(0).toFile() : null;
	}

	public List<ReportMessage> buildSchemaErrorReport(List<String> errors,  String filename, String outputDir) {
		List<ReportMessage> results = new ArrayList<ReportMessage>();
		ReportMessage report = null;
		String[] msgs = null;
		if(errors.size() == 1) {
			report = new ReportMessage();
			Parameters parameter = getObjectFromXml(Parameters.class, outputDir + PROPERTITIES + XML); //getParameters(outputDir);
			report.setDetails(String.format(report.getDetails(), filename, parameter.getDoctype(), parameter.getTemplate(), parameter.getContentStatus()));
			results.add(report);
		}
		for (int i = 0; i < errors.size(); i++) {
			report = new ReportMessage();
			if(errors.get(i).indexOf(" Error: ") > -1) {
				msgs = errors.get(i).split(" Error:");
				report.setRule("SYSTEM-0");
				report.setCategory("System Error");
				report.setLabel("An uncharacterized system error occurred please contact HPFB support.");
				report.setSeverity("Error");
				report.setTest("Schema Validation");
				report.setDetails(msgs[1]);
				report.setLocation(msgs[0]);
			} else {
				msgs = errors.get(i).split(":");
				report.setCategory(msgs[0]);
				report.getSeverity();
				report.setDetails(msgs[1]);
				report.setLocation(msgs[2]);
			}
			
			results.add(report);
		}
		return results;
	}

	public <T> T getObjectFromXml(Class<T> T, String filePath){
		try {
			File data = new File(filePath);
			JAXBContext jaxbContext = JAXBContext.newInstance(T);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			@SuppressWarnings("unchecked")
			T result = (T) jaxbUnmarshaller.unmarshal(data);
			return result;
		} catch (JAXBException e) {
			logger.error("Error(JAXBException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return null;
	}
	public <T> void writeObjectToXml(String filePath, T obj) {
		try {
			File objFile = new File(filePath);
			JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(obj, objFile);
			
		} catch (JAXBException e) {
			logger.error("Error(JAXBException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
	}
//	public Parameters getParameters(String path) {
//		try {
//			File data = new File(path + File.separator + PROPERTITIES + XML);
//			JAXBContext jaxbContext = JAXBContext.newInstance(Parameters.class);
//			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//			Parameters parameters = (Parameters) jaxbUnmarshaller.unmarshal(data);
//			return parameters;
//		} catch (JAXBException e) {
//			logger.error("Error(JAXBException): " + StringUtils.join(e.getStackTrace(), '\n'));
//		}
//		return new Parameters();
//	}
//	public Parameters writeParameters(String outputDir, Parameters parameters) {
//		try {
//			File parametersFile = new File(outputDir  + PROPERTITIES + XML);
//			JAXBContext jaxbContext = JAXBContext.newInstance(Parameters.class);
//			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//			jaxbMarshaller.marshal(parameters, parametersFile);
//			
//		} catch (JAXBException e) {
//			logger.error("Error(JAXBException): " + StringUtils.join(e.getStackTrace(), '\n'));
//		}
//		return new Parameters();
//	}

//	public Report getReportMsgs(String path) {
//		try {
//			File data = new File(path + File.separator + REPORT_XML);
//			JAXBContext jaxbContext = JAXBContext.newInstance(Report.class);
//			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//			Report report = (Report) jaxbUnmarshaller.unmarshal(data);
//			return report;
//		} catch (JAXBException e) {
//			logger.error("Error(JAXBException): " + StringUtils.join(e.getStackTrace(), '\n'));
//		}
//		return new Report();
//	}

	public String getXmlStylesheet(String xmlFile) {
		String result = StringUtils.EMPTY;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(xmlFile));
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 7 && nodes.item(i).getTextContent().endsWith(".xsl\"")) {
					String text = nodes.item(i).getTextContent();
					result = text.substring(text.indexOf("href=\"") + 6, text.length() - 1);
				}
			}
		} catch (SAXException | IOException e) {
			logger.error("Error(SAXException | IOException): " + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			logger.error("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return result;
	}

	public void copyURLtoFile(String source, String destDir) throws IOException {
		URL website = new URL(source);
		String destFile = source.substring(source.lastIndexOf('/') + 1);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(destDir + destFile);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();
		fos.close();
	}

	public String getImportFile(String xsltFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(xsltFile));
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 1) {
					NodeList children = nodes.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName().equals("xsl:import")) {
							return children.item(j).getAttributes().getNamedItem("href").getTextContent();
						}
					}
				}
			}
		} catch (SAXException | IOException e) {
			logger.error("Error(SAXException | IOException): " + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			logger.error("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}

	public String getIncludeFile(String xsltFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(xsltFile));
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 1) {
					NodeList children = nodes.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName().equals("xsl:include")) {
							return children.item(j).getAttributes().getNamedItem("href").getTextContent();
						}
					}
				}
			}
		} catch (SAXException | IOException e) {
			logger.error("Error(SAXException | IOException): " + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			logger.error("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}

	public void backupFile(String filename) {
		File file = new File(filename);
		if (file != null && file.exists()) {
			String sufix = null;
			File newFile = null;
			for (int i = 0; i < 1000; i++) {
				sufix = String.format(".%03d", i);
				newFile = new File(filename + sufix);
				if (!newFile.exists()) {
					break;
				}
			}
			file.renameTo(newFile);
		}
	}
	public void checkSystemDirectory() {
		File file = new File(UPLOADED_FOLDER);
		if(file == null || ! file.exists() || ! file.isDirectory()) {
			logger.error("!!!!!!Please check the directory: " + UPLOADED_FOLDER);
		}
		file = new File(SRC_RULES_DIR);
		if(file == null || ! file.exists() || ! file.isDirectory()) {
			logger.error("!!!!!!Please check the directory: " + SRC_RULES_DIR);
		}
		file = new File(DEST_RULE_DIR);
		if(file == null || ! file.exists() || ! file.isDirectory()) {
			logger.error("!!!!!!Please check the directory: " + DEST_RULE_DIR);
		}
		file = new File(LOCAL_XSLT_DIR);
		if(file == null || ! file.exists() || ! file.isDirectory()) {
			logger.error("!!!!!!Please check the directory: " + LOCAL_XSLT_DIR);
		}
		file = new File(OIDS_DIR);
		if(file == null || ! file.exists() || ! file.isDirectory()) {
			logger.error("!!!!!!Please check the directory: " + OIDS_DIR);
		}
	}
}
