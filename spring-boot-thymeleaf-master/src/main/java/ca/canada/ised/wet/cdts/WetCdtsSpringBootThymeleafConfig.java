/**
 *
 */
package ca.canada.ised.wet.cdts;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * WetCdtsSpringBootThymeleafConfig.
 *
 * Leave creation of LocalChangeInterceptor up to the apps. WETTemplateInterceptor will be component scanned
 * automatically, no need for bean creation here.
 *
 * @author Andrew Pitt
 * @since 1.0.0-SNAPSHOT
 */
@EnableConfigurationProperties
@Configuration
@ComponentScan(basePackages = {"ca.canada.ised.wet.cdts.components","hp.hpfb.web"}, useDefaultFilters=true)
@PropertySource("classpath:application.properties")
public class WetCdtsSpringBootThymeleafConfig {
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		return propertySourcesPlaceholderConfigurer;
	}
}
