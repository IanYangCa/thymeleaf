package hp.hpfb.web.configuration;

import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import ca.canada.ised.wet.cdts.components.wet.interceptor.WETLocaleChangeInterceptor;
import ca.canada.ised.wet.cdts.components.wet.interceptor.WETTemplateInterceptor;
import hp.hpfb.web.service.utils.Utilities;

/**
 * Web configuration.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	private static final Logger logger = LogManager.getLogger(WebConfig.class);

    /** The cdn template interceptor. */
    @Autowired
    private WETTemplateInterceptor cdnTemplateInterceptor;
    @Autowired
    private Utilities utilities;
    private static final String[] RESOURCE_LOCATIONS = {
    		"classpath:/META-INF/resources/", 
    		"classpath:/resources/", 
    		"classpath:/images/",
    		"classpath:/static/", 
    		"classpath:/public/" };
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	if (!registry.hasMappingForPattern("/**")) {
    		registry.addResourceHandler("/**").addResourceLocations(
    				RESOURCE_LOCATIONS);
    	}
    }
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.CANADA);
        return sessionLocaleResolver;
    }
    
    /** {@inheritDoc} */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(cdnTemplateInterceptor);
        registry.addInterceptor(new WETLocaleChangeInterceptor());
    }
    @Bean                           // bean for http session listener
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
        	// This method will be called when session created
        	@Override
            public void sessionCreated(HttpSessionEvent se) {               
        		logger.info("Session Created with session id+" + se.getSession().getId());
        		try {
	                File dir = new File(utilities.UPLOADED_FOLDER + se.getSession().getId());
	                if(! dir.exists()) {
	                	dir.mkdir();
	                }
        		}catch(NullPointerException e) {
        			logger.error("!!!!!!ERROR: Please check the directory: " + utilities.UPLOADED_FOLDER);
        		}
            }
        	// This method will be automatically called when session destroyed
            @Override
            public void sessionDestroyed(HttpSessionEvent se) {         
                logger.info("Session Destroyed, Session id:" + se.getSession().getId());
                //remove temporary directory
                File dir = new File(utilities.UPLOADED_FOLDER + se.getSession().getId());
                if(dir.exists() && dir.isDirectory()) {
                	utilities.removeDir(dir);
                }
            }
        };
    }
    @Bean                   // bean for http session attribute listener
    public HttpSessionAttributeListener httpSessionAttributeListener() {
        return new HttpSessionAttributeListener() {
        	// This method will be automatically called when session attribute added
        	@Override
            public void attributeAdded(HttpSessionBindingEvent se) {            
        		logger.info("Attribute Added following information");
        		logger.info("Attribute Name:" + se.getName());
        		logger.info("Attribute Value:" + se.getName());
            }
        	// This method will be automatically called when session attribute removed
            @Override
            public void attributeRemoved(HttpSessionBindingEvent se) {      
            	logger.info("attributeRemoved");
            	logger.info("Attribute Name:" + se.getName());
            }
            // This method will be automatically called when session attribute replace
            @Override
            public void attributeReplaced(HttpSessionBindingEvent se) {     
            	logger.info("Attribute Replaced following information");
            	logger.info("Attribute Name:" + se.getName());
            	logger.info("Attribute Old Value:" + se.getValue());
            }
        };
    }
}