package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.CourseOffering;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handler class for the soup ordering API endpoint.
 *
 * <p>This endpoint is similar to the endpoint(s) you'll need to create for Sprint 2. It takes a
 * basic GET request with no Json body, and returns a Json object in reply. The responses are more
 * complex, but this should serve as a reference.
 */
public class GetOfferingHandler implements Route {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final CourseDataSource courseDataSource;

  public GetOfferingHandler(CourseDataSource courseDataSource) {
    this.courseDataSource = courseDataSource;
  }

  /**
   * Searches the courses and professors json files for a match.
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return response content
   * @throws Exception This is part of the interface; we don't have to throw anything.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Query params are retrieved with request.queryParams(name)
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> responseMap = new HashMap<>();
    String courseCode = request.queryParams("code");
    String courseCrn = request.queryParams("crn");
    if (courseCode == null || courseCode.isBlank() || courseCrn == null || courseCrn.isBlank()) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", "Bad request: course code or crn are empty");
      responseMap.put("code", courseCode);
      responseMap.put("crn", courseCrn);
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }
    try {
      CourseOffering offering = courseDataSource.getCourseOffering(courseCode, courseCrn);
      responseMap.put("result", "success");
      responseMap.put("offering", moshi.adapter(CourseOffering.class).toJson(offering));
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
      responseMap.put("code", courseCode);
      responseMap.put("crn", courseCrn);
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
