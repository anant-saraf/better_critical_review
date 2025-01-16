package edu.brown.cs.student.main.server.storage;

import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.reviews.ReviewStatus;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface StorageInterface {
  void addReview(String semestercrn, String uniqueID, Review review)
      throws ExecutionException, InterruptedException, IllegalAccessException;

  void deleteReview(String crn, String uniqueID) throws ExecutionException, InterruptedException;

  long changeKarma(String semestercrn, String uniqueID, String voterUserID, boolean upvote)
      throws ExecutionException, InterruptedException;

  List<Review> getReviews(String crn) throws InterruptedException, ExecutionException;

  void addCourse(String userId, String semesterCrn) throws ExecutionException, InterruptedException;

  Map<String, ReviewStatus> getUserCourses(String userId)
      throws ExecutionException, InterruptedException;

  void moderateReview(String reviewId, boolean approve)
      throws ExecutionException, InterruptedException, IllegalAccessException;

  List<Review> getUnmoderatedReviews() throws ExecutionException, InterruptedException;
}
