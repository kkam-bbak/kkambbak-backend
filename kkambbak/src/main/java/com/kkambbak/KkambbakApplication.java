package com.kkambbak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KkambbakApplication {

	public static void main(String[] args) {
		SpringApplication.run(KkambbakApplication.class, args);
	}

}
