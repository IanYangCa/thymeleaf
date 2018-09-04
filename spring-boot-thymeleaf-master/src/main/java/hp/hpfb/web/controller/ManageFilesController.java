package hp.hpfb.web.controller;

import java.io.File;
import java.net.MalformedURLException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hp.hpfb.web.service.utils.Utilities;

@Controller
public class ManageFilesController {
	private static Logger logger = LogManager.getLogger(Utilities.class);
	@Autowired
	private Utilities utilities;

	@RequestMapping(value="/admin/manageFiles", method=RequestMethod.GET)
    public String manageFiles(Model model, HttpServletRequest req){
		  return "manageFiles";
    }
    @RequestMapping("/admin/manageFiles/{sourceDir:.+}")
    @ResponseBody
    public List<String> listFiles(@PathVariable int sourceDir, HttpServletRequest req) {
        String root = getSourceDir(sourceDir, req.getSession().getId());
		try {
			File dir = new File(root);
			if(dir.isDirectory()) {
				return Arrays.stream(dir.list()).map(item -> "/admin/manageFile/".concat("" + sourceDir).concat("/").concat(item)).collect(Collectors.toList()); 
			}
	        return null;
		} catch (Exception e) {
			logger.error("Error(In LoadStylesheetController): Exception:" + e.getClass().getName() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
		}
		return null;
    }
    @RequestMapping("/admin/manageFile/{sourceDir:.+}/{filename:.+}/{action:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable int sourceDir, @PathVariable String filename, @PathVariable String action, HttpServletRequest req) {
        Resource res;
        String root = getSourceDir(sourceDir, req.getSession().getId());
		try {
			if("delete".equalsIgnoreCase(action)) {
				File file = new File(root + filename);
				if(file != null && file.exists()) {
					file.delete();
				}
			} else {
				res = loadAsResource(filename, root);
		        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + res.getFilename() + "\"").body(res);
			}
		} catch (Exception e) {
			logger.error("Error in LoadBusinessRuleController: " + StringUtils.join(e.getStackTrace(), "\n"));
		}
		return null;
    }
    public Resource loadAsResource(String filename, String root) throws Exception {
        try {
            Path file = load(root + filename);
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
    public List<String> loadAll(int sourceDir, String sessionId){
    	String root = getSourceDir(sourceDir, sessionId);
		File dir = new File(root);
		if(dir != null && dir.isDirectory()) {
			String[] list = dir.list();
			String path = "/admin/manageFile/" + sourceDir + Utilities.FILE_SEPARATOR;
			if(list != null && list.length > 0) {
				return Arrays.stream(list).map(item -> path.concat(item)).collect(Collectors.toList());
			}
		} else {
			logger.info("Error: Dir(" + dir + ") is not directory!");
			System.out.println("Error: Dir(" + dir + ") is not directory!");
		}
		return null;
    }
    private String getSourceDir(int sourceDir, String sessionId) {
    	String root = "";
		switch(sourceDir) {
		case 0:
			root = utilities.UPLOADED_FOLDER + sessionId + Utilities.FILE_SEPARATOR;
			break;
		case 1:
			root = utilities.SRC_RULES_DIR;
			break;
		case 2:
			root = utilities.DEST_RULE_DIR;
			break;
		case 3:
			root = utilities.OIDS_DIR;
			break;
		}
		return root;
    	
    }
}
