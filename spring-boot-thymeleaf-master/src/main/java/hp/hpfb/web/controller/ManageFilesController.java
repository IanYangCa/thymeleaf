package hp.hpfb.web.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import hp.hpfb.web.model.UserFile;
import hp.hpfb.web.service.utils.Utilities;

@Controller
public class LoggerFilesController {
	private static Logger logger = LogManager.getLogger(Utilities.class);

	@RequestMapping(value="/admin/logfiles", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req){
		model.addAttribute("userFile", new UserFile());
      String userPath = "../logs"; //getLogsRoot("../logs");
      try {
    	  model.addAttribute("logDir", userPath);
    	  model.addAttribute("files", loadAll(userPath));
		  return "logfiles";
      } catch(Exception e) {
			model.addAttribute("errorMsg",  "Errors:\n" + e.getClass().getSimpleName() + "\n" + StringUtils.join(e.getStackTrace(), "\n"));
			return "error";
      }
    }
    public List<String> loadAll(String root){
    	
		File dir = new File(root);
		if(dir != null && dir.isDirectory()) {
			String[] list = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".log");
				}
			});
			if(list != null && list.length > 0) {
				return Arrays.stream(list).map(item -> "/spl-validator/admin/logfile/".concat(item)).collect(Collectors.toList());
			}
		} else {
			logger.info("Error: Dir(" + dir + ") is not directory!");
			System.out.println("Error: Dir(" + dir + ") is not directory!");
		}
		return null;
    }
}
