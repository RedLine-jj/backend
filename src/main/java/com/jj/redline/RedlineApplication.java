package com.jj.redline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RedlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedlineApplication.class, args);
	}

}
