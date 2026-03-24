package com.union.union;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
public class UnionApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnionApplication.class, args);
	}

}
