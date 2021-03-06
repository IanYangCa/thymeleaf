package hp.hpfb.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import hp.hpfb.web.exception.SplException;
import hp.hpfb.web.model.UserFile;
import hp.hpfb.web.service.utils.Utilities;

@Controller
public class RenderXmlController {
	
	private static Logger logger = LogManager.getLogger(Utilities.class);

	@Autowired
	private Utilities utilities;

	@RequestMapping(value="/renderXML", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req) throws Exception {
		model.addAttribute("userFile", new UserFile());
		return "renderXml";
    }
	
	@RequestMapping(value="/renderXML", method=RequestMethod.POST)
	public String renderXmlConfirm(@ModelAttribute UserFile renderXml, Model model, HttpServletRequest req) throws Exception {
		String outputDir = utilities.UPLOADED_FOLDER + req.getSession().getId() + Utilities.FILE_SEPARATOR;
        File outDir = new File(outputDir);
        if(outDir != null && outDir.exists()) {
            utilities.removeFiles(outputDir);
        } else {
        	outDir.mkdir();
        }
        String filename = outputDir + renderXml.getFile().getOriginalFilename();
        byte[] bytes = renderXml.getFile().getBytes();
        Path path = Paths.get(filename);
        Files.write(path, bytes, StandardOpenOption.CREATE);
        if(utilities.isZipFile(filename)) {
        	utilities.unzipFile(filename, outputDir);
        	File temp = utilities.findXmlFile(outputDir);
        	filename = outputDir + temp.getPath();
        } 

		File file = utilities.findXmlFile(outputDir);
		if(file == null ) {
			model.addAttribute("message", "File is not exists!");
			return "renderXml";
		}
		String xmlFilename = file.getPath();
		if(file != null) {
			try {
				String xsltFilename = utilities.getXmlStylesheet(outputDir + xmlFilename);
				String version = "";
				if(xsltFilename.lastIndexOf('/') > -1) {
					version = xsltFilename.substring(0, xsltFilename.lastIndexOf('/'));
					xsltFilename = xsltFilename.substring(xsltFilename.lastIndexOf('/') + 1);
				}
				if(version.lastIndexOf('/') > -1) {
					version = version.substring(version.lastIndexOf('/') + 1);
				}
				if(StringUtils.isBlank(version) || "/".equals(version) || ".".equals(version) ||
						renderXml != null && renderXml.getLocal() != null && renderXml.getLocal()) {
					if(StringUtils.isBlank(version) || "/".equals(version) || ".".equals(version)) {
						utilities.renderXml(utilities.LOCAL_XSLT_DIR + xsltFilename, outputDir + xmlFilename, outputDir + "temp.htm", null);
					} else {
						utilities.renderXml(utilities.LOCAL_XSLT_DIR + version + Utilities.FILE_SEPARATOR + xsltFilename, outputDir + xmlFilename, outputDir + "temp.htm", null);
					}
				} else {
					String xsltFileUrl = utilities.getXmlStylesheet(outputDir + xmlFilename);
					if(StringUtils.isNotBlank(xsltFileUrl)) {
						try {
							String rootUrl = xsltFileUrl.substring(0, xsltFileUrl.lastIndexOf('/') + 1);
							String targetFilename = xsltFileUrl.substring(xsltFileUrl.lastIndexOf('/') + 1);
							utilities.copyURLtoFile(xsltFileUrl, outputDir);
							String importFilename = utilities.getImportFile(outputDir + targetFilename);
							if(StringUtils.isNotBlank(importFilename)) {
								utilities.copyURLtoFile(rootUrl + importFilename, outputDir);
								importFilename = utilities.getIncludeFile(outputDir + importFilename);
								if(StringUtils.isNotBlank(importFilename)) {
									utilities.copyURLtoFile(rootUrl + importFilename, outputDir);
								}
							}
						} catch (IOException e) {
							logger.error(StringUtils.join(e.getStackTrace(), '\n'));
							return "error";
						}
						utilities.renderXml(outputDir + xsltFilename, outputDir + file.getPath(), outputDir + "temp.htm", null);
					}

				}
			} catch (SplException e) {
				//TODO add later
			}
			
		}
		return "renderXmlDone";
    }
	@RequestMapping(value="/xmlHtml", method=RequestMethod.GET)
	public void xmlHtml(Model model, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// add as url for get rendered html file
    }

}
