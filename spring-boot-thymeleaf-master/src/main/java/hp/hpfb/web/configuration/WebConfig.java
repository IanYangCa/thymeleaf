package hp.hpfb.web.configuration;

import java.util.Locale;

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

/**
 * Web configuration.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    /** The cdn template interceptor. */
    @Autowired
    private WETTemplateInterceptor cdnTemplateInterceptor;
    private static final String[] RESOURCE_LOCATIONS = {
    		"classpath:/META-INF/resources/", "classpath:/resources/",
    		"classpath:/static/", "classpath:/public/" };
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    	if (!registry.hasMappingForPattern("/webjars/**")) {
//    		registry.addResourceHandler("/webjars/**").addResourceLocations(
//    				"classpath:/META-INF/resources/webjars/");
//    	}
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
}