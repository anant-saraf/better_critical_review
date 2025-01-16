package edu.brown.cs.student.CabAPISourceTests;

import static edu.brown.cs.student.main.server.reviews.RandomStringGenerator.generateRandomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.server.datasource.CabAPISource;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.storage.FirebaseUtilities;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FirebaseUnit {

  CabAPISource source;
  FirebaseUtilities utils;
  StorageInterface storage;

  /**
   * having issues with/1 review should be uploaded to:
   * http://localhost:3232/get-reviews?semestercrn=20241018090&sortMode=0&filterMode=0&filterKey=0
   */
  @Test
  public void testAddGetReviewsSimple()
      throws ExecutionException, InterruptedException, IllegalAccessException {
    try {
      this.storage = new FirebaseUtilities();
      source =
          new CabAPISource(
              Path.of("data/mocks/CourseDatasetMock.json"),
              Path.of("data/professors/professors.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    /**
     * "concentrator=true&professorRating=5&courseRating=5&" +
     * "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview" +
     * "&semestercrn=20231016486"
     */
    String uniqueID = generateRandomString();
    this.storage.addCourse("testUser", "20231016486");
    Review review =
        new Review(
            "20231016486",
            "testUser",
            "thisIsATestReviewFromtestUploadGetDeleteReview",
            5,
            5,
            0,
            true,
            15,
            2,
            false,
            uniqueID,
            OffsetDateTime.now());
    this.storage.addReview("20231016486", uniqueID, review);
    this.storage.moderateReview(uniqueID, true);

    List<Review> reviews;
    try {
      reviews = this.storage.getReviews("20231016486");
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    } finally {
      this.storage.deleteReview("20231016486", uniqueID);
    }
    assertEquals("thisIsATestReviewFromtestUploadGetDeleteReview", reviews.get(0).reviewText);
  }

  @Test
  public void testAddGetReviewsMultipleUsers()
      throws ExecutionException, InterruptedException, IllegalAccessException {
    try {
      this.storage = new FirebaseUtilities();
      source =
          new CabAPISource(
              Path.of("data/mocks/CourseDatasetMock.json"),
              Path.of("data/professors/professors.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Review review1 =
        new Review(
            "20231016486",
            "testUser",
            "a review from testUser",
            5,
            5,
            0,
            false,
            15,
            2,
            false,
            "id1",
            OffsetDateTime.now());
    this.storage.addCourse("testUser", "20231016486");
    this.storage.addCourse("testUser2", "20231016486");
    this.storage.addCourse("testUser3", "20231016486");
    this.storage.addCourse("testUser4", "20231016486");
    this.storage.addReview("20231016486", "id1", review1);

    Review review2 =
        new Review(
            "20231016486",
            "testUser2",
            "a review from testUser2",
            4,
            4,
            0,
            true,
            7,
            3,
            false,
            "id2",
            OffsetDateTime.now());
    this.storage.addReview("20231016486", "id2", review2);

    Review review3 =
        new Review(
            "20231016486",
            "testUser3",
            "a review from testUser3",
            5,
            5,
            0,
            true,
            3,
            4,
            false,
            "id3",
            OffsetDateTime.now());
    this.storage.addReview("20231016486", "id3", review3);

    Review review4 =
        new Review(
            "20231016486",
            "testUser4",
            "a review from testUser4",
            1,
            1,
            0,
            false,
            20,
            1,
            false,
            "id4",
            OffsetDateTime.now());
    this.storage.addReview("20231016486", "id4", review4);
    this.storage.moderateReview("id1", true);
    this.storage.moderateReview("id2", true);
    this.storage.moderateReview("id3", true);
    this.storage.moderateReview("id4", true);
    List<Review> reviews;
    try {

      reviews = this.storage.getReviews("20231016486");
      this.storage.deleteReview("20231016486", "id1");
      this.storage.deleteReview("20231016486", "id2");
      this.storage.deleteReview("20231016486", "id3");
      this.storage.deleteReview("20231016486", "id4");
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    StringBuilder reviewTextCollection = new StringBuilder();
    StringBuilder uniqueIDCollection = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      reviewTextCollection.append(reviews.get(i).reviewText).append(" ");
      uniqueIDCollection.append(reviews.get(i).uniqueID).append(" ");
    }

    List<Review> reviewsToCheck = new ArrayList<>(List.of(review1, review2, review3, review4));

    for (int i = 0; i < 4; i++) {
      if (!reviewTextCollection.toString().contains(reviewsToCheck.get(i).reviewText))
        Assertions.fail();
      if (!uniqueIDCollection.toString().contains(reviewsToCheck.get(i).uniqueID))
        Assertions.fail();
    }
  }

  @Test
  public void testAddReviewChangeKarma()
      throws ExecutionException, InterruptedException, IllegalAccessException {

    try {
      this.storage = new FirebaseUtilities();
      source =
          new CabAPISource(
              Path.of("data/courses/courses.json"), Path.of("data/professors/professors.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.storage.deleteReview("20231016486", "sampleRandomString");
    /**
     * "concentrator=true&professorRating=5&courseRating=5&" +
     * "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview" +
     * "&semestercrn=20231016486"
     */
    Review review =
        new Review(
            "20231016486",
            "testUser",
            "thisIsATestReviewFromtestUploadGetDeleteReview",
            5,
            5,
            0,
            true,
            15,
            2,
            true,
            "sampleRandomString",
            OffsetDateTime.now());
    this.storage.addReview("20231016486", "sampleRandomString", review);

    long karma;
    // upvoting
    try {
      karma = this.storage.changeKarma("20231016486", review.uniqueID, "testUser", true);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertEquals(1, karma);

    // upvoting a second time
    try {
      karma = this.storage.changeKarma("20231016486", review.uniqueID, "testUser", true);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertEquals(1, karma);

    // downvoting after upvoting
    try {
      karma = this.storage.changeKarma("20231016486", review.uniqueID, "testUser", false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertEquals(-1, karma);

    // downvoting a second time
    try {
      karma = this.storage.changeKarma("20231016486", review.uniqueID, "testUser", false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertEquals(-1, karma);
  }
}
