package com.dellin.mondoc.config;

import com.dellin.mondoc.filter.CustomAuthenticationFilter;
import com.dellin.mondoc.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	@Qualifier("delegatedAuthenticationEntryPoint")
	final AuthenticationEntryPoint authEntryPoint;
	
	private final UserDetailsService userDetailsService;
	private final AuthenticationManagerBuilder authManagerBuilder;
	
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		CustomAuthenticationFilter customAuthenticationFilter =
				new CustomAuthenticationFilter(authManagerBuilder.getOrBuild());
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeHttpRequests((requests) -> requests.requestMatchers("api/login/**",
							"/api/token/refresh/**").permitAll()
					.requestMatchers("/session/**").permitAll()
					.requestMatchers(GET, "/api/user/**").hasAnyAuthority("ROLE_USER")
					.requestMatchers(POST, "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN")
					.requestMatchers(POST, "/api/role/**").hasAnyAuthority("ROLE_ADMIN")
					.anyRequest().authenticated()).exceptionHandling()
			.authenticationEntryPoint(authEntryPoint);
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(),
				UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
