package de.bund.digitalservice.a2j.config;

import de.bund.digitalservice.a2j.service.receiver.verification.CallbackVerificationFilter;
import de.bund.digitalservice.a2j.service.receiver.verification.CallbackVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final CallbackVerificationService callbackVerificationService;

  public SecurityConfig(CallbackVerificationService callbackVerificationService) {
    this.callbackVerificationService = callbackVerificationService;
  }

  @Bean
  public SecurityFilterChain springSecurityWebFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(
            csrf -> csrf.ignoringRequestMatchers("/api/sender/submit", "/callbacks/fit-connect"))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers("/.well-known/security.txt")
                    .permitAll()
                    .requestMatchers("/actuator/health/readiness", "/actuator/health/liveness")
                    .permitAll()
                    .requestMatchers("/api/sender/**")
                    .permitAll()
                    .requestMatchers("/callbacks/fit-connect")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .addFilterAfter(
            new CallbackVerificationFilter(callbackVerificationService),
            BasicAuthenticationFilter.class)
        .build();
  }

  @Bean
  public UserDetailsService noOpUserDetailsService() {
    return username -> null;
  }
}
