package hp.hpfb.web.configuration;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;

@Component
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
    protected Log logger = LogFactory.getLog(this.getClass());

	@PostConstruct
	public void initialized() {
		logger.info("Initialized SecurityWebApplicationInitializer");
	}
}
