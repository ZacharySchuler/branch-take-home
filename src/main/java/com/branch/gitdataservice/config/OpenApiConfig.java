package com.branch.gitdataservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenApi(
			@Value("${app.title}") String title,
			@Value("${app.version}") String version
	) {
		return new OpenAPI().info(
				new Info()
						.title(title)
						.version(version)
						.description("A service to fetch a users git repository data")
		);
	}
}
