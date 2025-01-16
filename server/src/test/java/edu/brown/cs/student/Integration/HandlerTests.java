package edu.brown.cs.student.Integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Spark.before;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.auth.AuthFilter;
import edu.brown.cs.student.main.server.auth.MockAuthVerifier;
import edu.brown.cs.student.main.server.datasource.CabAPISource;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.Professor;
import edu.brown.cs.student.main.server.handlers.GetDescriptionHandler;
import edu.brown.cs.student.main.server.handlers.GetOfferingHandler;
import edu.brown.cs.student.main.server.handlers.GetProfessorHandler;
import edu.brown.cs.student.main.server.handlers.GetReviewsHandler;
import edu.brown.cs.student.main.server.handlers.KarmaHandler;
import edu.brown.cs.student.main.server.handlers.SearchHandler;
import edu.brown.cs.student.main.server.handlers.UploadReviewHandler;
import edu.brown.cs.student.main.server.reviews.OffsetDateTimeAdapter;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.storage.FirebaseUtilities;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class HandlerTests {

  private final ParameterizedType listOfProfessorsType =
      Types.newParameterizedType(List.class, Professor.class);
  private final ParameterizedType listOfCoursesType =
      Types.newParameterizedType(List.class, Course.class);
  private final Type listOfReviewsType = Types.newParameterizedType(List.class, Review.class);

  private CabAPISource source;
  private FirebaseUtilities utils;

  @BeforeAll
  public static void setUpBeforeEverything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  @BeforeEach
  public void setup() throws IOException {
    this.source =
        new CabAPISource(
            Path.of("data/mocks/CourseDatasetMock.json"),
            Path.of("data/professors/professors.json"));

    this.utils = new FirebaseUtilities();
    before(new AuthFilter(new MockAuthVerifier("testUser")));

    // kept here for now, will move into respective tests as needed

    Spark.get("karma", new KarmaHandler(this.source, this.utils));
    Spark.get("offering", new GetOfferingHandler(this.source));
    Spark.get("description", new GetDescriptionHandler(this.source));
    Spark.get("professor", new GetProfessorHandler(this.source));
    Spark.get("upload-review", new UploadReviewHandler(source, utils));
    Spark.get("get-reviews", new GetReviewsHandler(source, utils));
    Spark.get("search", new SearchHandler(source));

    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("search");
    Spark.unmap("upload-review");
    Spark.unmap("get-reviews");
    Spark.unmap("karma");
    Spark.unmap("offering");
    Spark.unmap("description");
    Spark.unmap("professor");

    Spark.awaitStop();
  }

  private static HttpURLConnection tryRequest(String apiCall, String params) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall + "?" + params);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  private Map<String, Object> deserialize(HttpURLConnection clientConnection) throws IOException {
    Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(mapType);
    Map<String, Object> responseMap =
        jsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    return responseMap;
  }

  @Test
  public void testSearch() throws IOException {
    Moshi moshi = new Moshi.Builder().build();

    // searching with a course code query
    HttpURLConnection clientConnection = tryRequest("search", "query=AFRI");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assert (responseMap.get("professors") != null);
    assert (responseMap.get("courses") != null);

    String coursesJson = (String) responseMap.get("courses");
    JsonAdapter<List<Course>> courseListAdapter = moshi.adapter(listOfCoursesType);
    List<Course> courses = courseListAdapter.fromJson(coursesJson);

    assert courses != null;
    for (Course course : courses) {
      if (!course.code().contains("AFRI")) {
        Assertions.fail("Course " + course.code() + " does not contain AFRI");
      }
    }

    String professorsJson = (String) responseMap.get("professors"); // Get JSON string
    JsonAdapter<List<Professor>> professorAdapter = moshi.adapter(listOfProfessorsType);
    List<Professor> professors = professorAdapter.fromJson(professorsJson);
    assert professors.isEmpty();
    // Assert professors list

    // searching with a title query
    clientConnection = tryRequest("search", "query=mountains");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assert (responseMap.get("professors") != null);
    assert (responseMap.get("courses") != null);

    coursesJson = (String) responseMap.get("courses");
    courses = courseListAdapter.fromJson(coursesJson);

    assert courses != null;
    assertEquals(1, courses.size());
    assertEquals("HIAA 0422", courses.get(0).code());
    assertEquals(
        "Mountains and Waters: A History of Chinese Landscape Painting", courses.get(0).title());

    // searching with a professor query
    clientConnection = tryRequest("search", "query=CSCI");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assert (responseMap.get("professors") != null);
    assert (responseMap.get("courses") != null);

    coursesJson = (String) responseMap.get("courses");
    courses = courseListAdapter.fromJson(coursesJson);

    assert courses != null;
    assert (courses.size() == 2);
    assertEquals("CSCI 0112", courses.get(0).code());
    assertEquals("CSCI 0320", courses.get(1).code());

    // testing for multiple professor name results
    clientConnection = tryRequest("search", "query=Adam");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    professorsJson = (String) responseMap.get("professors"); // Get JSON string
    professorAdapter = moshi.adapter(listOfProfessorsType);
    professors = professorAdapter.fromJson(professorsJson);

    assert professors != null;
    assert (professors.size() == 5);
    for (Professor prof : professors) {
      if (!prof.name().toLowerCase().contains("adam")) {
        Assertions.fail("Professor " + prof.name() + " does not contain Adam");
      }
    }

    clientConnection = tryRequest("search", "query=Anna");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    professorsJson = (String) responseMap.get("professors"); // Get JSON string
    professorAdapter = moshi.adapter(listOfProfessorsType);
    professors = professorAdapter.fromJson(professorsJson);

    assert professors != null;
    assert (professors.size() == 16);
    for (Professor prof : professors) {
      if (!prof.name().toLowerCase().contains("anna")) {
        Assertions.fail("Professor " + prof.name() + " does not contain Anna");
      }
    }
    clientConnection.disconnect();
  }

  @Test
  public void testSearchError() throws IOException {
    Moshi moshi = new Moshi.Builder().build();

    // searching with a course code query
    HttpURLConnection clientConnection = tryRequest("search", "query=");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("Bad request: query is empty", responseMap.get("errormessage"));

    clientConnection = tryRequest("search", "query=a");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);
    assertEquals("Invalid term or max results provided.", responseMap.get("errormessage"));
  }

  /**
   * Test fails sometimes due to firebase flakiness- note that running it different times will give
   * different results and if you see the end reviewList, it will have a different review everytime
   *
   * @throws IOException
   */
  @Test
  public void testUploadGetDeleteReview()
      throws IOException, ExecutionException, InterruptedException, IllegalAccessException {
    Moshi moshi = new Moshi.Builder().add(new OffsetDateTimeAdapter()).build();

    this.utils.addCourse("testUser", "20241017039");
    // uploading a review
    HttpURLConnection clientConnection =
        tryRequest(
            "upload-review",
            "concentrator=true&professorRating=5&courseRating=5&"
                + "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview"
                + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    String reviewID = (String) responseMap.get("reviewID");
    this.utils.moderateReview(reviewID, true);
    // getting that review
    clientConnection =
        tryRequest(
            "get-reviews", "filterKey=0&sortMode=0&filterMode=0" + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assert (responseMap.get("reviews") != null);

    String reviewsJson = (String) responseMap.get("reviews"); // Get JSON string
    JsonAdapter<List<Review>> reviewAdapter = moshi.adapter(listOfReviewsType);
    List<Review> reviews = reviewAdapter.fromJson(reviewsJson);

    assert reviews != null;
    assertEquals("thisIsATestReviewFromtestUploadGetDeleteReview", reviews.get(0).reviewText);
    assertTrue(reviews.get(0).concentrator);
    assertEquals(5, reviews.get(0).professorRating);
    assertEquals(5, reviews.get(0).courseRating);
    assertEquals(4, reviews.get(0).hours);
    assertEquals(1, reviews.get(0).year);

    // utils.deleteAllReviews("20241017039");
    this.utils.deleteReview("20241017039", reviewID);

    clientConnection =
        tryRequest(
            "get-reviews", "filterKey=0&sortMode=0&filterMode=0" + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    List<Review> emptyList = Collections.emptyList();
    System.out.println("HandlerTest getReview on empty responseMap: " + responseMap);
    assert responseMap != null;
    reviewsJson = (String) responseMap.get("reviews"); // Get JSON string
    reviewAdapter = moshi.adapter(listOfReviewsType);
    reviews = reviewAdapter.fromJson(reviewsJson);

    assertEquals("success", responseMap.get("result"));
    assertEquals(emptyList, reviews);
    clientConnection.disconnect();
  }

  @Test
  public void testUploadGetError() throws IOException {
    Moshi moshi = new Moshi.Builder().build();

    // testing for upload missing a param
    HttpURLConnection clientConnection =
        tryRequest(
            "upload-review",
            "professorRating=5&courseRating=5&"
                + "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview"
                + "&semestercrn=20231016486");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals(
        "Bad request: concentrator is not true or false,"
            + " course and professor rating not given, or semestercrn doesn't exist",
        responseMap.get("errormessage"));

    // testing for upload invalid professorRating
    clientConnection =
        tryRequest(
            "upload-review",
            "concentrator=true&professorRating=10&courseRating=5&"
                + "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview"
                + "&semestercrn=20231016486");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals(
        "Professor and course ratings must be in the range 1-5.", responseMap.get("errormessage"));

    // testing for get review missing a param
    clientConnection =
        tryRequest("get-reviews", "filterKey=&sortMode=10&filterMode=5&" + "semestercrn=4");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("Parameters are empty or missing", responseMap.get("errormessage"));

    // testing for invalid sort modes
    clientConnection =
        tryRequest("get-reviews", "sortMode=10&filterMode=karma&filterKey=0&" + "semestercrn=4");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("Invalid sort mode: 10", responseMap.get("errormessage"));
  }

  @Test
  public void testUploadGetKarmaDeleteReview()
      throws IOException, ExecutionException, InterruptedException, IllegalAccessException {
    Moshi moshi = new Moshi.Builder().add(new OffsetDateTimeAdapter()).build();

    // utils.deleteAllReviews("20241017039");
    this.utils.addCourse("testUser", "20241017039");
    // uploading a review
    HttpURLConnection clientConnection =
        tryRequest(
            "upload-review",
            "concentrator=true&professorRating=5&courseRating=5&"
                + "hours=4&year=1&reviewText=thisIsATestReviewFromtestUploadGetDeleteReview"
                + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    String reviewID = (String) responseMap.get("reviewID");
    this.utils.moderateReview(reviewID, true);

    // getting that review
    clientConnection =
        tryRequest(
            "get-reviews", "filterKey=0&sortMode=0&filterMode=0" + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assert (responseMap.get("reviews") != null);

    String reviewsJson = (String) responseMap.get("reviews"); // Get JSON string
    JsonAdapter<List<Review>> reviewAdapter = moshi.adapter(listOfReviewsType);
    List<Review> reviews = reviewAdapter.fromJson(reviewsJson);

    clientConnection =
        tryRequest("karma", "upvote=true&uniqueID=" + reviewID + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assertEquals(1, ((Double) responseMap.get("karma")).intValue());

    // getting that review
    clientConnection =
        tryRequest(
            "get-reviews", "filterKey=0&sortMode=0&filterMode=0" + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assert (responseMap.get("reviews") != null);

    reviewsJson = (String) responseMap.get("reviews"); // Get JSON string
    reviewAdapter = moshi.adapter(listOfReviewsType);
    reviews = reviewAdapter.fromJson(reviewsJson);

    assert reviews != null;
    assertEquals(1, (int) (reviews.get(0).karma));

    clientConnection =
        tryRequest("karma", "upvote=false&uniqueID=" + reviewID + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assertEquals(-1, ((Double) responseMap.get("karma")).intValue());

    clientConnection =
        tryRequest(
            "get-reviews", "filterKey=0&sortMode=0&filterMode=0" + "&semestercrn=20241017039");
    assertEquals(200, clientConnection.getResponseCode());
    responseMap = deserialize(clientConnection);
    this.utils.deleteReview("20241017039", reviewID);

    assert responseMap != null;
    assertEquals("success", responseMap.get("result"));
    assert (responseMap.get("reviews") != null);

    reviewsJson = (String) responseMap.get("reviews"); // Get JSON string
    reviewAdapter = moshi.adapter(listOfReviewsType);
    reviews = reviewAdapter.fromJson(reviewsJson);

    assert reviews != null;
    assertEquals(-1, (int) reviews.get(0).karma);
  }
}
