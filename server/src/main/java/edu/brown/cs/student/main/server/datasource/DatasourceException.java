package edu.brown.cs.student.main.server.datasource;

/** Exception thrown due to missing entries in the datasource. */
public class DatasourceException extends RuntimeException {

  public DatasourceException(String message) {
    super(message);
  }
}
