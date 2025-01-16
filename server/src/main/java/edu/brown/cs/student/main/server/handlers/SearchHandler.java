package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.Professor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
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
public class SearchHandler implements Route {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final Type listOfProfessorsType = Types.newParameterizedType(List.class, Professor.class);
  private final Type listOfCoursesType = Types.newParameterizedType(List.class, Course.class);
  private final CourseDataSource courseDataSource;

  public SearchHandler(CourseDataSource courseDataSource) {
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
    String query = request.queryParams("query");
    if (query == null || query.isBlank()) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", "Bad request: query is empty");
      responseMap.put("query", query);
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }
    try {
      List<Course> courseList = courseDataSource.searchCourses(query, 110);
      List<Professor> professorList = courseDataSource.searchProfessors(query, 50);

      responseMap.put("result", "success");
      responseMap.put("professors", moshi.adapter(listOfProfessorsType).toJson(professorList));
      responseMap.put("courses", moshi.adapter(listOfCoursesType).toJson(courseList));
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
      responseMap.put("query", query);
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
