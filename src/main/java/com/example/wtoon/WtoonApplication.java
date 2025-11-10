package com.example.wtoon;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "Wtoon API", // Tiêu đề của API
				version = "1.0.0", // Phiên bản
				description = "API Backend cho dự án Webtoon Clone"
		)
)
public class WtoonApplication {

	public static void main(String[] args) {
		SpringApplication.run(WtoonApplication.class, args);
	}

}
