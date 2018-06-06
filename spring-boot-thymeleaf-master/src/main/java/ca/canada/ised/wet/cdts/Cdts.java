package ca.canada.ised.wet.cdts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableAutoConfiguration
//@EnableConfigurationProperties
//@Configuration
//@ComponentScan(basePackages = {"ca.canada.ised.wet.cdts.components","hp.hpfb.web"}, useDefaultFilters=true)
public class Cdts {

	public static void main(String[] args) {
		SpringApplication.run(Cdts.class, args);
	}
}
