package hp.hpfb.web.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hp.hpfb.web.model.UserFile;
import hp.hpfb.web.service.utils.Utilities;

@Controller
public class LoadStylesheetController {
	@Autowired
	private Utilities utilities;
	@RequestMapping(value="/admin/loadStylesheet", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req) throws Exception {
		model.addAttribute("userFile", new UserFile());
		return "loadStylesheet";
    }
	@RequestMapping(value="/admin/loadStylesheet", method=RequestMethod.POST)
    public String renderXml(Model model, @ModelAttribute UserFile userFile, HttpServletRequest req) throws Exception {
		String outputDir = utilities.LOCAL_XSLT_DIR + userFile.getVersion() + Utilities.FILE_SEPARATOR;
		File dir = new File(outputDir);
		if(! dir.exists()) {
			dir.mkdir();
		}
        String filename = outputDir + userFile.getFile().getOriginalFilename();
        utilities.backupFile(filename);
		Path path = Paths.get(filename);
        byte[] bytes = userFile.getFile().getBytes();
        Files.write(path, bytes, StandardOpenOption.CREATE);
        model.addAttribute("userFile", userFile);
		return "loadStylesheetDone";
    }
    @RequestMapping("/admin/stylesheet/{version:.+}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String version, @PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(version + Utilities.FILE_SEPARATOR + filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
