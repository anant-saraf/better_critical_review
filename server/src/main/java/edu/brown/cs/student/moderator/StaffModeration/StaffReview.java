package edu.brown.cs.student.moderator.StaffModeration;

import edu.brown.cs.student.main.server.reviews.Review;
import java.util.ArrayList;
import java.util.List;

public class StaffReview {
  private List<Review> reviews;

  public StaffReview() {
    this.reviews = new ArrayList<>();
  }

  public List<Review> getReviews() {
    return reviews;
  }

  public void addReview(Review review) {
    this.reviews.add(review);
  }

  public void updateApprovalStatus(int index, boolean isApproved) {
    if (index >= 0 && index < reviews.size()) {
      reviews.get(index).setApproved(isApproved);
    }
  }
}
