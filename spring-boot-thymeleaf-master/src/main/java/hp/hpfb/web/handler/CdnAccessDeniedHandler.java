package hp.hpfb.web.handler;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CdnAccessDeniedHandler implements AccessDeniedHandler {
	private static Logger logger = LogManager.getLogger(CdnAccessDeniedHandler.class);

	@Override
	public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			org.springframework.security.access.AccessDeniedException arg2) throws IOException, ServletException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("CdnAccessDeniedHandler handler");
		if (auth != null) {
			logger.info("User '" + auth.getName() + "' attempted to access the protected URL: "
					+ httpServletRequest.getRequestURI());
		}

		httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/error/403");
	}
	@PostConstruct
	public void afterInitialed() {
		logger.info("Initialed CdnAccessDeniedHandler");
	}

}
