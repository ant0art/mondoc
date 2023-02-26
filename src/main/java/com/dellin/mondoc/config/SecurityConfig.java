package com.dellin.mondoc.config;

import com.dellin.mondoc.filter.CustomAuthenticationFilter;
import com.dellin.mondoc.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Qualifier("delegatedAuthenticationEntryPoint")
	final AuthenticationEntryPoint authEntryPoint;
	
	private final UserDetailsService userDetailsService;
	
	private final BCryptPasswordEncoder encoder;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		CustomAuthenticationFilter customAuthenticationFilter =
				new CustomAuthenticationFilter(authenticationManager());
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.authorizeRequests()
				.antMatchers("/api/login/**", "/api/token/refresh/**").permitAll();
		http.authorizeRequests()
				.antMatchers("/sessions/**").permitAll()
				.antMatchers("/comments/**").permitAll()
				.antMatchers(POST, "/orders/**").permitAll();
		
		http.authorizeRequests()
				.antMatchers(GET, "/api/user/**").hasAnyAuthority("ROLE_USER");
		http.authorizeRequests()
				.antMatchers(POST, "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests()
				.antMatchers(POST, "/api/role/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests()
				.anyRequest().authenticated().and()
				.exceptionHandling().authenticationEntryPoint(authEntryPoint);
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(),
				UsernamePasswordAuthenticationFilter.class);
	}
}
