// package edu.brown.cs.student.moderator.StaffModeration;
//
// import edu.brown.cs.student.main.server.reviews.Review;
// import edu.brown.cs.student.main.server.storage.StorageInterface;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ExecutionException;
//
//// TODO: Consider adding synchronized for concurrency
//
// public class InMemoryReviewStorage implements StorageInterface {
//
//  private final Map<String, List<Review>> courseReviews;
//  private final List<Review> approvedReviews;
//
//  public InMemoryReviewStorage() {
//    this.courseReviews = new HashMap<>();
//    this.approvedReviews = new ArrayList<>();
//  }
//
//  @Override
//  public void addReview(String uid, String crn, Review review) {
//    courseReviews.computeIfAbsent(crn, k -> new ArrayList<>()).add(review);
//  }
//
//  // TODO: Change how delete review so not just based on userID
//  @Override
//  public void deleteReview(String crn, String userID) {
//    List<Review> reviews = courseReviews.get(crn);
//    if (reviews != null) {
//      reviews.removeIf(review -> review.uid.equals(userID));
//    }
//  }
//
//  @Override
//  public void changeKarma(String crn, String userID, boolean upvote)
//      throws ExecutionException, InterruptedException {
//    List<Review> reviews = courseReviews.get(crn);
//    if (reviews != null) {
//      for (Review review : reviews) {
//        if (review.uid.equals(userID)) {
//          review.karma += (upvote ? 1 : -1);
//          break;
//        }
//      }
//    }
//  }
//
//  @Override
//  public List<Review> getReviews(String crn) {
//    return courseReviews.getOrDefault(crn, new ArrayList<>());
//  }
//
//  @Override
//  public void clearUser(String userID) throws InterruptedException, ExecutionException {
//    for (List<Review> reviews : courseReviews.values()) {
//      reviews.removeIf(review -> review.uid.equals(userID));
//    }
//  }
//
//  public void approveReview(String crn, String userID) {
//    List<Review> reviews = courseReviews.get(crn);
//    if (reviews != null) {
//      for (Review review : reviews) {
//        if (review.uid.equals(userID) && !review.isApproved) {
//          review.setApproved(true);
//          approvedReviews.add(review); // Add to approved reviews list
//          break;
//        }
//      }
//    }
//  }
//
//  // Get approved reviews
//  public List<Review> getApprovedReviews() {
//    return approvedReviews;
//  }
// }
