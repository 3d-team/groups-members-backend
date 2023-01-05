package com.team3d.awad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.config.AppProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AwadApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwadApplication.class, args);
	}
}
