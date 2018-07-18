package hp.hpfb.web.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class CdnPasswordProvider implements AuthenticationProvider {
	private static String salt = "hc.hcfb.application";
	private static ShaPasswordEncoder encoder = new ShaPasswordEncoder(512);
	private static List<UsernamePasswordAuthenticationToken> users = new ArrayList<UsernamePasswordAuthenticationToken>();
	@Value("${admin.user}")
	public String username;
	@Value("${admin.password}")
	public String password;

	public String encode(String rawPassword) {
		String crypt = encoder.encodePassword(rawPassword, salt);
        return crypt;
	}
	public boolean isValid(String encpwd, String rawPassword) {
		boolean crypt = encoder.isPasswordValid(encpwd, rawPassword, salt);
        return crypt;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		Object credentials = authentication.getCredentials();
		if(!(String.class.isInstance(credentials))) {
			return null;
		}
		String pwd = credentials.toString();
		Optional<UsernamePasswordAuthenticationToken> user = users.stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst();
		if( user != null 
				&& user.isPresent() 
				&& user.get() != null 
				&& encoder.isPasswordValid(user.get().getCredentials().toString(), pwd, salt)) {
			return new UsernamePasswordAuthenticationToken(name, user.get().getCredentials(), AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN"));
		} else {
			return null;
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
	@PostConstruct
	public void initialUsers() {
		users.add(new UsernamePasswordAuthenticationToken(username, password, AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN")));
	}
	

}
