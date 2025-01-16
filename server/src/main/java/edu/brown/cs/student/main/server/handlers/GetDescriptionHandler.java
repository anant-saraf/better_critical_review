package edu.brown.cs.student.main.server.handlers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CabCourseResponse;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.DatasourceException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import okio.Buffer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
public class GetDescriptionHandler implements Route {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final CourseDataSource courseDataSource;
  private final LoadingCache<String, String> descriptionCache;

  public GetDescriptionHandler(CourseDataSource courseDataSource) {
    CacheLoader<String, String> loader =
        new CacheLoader<>() {
          @Override
          public String load(String semesterCrn) throws IOException {
            String semester = semesterCrn.substring(0, 6);
            String crn = semesterCrn.substring(6);
            Moshi moshi = new Moshi.Builder().build();
            HttpURLConnection connection =
                connect(
                    new URL("https://cab.brown.edu/api/?page=fose&route=details"),
                    "{\"key\":\"crn:"
                        + crn
                        + "\",\"srcdb\":\""
                        + semester
                        + "\",\"userWithRolesStr\":\"!!!!!!\"}");
            CabCourseResponse jsonData =
                moshi
                    .adapter(CabCourseResponse.class)
                    .fromJson(new Buffer().readFrom(connection.getInputStream()));
            connection.disconnect();
            if (jsonData == null) {
              throw new NullPointerException("Invalid description returned by CAB");
            }
            return Jsoup.clean(jsonData.description(), Safelist.none());
          }
        };

    this.descriptionCache = CacheBuilder.newBuilder().maximumSize(100).build(loader);
    this.courseDataSource = courseDataSource;
  }

  private HttpURLConnection connect(URL requestURL, String payload)
      throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection clientConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    clientConnection.setRequestProperty("Content-Type", "application/json");
    clientConnection.setRequestProperty("Accept", "application/json");
    clientConnection.setRequestMethod("POST");
    if (payload != null && !payload.isEmpty()) {
      clientConnection.setDoOutput(true);
      OutputStream stream = clientConnection.getOutputStream();
      stream.write(payload.getBytes(StandardCharsets.UTF_8));
      stream.close();
    }
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200) {
      if (clientConnection.getResponseCode() == 429) {
        System.out.println("Rate limited");
        try {
          Thread.sleep(10 * 1000);
        } catch (Exception ignored) {

        }
        return connect(requestURL, payload);
      }
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
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
    String crn = request.queryParams("crn");
    String semester = request.queryParams("semester");
    if (crn == null || semester == null || !this.courseDataSource.crnExists(semester, crn)) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", "Bad request: CRN or semester are invalid");
      responseMap.put("crn", crn);
      responseMap.put("semester", semester);
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }
    try {
      String description = this.descriptionCache.get(semester + crn);
      responseMap.put("result", "success");
      responseMap.put("description", description);
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
      responseMap.put("crn", crn);
      responseMap.put("semester", semester);
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
