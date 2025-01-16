package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.datasource.CourseOffering;
import edu.brown.cs.student.main.server.reviews.OffsetDateTimeAdapter;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetUnmoderatedReviewsHandler implements Route {
  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final Type listOfReviewsType = Types.newParameterizedType(List.class, Review.class);
  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public GetUnmoderatedReviewsHandler(
      CourseDataSource dataSource, StorageInterface storageHandler) {
    this.dataSource = dataSource;
    this.storageHandler = storageHandler;
  }

  /** Handler for getting the unmoderated reviews. */
  @Override
  public Object handle(Request request, Response response) {
    // TODO: Check if user is moderator
    Moshi moshi = new Moshi.Builder().add(new OffsetDateTimeAdapter()).build();
    Map<String, Object> responseMap = new HashMap<>();

    try {
      List<Review> reviewList = this.storageHandler.getUnmoderatedReviews();
      reviewList.sort(Comparator.comparingLong(r -> r.submitTime.toEpochSecond()));
      for (int i = 0; i < reviewList.size(); i++) {
        CourseOffering offering =
            this.dataSource.getOfferingBySemesterCrn(reviewList.get(i).semestercrn);
        reviewList.get(i).setPrettyTitle(offering.code() + ": " + offering.title());
      }
      responseMap.put("result", "success");
      responseMap.put("reviews", moshi.adapter(listOfReviewsType).toJson(reviewList));
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", e.getMessage());
    }
    return moshi.adapter(mapStringObjectType).toJson(responseMap);
  }
}
