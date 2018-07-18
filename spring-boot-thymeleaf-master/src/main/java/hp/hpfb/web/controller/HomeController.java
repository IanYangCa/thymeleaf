package hp.hpfb.web.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import hp.hpfb.web.model.User;

@Controller
public class HomeController {
	
	    @RequestMapping({"/home"})
	    public String greeting(String name, Model model, HttpServletRequest req) throws Exception {
	        return "home";
	    }
	    @RequestMapping({"/","/leftMenu"})
	    public String leftMenu(String name, Model model, HttpServletRequest req) throws Exception {
	        return "leftMenu";
	    }

	    @RequestMapping("/error/403")
	    public String error403() {
	        return "error/403";
	    }
	    @RequestMapping("/login")
	    public String login(Model model, HttpServletRequest req) {
	    	String logout = req.getParameter("logout");
	    	model.addAttribute("user", new User());
	    	if(logout == null) {
		        return "login";
	    	} else {
		        return "leftMenu";
	    	}
	    }
	    @RequestMapping(value="/login", method=RequestMethod.POST)
	    public String postLogin(Model model, @ModelAttribute User user, HttpServletRequest req, HttpServletResponse res) {
	    	System.out.println("login!!");
	        return "login";
	    }
	    @RequestMapping(value="/logout", method=RequestMethod.GET)
	    public String logout() {
	        return "redirect:/leftMenu";
	    }

}
