package ca.hc.fb.app.xml;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class XmlRender {
	public static void main(String[] args) {
		
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jfc.setDialogTitle("Select A XML File");
		jfc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML file", "xml");
		jfc.addChoosableFileFilter(filter);

		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String xmlFile = jfc.getSelectedFile().getPath();
			System.out.println("XML file:" + xmlFile);
			String outputDir = xmlFile.substring(0, xmlFile.lastIndexOf(File.separator) + 1);
			System.out.println("out Dir:" + outputDir);
			String xsltFile = getXmlStylesheet(xmlFile);
			if(StringUtils.isNotBlank(xsltFile)) {
				xsltFile = outputDir + xsltFile.substring(xsltFile.lastIndexOf('/') + 1);
				System.out.println("XSLT file:" + xsltFile);
				downloadFiles(xmlFile, outputDir);
				try {
					renderXml(xsltFile, xmlFile, outputDir + "temp.html", null);
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						outputDir = outputDir.replace('\\','/');
					    Desktop.getDesktop().browse(new URI("file://" + outputDir + "temp.html"));
					}
					jfc.setCurrentDirectory(new File(outputDir));
					jfc.showOpenDialog(null);
				} catch (SplException e) {
					e.printStackTrace();
					System.out.println("!!!SplException:" + e.getErrorMsg());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static boolean fileIsexist(String dir, String filename) {
		File file = new File(dir + filename);
		if(file.exists()) {
			return true;
		}
		return false;
	}
	private static void copyURLtoFile(String source, String destDir) throws IOException {
		URL website = new URL(source);
		String destFile = source.substring(source.lastIndexOf('/') + 1);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(destDir + destFile);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();
		fos.close();
	}
	private static String getImportFile(String xsltFile) {
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
			System.out.println("Error(SAXException | IOException): " + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			System.out.println("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}
	public static String getIncludeFile(String xsltFile) {
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
			System.out.println("Error(SAXException | IOException): " + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			System.out.println("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}
	public static String getLabelFile(String xsltFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(xsltFile));
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 1) {
					NodeList children = nodes.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName().equals("xsl:param") && children.item(j).getAttributes().getNamedItem("name").getTextContent().equals("labelFile") ) {
							String temp = children.item(j).getAttributes().getNamedItem("select").getTextContent();
							return temp.substring(3, temp.length() -1);
						}
					}
				}
			}
		} catch (SAXException | IOException e) {
			System.out.println("Error(SAXException | IOException): " + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			System.out.println("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}
	public static String getCSSFile(String xsltFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(xsltFile));
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 1) {
					NodeList children = nodes.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName().equals("xsl:param") && children.item(j).getAttributes().getNamedItem("name").getTextContent().equals("cssFile") ) {
							String temp = children.item(j).getAttributes().getNamedItem("select").getTextContent();
							return temp.substring(3, temp.length() -1);
						}
					}
				}
			}
		} catch (SAXException | IOException e) {
			System.out.println("Error(SAXException | IOException): " + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			System.out.println("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return StringUtils.EMPTY;
	}
	private static void downloadFiles(String xmlFile, String outputDir) {
		String xsltFileUrl = getXmlStylesheet(xmlFile);
		if(StringUtils.isNotBlank(xsltFileUrl)) {
			try {
				String rootUrl = xsltFileUrl.substring(0, xsltFileUrl.lastIndexOf('/') + 1);
				String targetFilename = xsltFileUrl.substring(xsltFileUrl.lastIndexOf('/') + 1);
				if(! fileIsexist(outputDir, targetFilename)) {
					copyURLtoFile(xsltFileUrl, outputDir);
					String importFilename = getImportFile(outputDir + targetFilename);
					if(StringUtils.isNotBlank(importFilename)) {
						System.out.println("Import file: " + importFilename);
						copyURLtoFile(rootUrl + importFilename, outputDir);
						importFilename = getIncludeFile(outputDir + importFilename);
						if(StringUtils.isNotBlank(importFilename)) {
							System.out.println("Include file: " + importFilename);
							copyURLtoFile(rootUrl + importFilename, outputDir);
						}
					}
				}
				String temp = getLabelFile(outputDir + targetFilename);
				if(StringUtils.isNotBlank(temp) && ! fileIsexist(outputDir, temp)) {
					System.out.println("Label file: " + temp.substring(3, temp.length() -1));
					copyURLtoFile(rootUrl + temp.substring(3, temp.length() -1), outputDir);
				}
				temp = getCSSFile(outputDir + targetFilename);
				if(StringUtils.isNotBlank(temp) && ! fileIsexist(outputDir, temp)) {
					System.out.println("CSS file: " + temp.substring(3, temp.length() -1));
					copyURLtoFile(rootUrl + temp.substring(3, temp.length() -1), outputDir);
				}
			} catch (IOException e) {
				System.out.println(StringUtils.join(e.getStackTrace(), '\n'));
			}
		}

	}
	private static String getXmlStylesheet(String xmlFile) {
		String result = "";
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			File xml = new File(xmlFile);
			Document document = builder.parse(xml);
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == 7 && nodes.item(i).getTextContent().endsWith(".xsl\"")) {
					String text = nodes.item(i).getTextContent();
					result = text.substring(text.indexOf("href=\"") + 6, text.length() - 1);
				}
			}
		} catch (SAXException | IOException e) {
			System.out.println("Error(SAXException | IOException): " + StringUtils.join(e.getStackTrace(), '\n'));
		} catch (ParserConfigurationException e) {
			System.out.println("Error(ParserConfigurationException): " + StringUtils.join(e.getStackTrace(), '\n'));
		}
		return result;
	}
	private static void renderXml(String xsltFile, String xmlFile, String targetFile, Map<String, String> params) throws SplException {
		try {
			File stylesheet = new File(xsltFile);
			if (stylesheet == null || !stylesheet.exists()) {
				System.out.println("Error: XSLT file --> " + xsltFile + " not existed!!!");
				return;
			}
			System.out.println("Stylesheet File: " + xsltFile);

			File xml = new File(xmlFile);
			if (xml == null || !xml.exists()) {
				System.out.println("Error: XML file --> " + xml + " not existed!!!");
				return;
			}
			System.out.println("XML File: " + xmlFile);
			System.out.println("Output File: " + targetFile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xml);

			StreamSource stylesource = new StreamSource(stylesheet);
			TransformerFactory transFact = TransformerFactory.newInstance();
			System.out.println("Loading Style sheet....");
			Transformer transformer = transFact.newTransformer(stylesource);
			System.out.println("End loading Style sheet.");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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
			System.out.println("Start transfer ....");
			transformer.transform(xmlSource, result);
			System.out.println("End of transfer.");
			Document doc = (Document) result.getNode();

			Document xmlDoc = builder.newDocument();
			LSSerializer serializer = ((DOMImplementationLS) xmlDoc.getImplementation()).createLSSerializer();
			LSOutput output = ((DOMImplementationLS) xmlDoc.getImplementation()).createLSOutput();
			File target = new File(targetFile);
			if (target != null && target.exists()) {
				target.delete();
			}
			FileOutputStream fout = new FileOutputStream(targetFile);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
			output.setEncoding("UTF-8");
			output.setCharacterStream(out);
			serializer.write(doc, output);
			out.flush();
			out.close();
			fout.close();
			System.out.println("Finished generated renderXml");
		} catch(IllegalArgumentException e) {
			System.out.println("Error(Exception):  " + StringUtils.join(e.getStackTrace(), '\n'));
			throw new SplException("IllegalArgumentException throwed");
		} catch(LSException e) {
			System.out.println("Error(Exception):  " + StringUtils.join(e.getStackTrace(), '\n'));
			throw new SplException("LSException throwed");
		} catch(TransformerException e) {
			System.out.println("Error(Exception):  " + StringUtils.join(e.getStackTrace(), '\n'));
			throw new SplException("TransformerException throwed");
		} catch (FileNotFoundException e) {
			String schemaFile = e.getMessage().substring(xmlFile.lastIndexOf('/') + 1);
			throw new SplException("SPL-1:SPL-1-001:Validation Report Overview:" + schemaFile + ":Schema Issue: The schema location is incorrect.");
		} catch(Exception e) {
			System.out.println("Error(Exception):  " + StringUtils.join(e.getStackTrace(), '\n'));
			throw new SplException("Other Exception throwed");
		}
	
	}
}
