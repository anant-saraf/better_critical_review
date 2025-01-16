package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import spark.Request;
import spark.Response;
import spark.Route;

public class ModerateHandler implements Route {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final StorageInterface storageHandler;

  public ModerateHandler(StorageInterface storageHandler) {
    this.storageHandler = storageHandler;
  }

  /** Handler for moderating a course. */
  @Override
  public Object handle(Request request, Response response) {
    // TODO: Check if user is moderator.
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> responseMap = new HashMap<>();

    if (request.queryParams("reviewID") == null
        || request.queryParams("reviewID").isEmpty()
        || request.queryParams("approve") == null
        || (!Objects.equals(request.queryParams("approve"), "true")
            && !Objects.equals(request.queryParams("approve"), "false"))) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", "Parameters are empty or missing");
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }

    try {
      this.storageHandler.moderateReview(
          request.queryParams("reviewID"), Objects.equals(request.queryParams("approve"), "true"));
      responseMap.put("result", "success");
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
