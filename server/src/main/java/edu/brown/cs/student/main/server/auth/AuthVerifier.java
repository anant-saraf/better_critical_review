package edu.brown.cs.student.main.server.auth;

import spark.Request;

public interface AuthVerifier {
  String getAuthenticatedUser(Request request) throws IllegalAccessException;
}
