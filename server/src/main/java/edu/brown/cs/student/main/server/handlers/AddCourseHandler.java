package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.reviews.OffsetDateTimeAdapter;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddCourseHandler implements Route {

  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);

  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public AddCourseHandler(CourseDataSource dataSource, StorageInterface storageHandler) {
    this.dataSource = dataSource;
    this.storageHandler = storageHandler;
  }

  /**
   * Handles a request to add a course to the user profile.
   *
   * @param request The request object providing information about the HTTP request
   * @param response The response object providing functionality for modifying the response
   * @return The content to be set in the response
   */
  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().add(new OffsetDateTimeAdapter()).build();
    Map<String, Object> responseMap = new HashMap<>();

    String semesterCrn = request.queryParams("semestercrn");
    try {
      if (semesterCrn != null
          && !semesterCrn.isBlank()
          && this.dataSource.crnExists(semesterCrn.substring(0, 6), semesterCrn.substring(6))) {
        this.storageHandler.addCourse(request.attribute("user"), semesterCrn);
        responseMap.put("result", "success");
      }
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
      responseMap.put("semestercrn", semesterCrn);
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
