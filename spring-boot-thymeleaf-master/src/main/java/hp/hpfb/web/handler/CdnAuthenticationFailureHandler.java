package hp.hpfb.web.handler;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CdnAuthenticationFailureHandler implements AuthenticationFailureHandler {
	private static Logger logger = LoggerFactory.getLogger(CdnAuthenticationFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		logger.info("Authentication Failure!!!!\n" + StringUtils.join(exception.getStackTrace(), '\n'));
	
	}
	@PostConstruct
	public void afterInitialed() {
		logger.info("Initialed CdnAuthenticationFailureHandler");
	}

}
