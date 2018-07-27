package hp.hpfb.web.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;
import ca.canada.ised.wet.cdts.components.wet.sidemenu.SectionMenu;
import hp.hpfb.web.handler.SideMenuHandler;

public class TestSideMenu extends AbstractMockMvcTest {
	@Autowired
	private SideMenuHandler handler;
	private Authentication auth;
	@Before
	public void before() {
		auth = Mockito.mock(Authentication.class);
		auth.setAuthenticated(true);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
	}
	@Test
	public void testSideMenuHandler() {
		Mockito.when(auth.isAuthenticated()).thenReturn(false);
		List<SectionMenu> list = handler.getSectionMenuList();
		System.out.println("Is not Admin");
		for(SectionMenu p : list) {
			System.out.println(p.getSectionNameEn());
			p.getMenuLinks().forEach((pp) -> System.out.println(pp.getHref()));
		}
		Mockito.when(auth.isAuthenticated()).thenReturn(true);
		list = handler.getSectionMenuList();
		System.out.println("Is Admin");
		for(SectionMenu p  : list) {
			System.out.println(p.getSectionNameEn());
			p.getMenuLinks().forEach((pp) -> System.out.println(pp.getHref()));
		}
		
	}

}
