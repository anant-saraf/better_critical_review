package edu.brown.cs.student.main.server.auth;

import com.clerk.backend_api.helpers.jwks.AuthenticateRequest;
import com.clerk.backend_api.helpers.jwks.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.jwks.RequestState;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashSet;
import java.util.Optional;
import spark.Request;

public class ClerkAuthVerifier implements AuthVerifier {
  private final String jwtKey;

  public ClerkAuthVerifier(String jwtKey) {
    this.jwtKey = jwtKey;
  }

  @Override
  public String getAuthenticatedUser(Request request) throws IllegalAccessException {
    // Build a HttpRequest because the Clerk API doesn't recognize Spark's Requests - only headers
    // are important
    String authHeader = request.headers("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalAccessException("Authentication token not provided");
    }
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .setHeader("Authorization", authHeader)
            .uri(URI.create("http://localhost"))
            .build();

    RequestState authState =
        AuthenticateRequest.authenticateRequest(
            httpRequest,
            new AuthenticateRequestOptions(
                Optional.empty(),
                Optional.of(jwtKey),
                Optional.empty(),
                new HashSet<>(),
                Optional.empty()));

    if (!authState.isSignedIn()
        || authState.claims().isEmpty()
        || authState.claims().get().getSubject() == null
        || authState.claims().get().getSubject().isBlank()) {
      throw new IllegalAccessException("Authentication token is invalid.");
    }

    return authState.claims().get().getSubject();
  }
}
