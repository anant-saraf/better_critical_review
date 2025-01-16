package edu.brown.cs.student.main.server.reviews;

public enum ReviewStatus {
  PUBLISHED("Published"),
  PENDING("Pending"),
  REVIEW_NOW("Review Now");

  private final String status;

  // Constructor
  ReviewStatus(String status) {
    this.status = status;
  }

  // Getter
  public String getStatus() {
    return status;
  }

  // Convert String to enum value
  public static ReviewStatus fromString(String status) {
    for (ReviewStatus reviewStatus : ReviewStatus.values()) {
      if (reviewStatus.status.equalsIgnoreCase(status)) {
        return reviewStatus;
      }
    }
    throw new IllegalArgumentException("Unknown review status: " + status);
  }
}
