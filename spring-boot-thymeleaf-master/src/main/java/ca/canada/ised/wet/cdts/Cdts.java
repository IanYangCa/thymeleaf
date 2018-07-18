package ca.canada.ised.wet.cdts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import hp.hpfb.web.service.utils.Utilities;

@SpringBootApplication
@EnableAutoConfiguration
public class Cdts {

	public static void main(String[] args) {
		SpringApplication.run(Cdts.class, args);
	}
}
