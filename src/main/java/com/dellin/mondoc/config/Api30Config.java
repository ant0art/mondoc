package com.dellin.mondoc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Monitoring.Doc", version = "v1"))
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP,
				bearerFormat = "JWT", scheme = "bearer")
public class Api30Config {}
