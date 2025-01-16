package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.CourseOffering;
import edu.brown.cs.student.main.server.reviews.ReviewStatus;
import edu.brown.cs.student.main.server.reviews.ReviewableCourse;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetUserProfileHandler implements Route {

  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public GetUserProfileHandler(CourseDataSource dataSource, StorageInterface storageHandler) {
    this.dataSource = dataSource;
    this.storageHandler = storageHandler;
  }

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();
    Moshi moshi = new Moshi.Builder().build();

    try {
      String userId = request.attribute("user");
      Map<String, ReviewStatus> semesterCrns = storageHandler.getUserCourses(userId);
      List<ReviewableCourse> courses = new ArrayList<>();

      for (Entry<String, ReviewStatus> entry : semesterCrns.entrySet()) {
        CourseOffering offering = this.dataSource.getOfferingBySemesterCrn(entry.getKey());
        courses.add(
            new ReviewableCourse(
                offering.title(),
                offering.code(),
                offering.semester(),
                offering.crn(),
                offering.no(),
                entry.getValue()));
      }

      responseMap.put("result", "success");
      responseMap.put("courses", courses);
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    } catch (Exception e) {
      e.printStackTrace();
      return buildErrorResponse(response, 500, e.getMessage(), moshi);
    }
  }

  //  private Object handleUpdate(Request request, Response response, String userId, Moshi moshi,
  // Map<String, Object> responseMap)
  //      throws IOException, ExecutionException, InterruptedException {
  //    String coursesJson = request.queryParams("courses");
  //
  //    if (coursesJson == null || coursesJson.isBlank()) {
  //      // Log detailed information for debugging
  //      System.out.println("coursesJson is null or blank for userId " + userId + ". Defaulting to
  // an empty profile.");
  //
  //      // Check if the user already has courses stored
  //      List<ReviewableCourse> existingCourses = storageHandler.getUserCourses(userId);
  //      if (existingCourses == null || existingCourses.isEmpty()) {
  //        System.out.println("No existing courses found for userId " + userId + ". Initializing an
  // empty profile.");
  //        storageHandler.storeUserCourses(userId, existingCourses); // Store an empty list only if
  // not already present
  //      } else {
  //        System.out.println("Existing courses found for userId " + userId + ": " +
  // existingCourses);
  //      }
  //    } else {
  //      // Parse and store courses if coursesJson is provided
  //      try {
  //        List<ReviewableCourse> courses = parseCourses(coursesJson, moshi);
  //        System.out.println("Storing updated courses for userId " + userId + ": " + courses);
  //        storageHandler.storeUserCourses(userId, courses);
  //      } catch (IOException e) {
  //        System.err.println("Failed to parse coursesJson for userId " + userId + ": " +
  // e.getMessage());
  //        response.status(400); // Bad Request
  //        responseMap.put("result", "error_bad_request");
  //        responseMap.put("errormessage", "Invalid courses JSON: " + e.getMessage());
  //        return moshi.adapter(mapStringObjectType).toJson(responseMap);
  //      }
  //    }
  //
  //    responseMap.put("result", "success");
  //    responseMap.put("message", "User profile updated successfully.");
  //    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  //  }

  private Object buildErrorResponse(
      Response response, int status, String errorMessage, Moshi moshi) {
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("result", "error_bad_request");
    responseMap.put("errormessage", errorMessage);
    response.status(status);
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
