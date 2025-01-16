package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import spark.Request;
import spark.Response;
import spark.Route;

public class KarmaHandler implements Route {

  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public KarmaHandler(CourseDataSource dataSource, StorageInterface storageHandler) {
    this.storageHandler = storageHandler;
    this.dataSource = dataSource;
  }

  /**
   * Handler for updating karma for a specific review based on upvotes/downvotes
   *
   * <p>Utilizes upvote and crn queryparams and gets UID using Auth
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

    System.out.println("Upvote: " + request.queryParams("upvote"));
    System.out.println("Semestercrn: " + request.queryParams("semestercrn"));

    if ((!Objects.equals(request.queryParams("upvote"), "true")
            && !Objects.equals(request.queryParams("upvote"), "false"))
        || (request.queryParams("semestercrn") == null
            || request.queryParams("semestercrn").isEmpty()
            || request.queryParams("uniqueID") == null
            || request.queryParams("uniqueID").isEmpty())) {

      responseMap.put("result", "error_bad_request");
      responseMap.put(
          "errormessage",
          "Bad request: CRN provided does not exist or is null/empty or upvote is null or empty");
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }

    try {
      String semestercrn = request.queryParams("semestercrn");
      boolean upvote = Objects.equals(request.queryParams("upvote"), "true");
      String uniqueID = request.queryParams("uniqueID");

      long karma =
          storageHandler.changeKarma(semestercrn, uniqueID, request.attribute("user"), upvote);

      responseMap.put("result", "success");
      responseMap.put("karma", karma);

    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
