package com.mvcApp.test.mvcApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import com.mvcApp.test.mvcApp.rest.HelloRestController;

@SpringBootApplication
public class MvcAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcAppApplication.class, args);
		HelloRestController.startup();
	}

}
