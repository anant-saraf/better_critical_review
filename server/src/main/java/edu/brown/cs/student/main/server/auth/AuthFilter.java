package edu.brown.cs.student.main.server.auth;

import static spark.Spark.halt;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Filter;
import spark.Request;
import spark.Response;

public class AuthFilter implements Filter {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final AuthVerifier verifier;

  public AuthFilter(AuthVerifier verifier) {
    this.verifier = verifier;
  }

  @Override
  public void handle(Request request, Response response) {
    if (request.requestMethod().equals("GET")) {
      try {
        String user = this.verifier.getAuthenticatedUser(request);
        request.attribute("user", user);
      } catch (IllegalAccessException e) {
        Moshi moshi = new Moshi.Builder().build();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("result", "error_unauthenticated");
        responseMap.put("errormessage", e.getMessage());
        halt(401, moshi.adapter(mapStringObjectType).toJson(responseMap));
      }
    }
  }
}
