package edu.brown.cs.student.main.server.storage;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.reviews.ReviewStatus;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class FirebaseUtilities implements StorageInterface {

  public FirebaseUtilities() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      String workingDirectory = System.getProperty("user.dir");
      Path firebaseConfigPath =
          Paths.get(workingDirectory, "src", "main", "resources", "firebase_config.json");

      FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath.toString());

      FirebaseOptions options =
          new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      FirebaseApp.initializeApp(options);
    } else {
      System.out.println("FirebaseApp already initialized.");
    }
  }

  @Override
  public void addReview(String semestercrn, String uniqueID, Review review)
      throws IllegalArgumentException, ExecutionException, InterruptedException,
          IllegalAccessException {
    if (semestercrn == null || uniqueID == null || review == null) {
      throw new IllegalArgumentException(
          "addReview: semestercrn, uniqueID, and data cannot be null");
    }

    Firestore db = FirestoreClient.getFirestore();
    DocumentReference reviewDoc =
        db.collection("moderation-queue").document(uniqueID); // Store under uniqueID
    DocumentReference userCourseRef =
        db.collection("users").document(review.userID).collection("courses").document(semestercrn);
    DocumentSnapshot userCourseSnapshot = userCourseRef.get().get();

    if (!userCourseSnapshot.exists()) {
      throw new IllegalAccessException(
          "User "
              + review.userID
              + " does not have semestercrn "
              + semestercrn
              + " added to their courses.");
    }
    if (!Objects.equals(userCourseSnapshot.getString("reviewStatus"), "REVIEW_NOW")) {
      throw new IllegalArgumentException("Cannot leave duplicate review.");
    }
    Map<String, Object> reviewData = new HashMap<>();
    reviewData.put("reviewText", review.reviewText);
    reviewData.put("courseRating", review.courseRating);
    reviewData.put("professorRating", review.professorRating);
    reviewData.put("karma", 0L); // Initialize karma
    reviewData.put("concentrator", review.concentrator);
    reviewData.put("hours", review.hours);
    reviewData.put("year", review.year);
    reviewData.put("isApproved", false);
    reviewData.put("userID", review.userID); // Add UID for internal use only
    reviewData.put("uniqueID", review.uniqueID); // Add UID for internal use only
    reviewData.put("semesterCrn", review.semestercrn);
    reviewData.put("submitTime", review.submitTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    reviewDoc.set(reviewData).get();
    userCourseRef.update("reviewStatus", "PENDING");
  }

  @Override
  public long changeKarma(String semestercrn, String uniqueID, String voterUserID, boolean upvote)
      throws ExecutionException, InterruptedException {
    if (semestercrn == null || uniqueID == null || voterUserID == null) {
      throw new IllegalArgumentException("changeKarma: Parameters cannot be null");
    }

    Firestore db = FirestoreClient.getFirestore();

    // Reference to the specific review
    DocumentReference reviewDoc =
        db.collection("reviews").document(semestercrn).collection("allReviews").document(uniqueID);

    // Reference to the voter's vote
    DocumentReference voteDoc = reviewDoc.collection("votes").document(voterUserID);

    // Fetch the current review data
    DocumentSnapshot reviewSnapshot = reviewDoc.get().get();
    if (!reviewSnapshot.exists()) {
      throw new IllegalStateException("Review document does not exist.");
    }
    Long currentKarma = reviewSnapshot.getLong("karma");
    if (currentKarma == null) {
      throw new IllegalStateException("Review document does not contain karma");
    }

    // Fetch voter's previous vote
    DocumentSnapshot voteSnapshot = voteDoc.get().get();
    String previousVote = voteSnapshot.exists() ? voteSnapshot.getString("vote") : null;

    int karmaChange = 0;

    // Handle upvote or downvote logic
    if (previousVote == null) {
      // User is voting for the first time
      karmaChange = upvote ? 1 : -1;
      voteDoc.set(Collections.singletonMap("vote", upvote ? "upvote" : "downvote")).get();
    } else if ("upvote".equals(previousVote) && !upvote) {
      // Changing from upvote to downvote
      karmaChange = -2;
      voteDoc.update("vote", "downvote").get();
    } else if ("downvote".equals(previousVote) && upvote) {
      // Changing from downvote to upvote
      karmaChange = 2;
      voteDoc.update("vote", "upvote").get();
    } else {
      // User clicked the same vote again (no change)
      return currentKarma;
    }

    reviewDoc.update("karma", FieldValue.increment(karmaChange)).get();

    return currentKarma + karmaChange;
  }

  @Override
  public List<Review> getReviews(String semestercrn)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    List<Review> allReviews = new ArrayList<>();

    CollectionReference reviewsCollection =
        db.collection("reviews").document(semestercrn).collection("allReviews");

    ApiFuture<QuerySnapshot> querySnapshot = reviewsCollection.get();

    for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {

      try {
        Review review = reviewFromDocument(document);
        allReviews.add(review);
      } catch (NullPointerException e) {
        System.out.println("Null Pointer Exception for review" + document.toString());
      }
    }
    return allReviews;
  }

  @Override
  public void deleteReview(String semestercrn, String uniqueID)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    DocumentReference reviewDoc =
        db.collection("reviews").document(semestercrn).collection("allReviews").document(uniqueID);
    String userID = reviewDoc.get().get().getString("userID");
    DocumentReference courseUser =
        db.collection("users").document(userID).collection("courses").document(semestercrn);
    reviewDoc.delete().get();
    courseUser.delete().get();
    System.out.println(
        "review deleted at\t" + "UniqueID: " + uniqueID + "\t" + "semestercrn : " + semestercrn);
  }

  @Override
  public void addCourse(String userId, String semesterCrn)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    System.out.println("storeUserCourses called for userId: " + userId);

    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty.");
    }

    // Reference to the user's courses sub-collection in Firestore
    CollectionReference userCoursesCollection =
        db.collection("users").document(userId).collection("courses");
    DocumentReference courseDoc =
        userCoursesCollection.document(semesterCrn); // Use CRN as unique identifier
    if (!courseDoc.get().get().exists()) {
      Map<String, Object> courseData = new HashMap<>();
      courseData.put("reviewStatus", "REVIEW_NOW");

      courseDoc.set(courseData).get(); // Save course data to Firestore
    } else {
      throw new IllegalArgumentException("User course already exists.");
    }
  }

  // Maps semester CRNs to review status
  @Override
  public Map<String, ReviewStatus> getUserCourses(String userId)
      throws ExecutionException, InterruptedException {
    Map<String, ReviewStatus> coursesMap = new LinkedHashMap<>();

    Firestore db = FirestoreClient.getFirestore();

    CollectionReference userCoursesCollection =
        db.collection("users").document(userId).collection("courses");
    ApiFuture<QuerySnapshot> querySnapshot = userCoursesCollection.get();

    // Wait for the query results
    QuerySnapshot snapshot = querySnapshot.get();

    // Loop through the documents and create ReviewableCourse objects
    for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
      String semesterCrn = document.getId();
      String reviewStatusString = document.getString("reviewStatus");

      // Convert the reviewStatus string to the enum value
      ReviewStatus reviewStatus =
          ReviewStatus.valueOf(reviewStatusString); // Converts the string to the enum
      coursesMap.put(semesterCrn, reviewStatus);
    }

    return coursesMap;
  }

  @Override
  public void moderateReview(String reviewId, boolean approve)
      throws ExecutionException, InterruptedException, IllegalAccessException {
    Firestore db = FirestoreClient.getFirestore();
    DocumentReference reviewDoc = db.collection("moderation-queue").document(reviewId);
    DocumentSnapshot reviewSnapshot = reviewDoc.get().get();
    if (!reviewSnapshot.exists()) {
      throw new IllegalArgumentException("Review ID " + reviewId + " does not exist.");
    }
    String userID = reviewSnapshot.getString("userID");
    String semesterCrn = reviewSnapshot.getString("semesterCrn");
    if (userID == null || userID.isBlank() || semesterCrn == null || semesterCrn.isEmpty()) {
      throw new IllegalStateException("Invalid user ID or semester CRN in database.");
    }
    DocumentReference userCourseRef =
        db.collection("users").document(userID).collection("courses").document(semesterCrn);
    DocumentSnapshot userCourseSnapshot = userCourseRef.get().get();
    if (!userCourseSnapshot.exists()) {
      throw new IllegalAccessException(
          "User "
              + userID
              + " does not have semestercrn "
              + semesterCrn
              + " added to their courses.");
    }

    // Reject review
    if (!approve) {
      reviewDoc.delete().get();
      userCourseRef.update("reviewStatus", "REVIEW_NOW").get();
      return;
    }

    Map<String, Object> reviewData = reviewSnapshot.getData();
    if (reviewData == null) {
      throw new IllegalStateException("Review data is null.");
    }
    reviewData.put("isApproved", true);
    db.collection("reviews")
        .document(semesterCrn)
        .collection("allReviews")
        .document(reviewId)
        .set(reviewData)
        .get();
    userCourseRef.update("reviewStatus", "PUBLISHED").get();
    reviewDoc.delete().get();
  }

  @Override
  public List<Review> getUnmoderatedReviews() throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    List<Review> allReviews = new ArrayList<>();

    CollectionReference moderationQueueCollection = db.collection("moderation-queue");
    ApiFuture<QuerySnapshot> querySnapshot = moderationQueueCollection.get();
    for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
      try {
        Review review = reviewFromDocument(document);
        allReviews.add(review);
      } catch (NullPointerException e) {
        System.out.println("Null Pointer Exception for review" + document.toString());
      }
    }
    return allReviews;
  }

  Review reviewFromDocument(QueryDocumentSnapshot document) {
    return new Review(
        document.contains("semesterCrn") ? document.getString("semesterCrn") : "",
        "0", // Mask UserID
        document.contains("reviewText") ? document.getString("reviewText") : "",
        document.contains("courseRating") ? document.getDouble("courseRating") : 0.0,
        document.contains("professorRating") ? document.getDouble("professorRating") : 0.0,
        document.contains("karma") ? document.getLong("karma") : 0L,
        document.contains("concentrator") ? document.getBoolean("concentrator") : false,
        document.contains("hours") ? document.getDouble("hours") : 0.0,
        document.contains("year") ? document.getLong("year") : 0L,
        document.contains("isApproved") ? document.getBoolean("isApproved") : false,
        document.contains("uniqueID") ? document.getString("uniqueID") : "",
        document.contains("submitTime")
            ? OffsetDateTime.parse(
                document.getString("submitTime"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            : OffsetDateTime.now());
  }

  public Map<String, Object> getDocument(String uid, String collection) {
    return null;
  }

  public void setDocument(String uid, String collection, Map<String, Object> data)
      throws Exception {
    Firestore db = FirestoreClient.getFirestore();
    db.collection(collection).document(uid).set(data);
  }
}
