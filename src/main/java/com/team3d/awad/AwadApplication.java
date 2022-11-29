package com.team3d.awad;

import com.team3d.awad.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AwadApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwadApplication.class, args);
	}

}
