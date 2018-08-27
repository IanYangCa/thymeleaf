package hp.hpfb.web.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import hp.hpfb.web.model.Parameters;
import hp.hpfb.web.model.Report;
import hp.hpfb.web.model.ReportSchema;
import hp.hpfb.web.service.XmlSchemaValidatingService;
import hp.hpfb.web.service.utils.Utilities;

@Controller
public class ValidationXmlController {
	private static Logger logger = LogManager.getLogger(ValidationXmlController.class);
	
	@Autowired
	private XmlSchemaValidatingService service;
	@Autowired
	private Utilities utilities;
	
	@RequestMapping(value="/validateXML", method=RequestMethod.GET)
    public String validateXml(Model model, HttpServletRequest req) throws Exception {
		return "validateXml";
    }
    @RequestMapping(value="/validateXML", method=RequestMethod.POST)
    public String validation(@RequestParam("file") MultipartFile file, Model model,
            RedirectAttributes redirectAttributes, HttpServletRequest req) {
    	if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to validate");
            return "redirect:validateXML";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            String outputDir = utilities.UPLOADED_FOLDER + req.getSession().getId() + Utilities.FILE_SEPARATOR;
            String filename = outputDir + file.getOriginalFilename();
            File outDir = new File(outputDir);
            if(outDir != null && outDir.exists()) {
                utilities.removeFiles(outputDir);
            } else {
            	outDir.mkdir();
            }
            Path path = Paths.get(filename);
            Files.write(path, bytes, StandardOpenOption.CREATE);
            if(utilities.isZipFile(filename)) {
            	utilities.unzipFile(filename, outputDir);
            	File temp = utilities.findXmlFile(outputDir);
            	filename = outputDir + temp.getPath();
            } 
            
            
            List<String> errors;
			try {
				errors = service.verifyXml(filename);
			} catch (SAXException e) {
				errors = new ArrayList<String>(1);
				errors.add("Bad XML Format!");
				errors.add("Read XML File:Parse Exception:Bad XML Format!");
			}
            if( errors.size() > 0 ) {
            	List<ReportSchema> reports = utilities.buildSchemaErrorReport(errors);
                utilities.writeSchemaErrorToReport(outputDir, reports);
                model.addAttribute("errorList", reports);
            } else {
            	//build validate business rules
            	utilities.rebuildBusinessRule();
            	
            	utilities.renderXml(utilities.SRC_RULES_DIR + "stripVestiges.xslt", filename, outputDir + "strip.xml", null);
            	
            	//retrieve parameters from xml file to properties.xml
            	utilities.renderXml(utilities.SRC_RULES_DIR + Utilities.PROPERTITIES + Utilities.XSLT, filename, outputDir + Utilities.PROPERTITIES + Utilities.XML, null);
            	Parameters p = utilities.getParameters(outputDir);
            	Map<String, String> params = new HashMap<String, String>();
            	
            	params.put("display-language",  p.getDisplayLanguage());
            	params.put("oid_loc", utilities.OIDS_DIR);
            	params.put("id",  file.getOriginalFilename());
            	params.put("property-file", outputDir + Utilities.PROPERTITIES + Utilities.XML);
            	logger.info("oid_loc:" + utilities.OIDS_DIR);
            	utilities.renderXml(utilities.DEST_RULE_DIR + Utilities.TARGET_BUSINESS_RULE_FILE + Utilities.XSLT, outputDir + "strip.xml", outputDir + "report0.xml", params);
				utilities.renderXml(utilities.SRC_RULES_DIR + "report.xslt", outputDir + "report0.xml", outputDir + "report.xml", params );
				Report report = utilities.getReportMsgs(outputDir);
				if(report.getReportMessage() != null && report.getReportMessage().size() > 0) {
					model.addAttribute("errorList", report.getReportMessage());
				}
            }
            return "validatedResult";
        } catch (IOException e) {
            e.printStackTrace();
        }
        String userPath = utilities.UPLOADED_FOLDER + req.getSession().getId() + "/";
        model.addAttribute("files", loadAll(Paths.get(userPath) ).map(
                path -> MvcUriComponentsBuilder.fromMethodName(ValidationXmlController.class,
                        "serveFile",  req.getSession().getId(), path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
        return "validateXml";
    }
    @RequestMapping(value="/downloadXML", method=RequestMethod.POST, params="download")
    public ResponseEntity<Resource> downloadXml(Model model,
             HttpServletRequest req) {
        Resource file;
		try {
			file = loadAsResource(utilities.UPLOADED_FOLDER + req.getSession().getId() +  File.separator + Utilities.REPORT_XML);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    	
    }
    @RequestMapping("/files/{directory:.+}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String directory, @PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(directory + File.separator + filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    public Resource loadAsResource(String filename) throws Exception {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new Exception("Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new Exception("Could not read file: " + filename, e);
        }
    }
    public Path load(String filename) {
        return Paths.get(utilities.UPLOADED_FOLDER).resolve(filename);
    }

    public Stream<Path> loadAll(Path root){
    	
    	try {
       		return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Throwable e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleStorageFileNotFound(Exception exc) {
        return ResponseEntity.notFound().build();
    }
}
