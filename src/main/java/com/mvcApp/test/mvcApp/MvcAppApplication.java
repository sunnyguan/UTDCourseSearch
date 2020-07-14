package com.mvcApp.test.mvcApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.mvcApp.test.mvcApp.rest.HelloRestController;

@SpringBootApplication
public class MvcAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MvcAppApplication.class, args);
		HelloRestController.startup();
	}
	
	@Configuration
	public static class WebConfig extends WebMvcConfigurerAdapter {

		@Override
		public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
			configurer.setDefaultTimeout(-1);
			configurer.setTaskExecutor(asyncTaskExecutor());
		}
		
		@Bean
		public AsyncTaskExecutor asyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("async");
		}
		
	}
	
}
