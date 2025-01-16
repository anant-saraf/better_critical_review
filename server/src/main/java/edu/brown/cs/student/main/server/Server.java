package edu.brown.cs.student.main.server;

import static spark.Spark.before;
import static spark.Spark.halt;

import edu.brown.cs.student.main.server.auth.AuthFilter;
import edu.brown.cs.student.main.server.auth.ClerkAuthVerifier;
import edu.brown.cs.student.main.server.datasource.CabAPISource;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.handlers.AddCourseHandler;
import edu.brown.cs.student.main.server.handlers.GetDescriptionHandler;
import edu.brown.cs.student.main.server.handlers.GetOfferingHandler;
import edu.brown.cs.student.main.server.handlers.GetProfessorHandler;
import edu.brown.cs.student.main.server.handlers.GetReviewsHandler;
import edu.brown.cs.student.main.server.handlers.GetUnmoderatedReviewsHandler;
import edu.brown.cs.student.main.server.handlers.GetUserProfileHandler;
import edu.brown.cs.student.main.server.handlers.KarmaHandler;
import edu.brown.cs.student.main.server.handlers.ModerateHandler;
import edu.brown.cs.student.main.server.handlers.SearchHandler;
import edu.brown.cs.student.main.server.handlers.UploadReviewHandler;
import edu.brown.cs.student.main.server.storage.FirebaseUtilities;
import java.io.IOException;
import java.nio.file.Path;
import spark.Spark;

/** Server stencil code taken from Sprint 2 gearup */
public class Server {
  private static final String jwtKey =
      "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0qJibRk0cz76Sjbi5HbPktCbLTQp5VSJ2K2lnslAVKZeYFhD34TU+nyDwK4rxdTPDZtcygxMf66gl+IMf3WATtCNgWdbmggXbz81i2AJkvosP5MrZ/gyYwXexk3lLidoE32RmPJJp3I3dW6FkpbA4n51ERqK2J1QDlNnvZ6PN21kguEZYQnOAcnAIVEx7GeOH61nRYgxycRnsdgcy+zgdyikUfHe1ZtCgGKyd0oU0rNc5jtVENtngYPGE3sk6NPIbTE6EpnKZ630BxzyR5l0IUStn+AUxPikMwhqwXwFHcVwS4bRei1oNMXJSIoQGkcQ7I0mH6aLtg9e4KmtlZRt7wIDAQAB\n"
          + "-----END PUBLIC KEY-----";

  public Server(CourseDataSource source) throws IOException {
    int port = 3232;
    Spark.port(port);

    // CORS
    before(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          response.header("Access-Control-Allow-Headers", "Authorization");
          if (request.requestMethod().equals("OPTIONS")) {
            halt(200);
          }
        });

    FirebaseUtilities utils = new FirebaseUtilities();
    before(new AuthFilter(new ClerkAuthVerifier(jwtKey)));
    Spark.get("search", new SearchHandler(source));
    Spark.get("upload-review", new UploadReviewHandler(source, utils));
    Spark.get("get-reviews", new GetReviewsHandler(source, utils));
    Spark.get("karma", new KarmaHandler(source, utils));
    Spark.get("offering", new GetOfferingHandler(source));
    Spark.get("description", new GetDescriptionHandler(source));
    Spark.get("professor", new GetProfessorHandler(source));
    // Spark.get("store-profile", new UserProfileHandler(source, utils));
    Spark.get("userProfile", new GetUserProfileHandler(source, utils));
    Spark.get("addCourse", new AddCourseHandler(source, utils));
    Spark.get("moderate", new ModerateHandler(utils));
    Spark.get("get-unmoderated", new GetUnmoderatedReviewsHandler(source, utils));

    //    Spark.get("review-handler-mock", new GetReviewHandlerMock(utils, source));
    //    Spark.get("simple-handler", new SimpleHandler());

    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }

  /**
   * Main method
   *
   * @param args arguments
   * @throws IOException from server class
   */
  public static void main(String[] args) throws IOException {
    Server server =
        new Server(
            new CabAPISource(
                Path.of("data/courses/courses.json"), Path.of("data/professors/professors.json")));
    System.out.println("Server started; exiting main...");
  }
}
