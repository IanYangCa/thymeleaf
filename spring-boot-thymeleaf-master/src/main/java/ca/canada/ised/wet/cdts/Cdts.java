package ca.canada.ised.wet.cdts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

import hp.hpfb.web.configuration.SpringSecurityConfig;

//@SpringBootApplication
@Configuration
@EnableAutoConfiguration
//public class Cdts {
//
//	public static void main(String[] args) {
//		SpringApplication.run(Cdts.class, args);
//	}
//}
public class Cdts extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(new Object[]{Cdts.class, SpringSecurityConfig.class});
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Cdts.class, args);
    }

}