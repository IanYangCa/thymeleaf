package hp.hpfb.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import hp.hpfb.web.model.UserFile;

@Controller
public class LoadBusineseRuleController {
	@RequestMapping(value="/admin/loadBusineseRule", method=RequestMethod.GET)
    public String renderXml(Model model, HttpServletRequest req) throws Exception {
		model.addAttribute("userFile", new UserFile());
		return "loadBusineseRule";
    }

}
