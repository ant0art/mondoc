package com.dellin.mondoc.config;

import com.dellin.mondoc.filter.CustomAuthenticationFilter;
import com.dellin.mondoc.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final String[] SWAGGER_ENDPOINT =
			{"/**swagger**/**", "/swagger-resources", "/swagger-resources/**", "/v2/api-docs", "/swagger-ui/**", "/v3/api-docs/**"};
	private static final String[] WHITELIST =
			{"/h2-console/**/**", "/docs/**", "/csrf/**", "/webjars/**"};
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
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.authorizeRequests()
				.antMatchers("/api/login/**", "/api/token/refresh/**").permitAll();
		http.authorizeRequests()
				.antMatchers(SWAGGER_ENDPOINT).permitAll()
				.antMatchers("/sessions/**").authenticated()
				.antMatchers("/orders/all/**").authenticated()
				.antMatchers("/comments/**").authenticated()
				.antMatchers("/orders/update/**", "/orders/stopUpdate/**")
				 .hasAnyAuthority("ROLE_ADMIN")
				.antMatchers("/documents/**").hasAnyAuthority("ROLE_ADMIN")
				.antMatchers("/companies/**").hasAnyAuthority("ROLE_ADMIN");
		
		http.authorizeRequests()
				.antMatchers("/users/**").hasAnyAuthority("ROLE_ADMIN")
				.antMatchers(POST, "/roles/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests()
				.anyRequest().authenticated().and()
				.exceptionHandling().authenticationEntryPoint(authEntryPoint);
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(),
				UsernamePasswordAuthenticationFilter.class);
	}
	
	@Override
	public void configure(WebSecurity web) {
		web.ignoring()
				.antMatchers(WHITELIST)
				.antMatchers();
	}
}
