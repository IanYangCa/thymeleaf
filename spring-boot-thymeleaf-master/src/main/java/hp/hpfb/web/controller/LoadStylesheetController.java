package hp.hpfb.web.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import hp.hpfb.web.service.utils.Utilities;

@Controller
public class LoadStylesheetController {
	
	private static final Logger logger = LogManager.getLogger(LoadStylesheetController.class);

	@Autowired
	private Utilities utilities;
	@RequestMapping(value="/admin/loadStylesheet", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req) throws Exception {
		return "loadStylesheet";
    }
	@RequestMapping(value="/admin/loadStylesheet", method=RequestMethod.POST)
    public ResponseEntity<Object> saveStylesheet(@RequestParam("version") String version, @RequestParam("files") MultipartFile file, Model model, HttpServletRequest req) throws Exception {
		if(StringUtils.isEmpty(version)) {
			return new ResponseEntity<>("Error: Veresion is Required!",HttpStatus.OK);
		}
		String outputDir = utilities.LOCAL_XSLT_DIR + version + Utilities.FILE_SEPARATOR;
		File dir = new File(outputDir);
		if(! dir.exists()) {
			dir.mkdir();
		}
        logger.info("File name: " + file.getOriginalFilename());
        Path userPath = Paths.get(outputDir, file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        Files.write(userPath, bytes);
		return new ResponseEntity<>(file.getOriginalFilename(),HttpStatus.OK);
    }
    @RequestMapping("/admin/stylesheet/{version:.+}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String version, @PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(version + Utilities.FILE_SEPARATOR + filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			logger.error("Error(In LoadStylesheetController): Exception:" + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
		}
		return null;
    }
    @RequestMapping("/admin/stylesheet/{version:.+}")
    @ResponseBody
    public List<String> getFiles(@PathVariable String version) {
		try {
			File dir = new File(utilities.LOCAL_XSLT_DIR + version);
			if(dir.isDirectory()) {
				return Arrays.stream(dir.list()).map(item -> "/admin/stylesheet/".concat(version).concat("/").concat(item)).collect(Collectors.toList()); 
			}
	        return null;
		} catch (Exception e) {
			logger.error("Error(In LoadStylesheetController): Exception:" + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
		}
		return null;
    }
    
    public List<String> loadAll(String root){
    	
    	try {
    		File dir = new File(root);
    		String[] list = dir.list(new FilenameFilter() {
    			@Override
    			public boolean accept(File dir, String name) {
    				return name.startsWith("business", 0);
    			}
    		});
    		return Arrays.stream(list).map(item -> "/stylesheet/".concat(item)).collect(Collectors.toList());
		} catch(Throwable e) {
			logger.error("Error(In LoadStylesheetController): Exception:" + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
		}
    	return null;
    }
    public Resource loadAsResource(String filename) throws Exception {
        try {
            Path file = load( utilities.LOCAL_XSLT_DIR + filename);
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
        return Paths.get(utilities.SRC_RULES_DIR).resolve(filename);
    }

}
