package edu.brown.cs.student.main.server.handlers;

import static edu.brown.cs.student.main.server.reviews.RandomStringGenerator.generateRandomString;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.Professor;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import spark.Request;
import spark.Response;
import spark.Route;

public class UploadReviewHandler implements Route {

  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final Type listOfProfessorsType = Types.newParameterizedType(List.class, Professor.class);
  private final Type listOfCoursesType = Types.newParameterizedType(List.class, Course.class);
  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public UploadReviewHandler(CourseDataSource dataSource, StorageInterface storageHandler) {
    this.storageHandler = storageHandler;
    this.dataSource = dataSource;
  }

  /**
   * Handler for uploading reviews
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return response content
   * @throws Exception This is part of the interface; we don't have to throw anything.
   */
  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> responseMap = new HashMap<>();

    if ((!Objects.equals(request.queryParams("concentrator"), "true")
            && !Objects.equals(request.queryParams("concentrator"), "false"))
        || request.queryParams("courseRating") == null
        || request.queryParams("courseRating").isEmpty()
        || request.queryParams("professorRating") == null
        || request.queryParams("professorRating").isEmpty()
        || request.queryParams("semestercrn") == null
        || request.queryParams("semestercrn").isEmpty()
        || request.queryParams("reviewText") == null
        || request.queryParams("reviewText").isEmpty()
        || request.queryParams("hours") == null
        || request.queryParams("hours").isEmpty()
        || request.queryParams("year") == null
        || request.queryParams("year").isEmpty()
        || !this.dataSource.crnExists(
            request.queryParams("semestercrn").substring(0, 6),
            request.queryParams("semestercrn").substring(6))) {
      responseMap.put("result", "error_bad_request");
      responseMap.put(
          "errormessage",
          "Bad request: concentrator is not true or false,"
              + " course and professor rating not given, or semestercrn doesn't exist");
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }

    try {
      boolean concentrator = Objects.equals(request.queryParams("concentrator"), "true");
      double courseRating = Double.parseDouble(request.queryParams("courseRating"));
      double hours = Double.parseDouble(request.queryParams("hours"));
      double professorRating = Double.parseDouble(request.queryParams("professorRating"));
      int year = Integer.parseInt(request.queryParams("year"));
      if (courseRating < 1
          || professorRating < 1
          || courseRating > 5
          || professorRating > 5
          || hours < 0
          || hours > 50
          || year < 1
          || year > 5) {
        throw new IllegalArgumentException(
            "Professor and course ratings must be in the range 1-5.");
      }
      String semestercrn = request.queryParams("semestercrn");
      String reviewText = request.queryParams("reviewText");
      String uid = request.attribute("user");

      String uniqueID = generateRandomString();

      // TODO: Make sure review object parameters are correct values (karma, isApproved)
      Review review =
          new Review(
              semestercrn,
              uid,
              reviewText,
              courseRating,
              professorRating,
              0,
              concentrator,
              hours,
              year,
              false,
              uniqueID,
              OffsetDateTime.now());
      this.storageHandler.addReview(semestercrn, uniqueID, review);
      responseMap.put("result", "success");
      responseMap.put("reviewID", uniqueID);
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
