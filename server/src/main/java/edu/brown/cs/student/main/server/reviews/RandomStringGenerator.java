package edu.brown.cs.student.main.server.reviews;

import java.security.SecureRandom;

public class RandomStringGenerator {

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int STRING_LENGTH = 16;

  public static String generateRandomString() {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(STRING_LENGTH);

    for (int i = 0; i < STRING_LENGTH; i++) {
      int randomIndex = random.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }

    return sb.toString();
  }
}
