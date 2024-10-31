package de.bund.digitalservice.a2j.service.subscriber;

import dev.fitko.fitconnect.api.domain.validation.ValidationResult;
import dev.fitko.fitconnect.client.SenderClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class CallbackVerificationFilter extends OncePerRequestFilter {
  private final SenderClient senderClient;
  private final String callbackSecret;
  private final Logger logger = LoggerFactory.getLogger(CallbackVerificationFilter.class);

  public CallbackVerificationFilter(
      SenderClient senderClient, @Value("${fitConnect.callbackSecret}") String callbackSecret) {
    this.senderClient = senderClient;
    this.callbackSecret = callbackSecret;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().startsWith("/callbacks/fit-connect");
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain chain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    String requestBody = wrappedRequest.getReader().lines().collect(Collectors.joining("\n"));

    String hmac = request.getHeader("callback-authentication");

    ValidationResult result =
        senderClient.validateCallback(
            hmac,
            Long.parseLong(request.getHeader("callback-timestamp")),
            requestBody,
            callbackSecret);

    if (!result.isValid()) {
      logger.info("Received invalid fit-connect callback");
      logger.info("hmac: " + hmac);
      logger.info("body: " + requestBody);
      logger.info("Validation Error: " + result.getError().getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    chain.doFilter(request, response);
  }
}