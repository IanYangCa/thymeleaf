package hp.hpfb.web.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ca.canada.ised.wet.cdts.components.wet.sidemenu.MenuLink;
import ca.canada.ised.wet.cdts.components.wet.sidemenu.SectionMenu;
import ca.canada.ised.wet.cdts.components.wet.sidemenu.SideMenuConfig;

@Component
public class SideMenuHandler {

	@Autowired
    private SideMenuConfig sideMenuConfig;
    public List<SectionMenu> getSectionMenuList() {
    	List<SectionMenu> menuList = sideMenuConfig.getSectionMenuList();
		checkMenuLink(menuList);
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	if(auth != null && auth.isAuthenticated() && ! "anonymousUser".equals(auth.getName())) {
    		return menuList;
    	} else {
    		List<SectionMenu> tempList = new ArrayList<SectionMenu>(menuList.size());
    		SectionMenu temp = null;
    		MenuLink link = null;
    		for(SectionMenu main : menuList) {
    			temp = new SectionMenu();
    			BeanUtils.copyProperties(main, temp, "menuLinks");
    			temp.setMenuLinks(new ArrayList<MenuLink>());
    			tempList.add(temp);
    			for(MenuLink sub : main.getMenuLinks()) {
    				if(! sub.isAdmin()) {
        				link = new MenuLink();
        				BeanUtils.copyProperties(sub, link);
        				temp.getMenuLinks().add(link);
    				}
    			}
    		}
    		return tempList;
    	}
    }
    private void checkMenuLink(List<SectionMenu> menus) {
		for(SectionMenu main : menus) {
			for(MenuLink sub : main.getMenuLinks()) {
				if(! sub.getHref().startsWith("/spl-validator")) {
					sub.setHref("/spl-validator" + sub.getHref());
				} else {
					return ;
				}
			}
		}

    }
}
