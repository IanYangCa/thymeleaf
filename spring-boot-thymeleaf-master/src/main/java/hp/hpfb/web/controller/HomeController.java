package hp.hpfb.web.controller;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.BreadcrumbService;
import ca.canada.ised.wet.cdts.components.wet.sidemenu.SideMenuConfig;

@Controller
public class HomeController {
	
		private static String VIEW_NAME = "hpfb.apps";
		@Autowired
		private BreadcrumbService bs;
		@Autowired
		SideMenuConfig menu;
//		@Autowired
//		private ResourceBundleMessageSource messageSource;
	    @RequestMapping("/home")
	    public String greeting(String name, Model model, HttpServletRequest req) throws Exception {
	    	bs.buildBreadCrumbs(VIEW_NAME, "");//req.getRequestURL().toString());
	        return "home";
	    }
	    @RequestMapping("/leftMenu")
	    public String leftMenu(String name, Model model, HttpServletRequest req) throws Exception {
	        return "leftMenu";
	    }

}
