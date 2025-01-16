// package edu.brown.cs.student.moderator;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// import edu.brown.cs.student.main.server.reviews.Review;
// import edu.brown.cs.student.moderator.StaffModeration.InMemoryReviewStorage;
// import java.util.List;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// public class InMemoryReviewStorageTest {
//
//  private InMemoryReviewStorage reviewStorage;
//  private Review review1;
//  private Review review2;
//
//  @BeforeEach
//  public void setUp() {
//    reviewStorage = new InMemoryReviewStorage();
//
//    review1 = new Review("CSCI1234", "user1", "Great course!", 4.5, 4.8, 0, true, 6.0, 3);
//
//    review2 =
//        new Review("CSCI1234", "user2", "Not my favorite course.", 3.0, 2.8, 0, false, 10.0, 2);
//  }
//
//  @Test
//  public void testAddAndRetrieveReviews() {
//    // Add reviews to the storage
//    reviewStorage.addReview(review1.uid, review1.crn, review1);
//    reviewStorage.addReview(review2.uid, review2.crn, review2);
//
//    // Retrieve reviews for the course
//    List<Review> reviews = reviewStorage.getReviews("CSCI1234");
//
//    // Verify that the reviews were stored correctly
//    assertEquals(2, reviews.size());
//    assertTrue(reviews.contains(review1));
//    assertTrue(reviews.contains(review2));
//  }
//
//  @Test
//  public void testDeleteReview() {
//    // Add reviews to the storage
//    reviewStorage.addReview(review1.uid, review1.crn, review1);
//    reviewStorage.addReview(review2.uid, review2.crn, review2);
//
//    // Delete one review
//    reviewStorage.deleteReview("CSCI1234", "user1");
//
//    // Verify the review was deleted
//    List<Review> reviews = reviewStorage.getReviews("CSCI1234");
//    assertEquals(1, reviews.size());
//    assertFalse(reviews.contains(review1));
//    assertTrue(reviews.contains(review2));
//  }
//
//  @Test
//  public void testChangeKarma() throws Exception {
//    // Add a review
//    reviewStorage.addReview(review1.uid, review1.crn, review1);
//
//    // Change karma (upvote)
//    reviewStorage.changeKarma("CSCI1234", "user1", true);
//
//    // Verify the karma was updated
//    List<Review> reviews = reviewStorage.getReviews("CSCI1234");
//    assertEquals(1, reviews.size());
//    assertEquals(1, reviews.get(0).karma);
//
//    // Change karma (downvote)
//    reviewStorage.changeKarma("CSCI1234", "user1", false);
//    reviews = reviewStorage.getReviews("CSCI1234");
//    assertEquals(0, reviews.get(0).karma);
//  }
//
//  @Test
//  public void testClearUser() throws Exception {
//    // Add reviews for two different users
//    reviewStorage.addReview(review1.uid, review1.crn, review1);
//    reviewStorage.addReview(review2.uid, review2.crn, review2);
//
//    // Clear reviews for one user
//    reviewStorage.clearUser("user1");
//
//    // Verify the user's reviews were removed
//    List<Review> reviews = reviewStorage.getReviews("CSCI1234");
//    assertEquals(1, reviews.size());
//    assertFalse(reviews.contains(review1));
//    assertTrue(reviews.contains(review2));
//  }
//
//  @Test
//  public void testGetReviewsForNonexistentCRN() {
//    // Retrieve reviews for a non-existent CRN
//    List<Review> reviews = reviewStorage.getReviews("CSCI5678");
//
//    // Verify that the list is empty
//    assertTrue(reviews.isEmpty());
//  }
// }
