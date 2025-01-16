package edu.brown.cs.student.main.server.auth;

import spark.Request;

public class MockAuthVerifier implements AuthVerifier {
  private final String username;

  public MockAuthVerifier(String username) {
    this.username = username;
  }

  @Override
  public String getAuthenticatedUser(Request request) {
    return username;
  }
}
