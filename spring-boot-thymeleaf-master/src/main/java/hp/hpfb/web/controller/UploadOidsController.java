package hp.hpfb.web.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hp.hpfb.web.model.UserFile;
import hp.hpfb.web.service.utils.Utilities;

@Controller
public class UploadOidsController {
	@Autowired
	private Utilities utilities;
	@RequestMapping(value="/admin/loadOIDS", method=RequestMethod.GET)
    public String loadOids(Model model, HttpServletRequest req) throws Exception {
		model.addAttribute("userFile", new UserFile());
        model.addAttribute("files", null);
		return "uploadOIDS";
    }
	@RequestMapping(value="/admin/loadOIDS", method=RequestMethod.POST)
    public ResponseEntity<Object> saveOids(@RequestParam("files") MultipartFile file, Model model, HttpServletRequest req) throws Exception {
        String dir = utilities.OIDS_DIR;
        System.out.println("File name: " + file.getOriginalFilename());
        Path userPath = Paths.get(dir, file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        Files.write(userPath, bytes);

//        return new ResponseEntity<>("Invalid file.",HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(file.getOriginalFilename(),HttpStatus.OK);
    }
    public List<String> loadAll(String root){
		File dir = new File(root);
		String[] list = dir.list();
		if(list != null && list.length > 0) {
			return Arrays.stream(list).map(item -> "/spl-validator/admin/oidFile/".concat(item)).collect(Collectors.toList());
		} else {
			return null;
		}
    }

}
