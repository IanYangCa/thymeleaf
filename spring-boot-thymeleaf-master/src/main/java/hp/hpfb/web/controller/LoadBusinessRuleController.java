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
public class LoadBusinessRuleController {
	@Autowired
	private Utilities utilities;
	@RequestMapping(value="/admin/loadBusinessRule", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req) throws Exception {
		model.addAttribute("userFile", new UserFile());
      String userPath = utilities.SRC_RULES_DIR;
      model.addAttribute("files", loadAll(userPath));
		return "loadBusinessRule";
    }
	@RequestMapping(value="/admin/loadBusinessRule", method=RequestMethod.POST)
    public String renderXml(Model model, @ModelAttribute UserFile userFile, HttpServletRequest req) throws Exception {
		String outputDir = utilities.SRC_RULES_DIR;
		File dir = new File(outputDir);
		if(dir == null || ! dir.exists()) {
			dir.mkdir();
		}
        String filename = outputDir + userFile.getFile().getOriginalFilename();
        utilities.backupFile(filename);
		Path path = Paths.get(filename);
        byte[] bytes = userFile.getFile().getBytes();
        Files.write(path, bytes, StandardOpenOption.CREATE);
        model.addAttribute("userFile", userFile);
		return "loadBusinessRuleDone";
    }
    @RequestMapping("/admin/businessRule/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
    		return Arrays.stream(list).map(item -> "/admin/businessRule/".concat(item)).collect(Collectors.toList());
		} catch(Throwable e) {
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
        return Paths.get(utilities.SRC_RULES_DIR).resolve(filename);
    }

}
