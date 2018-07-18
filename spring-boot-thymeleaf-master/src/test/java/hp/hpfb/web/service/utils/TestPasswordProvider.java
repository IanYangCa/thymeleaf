package hp.hpfb.web.service.utils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import ca.canada.ised.wet.cdts.components.wet.breadcrumbs.AbstractMockMvcTest;

public class TestPasswordProvider extends AbstractMockMvcTest {
	@Autowired
	private CdnPasswordProvider provider;
	@Value("${admin.user}")
	public String username;
	@Value("${admin.password}")
	public String password;
	
	@Test
	public void testEncoder() {
		String pwd = provider.encode("password");
		System.out.println("pwd: " + pwd);
		System.out.println("is valid: " + provider.isValid(pwd, "password"));
		System.out.println("properties pwd is valid: " + provider.isValid(password, "password"));
		Authentication auth = provider.authenticate(new UsernamePasswordAuthenticationToken("admin", "password"));
		if(auth != null) {
			System.out.println("user: " + auth.getName());
		} else {
			System.out.println("admin Failed! ");
		}
		auth = provider.authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
		if(auth != null) {
			System.out.println("user: " + auth.getName());
		} else {
			System.out.println("user Failed! ");
		}
	}

}
