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
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import hp.hpfb.web.exception.SplException;
import hp.hpfb.web.model.Errors;
import hp.hpfb.web.model.FailedAssert;
import hp.hpfb.web.model.Parameters;
import hp.hpfb.web.model.Report;
import hp.hpfb.web.model.ReportMessage;
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
        	//retrieve parameters from xml file to properties.xml
        	try {
//				utilities.renderXml(utilities.SRC_RULES_DIR + Utilities.PROPERTITIES + Utilities.XSLT, filename, outputDir + Utilities.PROPERTITIES + Utilities.XML, null);
				utilities.generateProperties(filename, utilities.SRC_RULES_DIR, outputDir);
			} catch (SplException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (RuntimeException e1 ) {
				e1.printStackTrace();
			}
        	Boolean hasException = false;
        	Parameters p = utilities.getParameters(outputDir);
        	Map<String, String> params = new HashMap<String, String>();
        	
        	params.put("display-language",  p.getDisplayLanguage());
        	params.put("oid_loc", utilities.OIDS_DIR);
        	params.put("id",  file.getOriginalFilename());
        	params.put("property-file", outputDir + Utilities.PROPERTITIES + Utilities.XML);
        	params.put("rule-file",  utilities.SRC_RULES_DIR + "hc-rules" + Utilities.XML);
        	try {
				errors = service.verifyXml(filename);
			} catch (SAXException e) {
				hasException = false;
				errors = new ArrayList<String>(1);
				errors.add("Bad XML Format!");
				errors.add("Read XML File:Parse Exception:Bad XML Format!");
			} catch (RuntimeException e) {
				hasException = false;
				errors = new ArrayList<String>(1);
				errors.add("Bad XML Format!");
				errors.add("Read XML File:Parse Exception:Bad XML Format!");
			} catch (SplException e) {
				System.out.println("SplException: " + e.getErrorMsg());
				hasException = true;
				errors = new ArrayList<String>(1);
				errors.add("Bad XML Format!");
        		Errors errs = new Errors();
        		errs.setFailedAssert(new FailedAssert());
        		String[] msgs = StringUtils.split(e.getErrorMsg(), ':');
        		errs.getFailedAssert().setFlag(msgs[0]);
        		errs.getFailedAssert().setId(msgs[1]);
        		errs.getFailedAssert().setTest(msgs[2]);
        		errs.getFailedAssert().setLocation(msgs[3]);
        		if(msgs.length > 4) {
        			errs.getFailedAssert().setText(msgs[4]);
        			for(int i= 5; i < msgs.length; i++) {
                		errs.getFailedAssert().setText(errs.getFailedAssert().getText() + ":" + msgs[i]);
        			}
        		}
                utilities.writeSchemaErrorToReport0(outputDir, errs);
				try {
					utilities.renderXml(utilities.SRC_RULES_DIR + "report.xslt", outputDir + "report0.xml", outputDir + "report.xml", params );
				} catch (SplException e1) {
					e1.printStackTrace();
				}
			}
            if( errors.size() > 0 ) {
            	if(! hasException) {
	            	List<ReportMessage> reports = utilities.buildSchemaErrorReport(errors, file.getOriginalFilename(), outputDir);
	                utilities.writeSchemaErrorToReport(outputDir, reports);
            	}
				Report report = utilities.getReportMsgs(outputDir);
				if(report.getReportMessage() != null && report.getReportMessage().size() > 0) {
					model.addAttribute("errorList", report.getReportMessage());
				}
            } else {
            	try {
	            	//build validate business rules
	            	utilities.rebuildBusinessRule();
	            	
	            	utilities.renderXml(utilities.SRC_RULES_DIR + "stripVestiges.xslt", filename, outputDir + "strip.xml", null);
	            	
	            	logger.info("oid_loc:" + utilities.OIDS_DIR);
	            	utilities.renderXml(utilities.DEST_RULE_DIR + Utilities.TARGET_BUSINESS_RULE_FILE + Utilities.XSLT, outputDir + "strip.xml", outputDir + "report0.xml", params);
					utilities.renderXml(utilities.SRC_RULES_DIR + "report.xslt", outputDir + "report0.xml", outputDir + "report.xml", params );
            	} catch ( SplException e) {
            		Errors errs = new Errors();
            		errs.setFailedAssert(new FailedAssert());
            		String[] msgs = StringUtils.split(e.getErrorMsg(), ':');
            		errs.getFailedAssert().setFlag(msgs[0]);
            		errs.getFailedAssert().setId(msgs[1]);
                    utilities.writeSchemaErrorToReport0(outputDir, errs);
					try {
						utilities.renderXml(utilities.SRC_RULES_DIR + "report.xslt", outputDir + "report0.xml", outputDir + "report.xml", params );
					} catch (SplException e1) {
						e1.printStackTrace();
					}
            	} catch(Exception e) {
            		logger.error("Other Exception!" + e.getClass().getName());
            	}
				Report report = utilities.getReportMsgs(outputDir);
				if(report.getReportMessage() != null && report.getReportMessage().size() > 0) {
					model.addAttribute("errorList", report.getReportMessage());
				}
				
            }
            return "validatedResult";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

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
