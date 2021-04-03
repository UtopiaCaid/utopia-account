package com.caid.utopia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.caid.utopia")
public class UtopiaAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtopiaAccountApplication.class, args);
	}

}
