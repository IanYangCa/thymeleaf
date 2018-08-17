package hp.hpfb.web.handler;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

@Component
public class CdnSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    protected Logger logger = LogManager.getLogger(CdnSuccessHandler.class);
    
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
    	logger.info("onAuthenticationSuccess!!!!!!!!!!");
        handle(request, response, authentication);
        clearAuthenticationAttributes(request);
	}
	   protected void handle(HttpServletRequest request, 
			      HttpServletResponse response, Authentication authentication)
			      throws IOException {
			  
			        String targetUrl = determineTargetUrl(authentication);
			        if("admin".equals(targetUrl)) {
			        	DefaultSavedRequest obj = (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
			        	targetUrl = obj == null ? "/" : obj.getServletPath();
			        	logger.info("requesturi:" + targetUrl);
			        	if(obj != null) {
				        	logger.info("requesturl:" + obj.getRequestURL());
				        	logger.info("servlet path:" + obj.getServletPath());
				        	logger.info("pathInfo:" + obj.getPathInfo());
			        	}
//			        	System.out.println("requesturi:" + targetUrl);
//			        	System.out.println("requesturl:" + obj.getRequestURL());
//			        	System.out.println("servlet path:" + obj.getServletPath());
//			        	System.out.println("pathInfo:" + obj.getPathInfo());
			        }
			        if (response.isCommitted()) {
			            logger.debug(
			              "Response has already been committed. Unable to redirect to "
			              + targetUrl);
			            return;
			        }
			 
			        redirectStrategy.sendRedirect(request, response, targetUrl);
			    }
			 
			    protected String determineTargetUrl(Authentication authentication) {
			        boolean isUser = false;
			        boolean isAdmin = false;
			        Collection<? extends GrantedAuthority> authorities
			         = authentication.getAuthorities();
			        for (GrantedAuthority grantedAuthority : authorities) {
			            if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
			                isUser = true;
			                break;
			            } else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
			                isAdmin = true;
			                break;
			            }
			        }
			 
			        if (isUser) {
			            return "user";
			        } else if (isAdmin) {
			            return "admin";
			        } else {
			            throw new IllegalStateException();
			        }
			    }
			 
			    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
			        this.redirectStrategy = redirectStrategy;
			    }
			    protected RedirectStrategy getRedirectStrategy() {
			        return redirectStrategy;
			    }
				@PostConstruct
				public void afterInitialed() {
					logger.info("Initialed CdnSuccessHandler");
				}

}
