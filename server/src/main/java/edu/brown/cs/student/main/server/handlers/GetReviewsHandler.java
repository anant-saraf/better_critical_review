package edu.brown.cs.student.main.server.handlers;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.CourseDataSource;
import edu.brown.cs.student.main.server.reviews.OffsetDateTimeAdapter;
import edu.brown.cs.student.main.server.reviews.Review;
import edu.brown.cs.student.main.server.storage.StorageInterface;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetReviewsHandler implements Route {

  private final Type mapStringObjectType =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private final Type listOfReviewsType = Types.newParameterizedType(List.class, Review.class);
  private final StorageInterface storageHandler;
  private final CourseDataSource dataSource;

  public GetReviewsHandler(CourseDataSource dataSource, StorageInterface storageHandler) {
    this.storageHandler = storageHandler;
    this.dataSource = dataSource;
  }

  /**
   * Handler for getting all reviews for a course. Also called to re-sort or filter reviews.
   *
   * <p>We can filter by rating, class year, concentrator and non concentrator
   *
   * <p>This method also calls the required methods to flag reviews with excessive downvotes and
   * store them
   *
   * <p>Requires a crn to match to a course * * Utilizes filterMode (karma, concentrator,
   * ratingLess, ratingMore, year) * and sortMode (karmaA, karmaD, ratingA, ratingD, yearA, yearD,
   * 0) * and filterKey: 0 for non-concentrator and 1 for concentrator, * karma provides reviews
   * with karma less than or equal to the key, * rating provides reviews with less or more than that
   * specified based on filterMode
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return response content
   * @throws Exception This is part of the interface; we don't have to throw anything.
   */
  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().add(new OffsetDateTimeAdapter()).build();
    Map<String, Object> responseMap = new HashMap<>();

    if (request.queryParams("semestercrn").isEmpty()
        || request.queryParams("semestercrn") == null
        || request.queryParams("sortMode").isEmpty()
        || request.queryParams("sortMode") == null
        || request.queryParams("filterMode").isEmpty()
        || request.queryParams("filterMode") == null
        || request.queryParams("filterKey").isEmpty()
        || request.queryParams("filterKey") == null
    /*TODO: Fix this || !this.dataSource.semestercrnExists(request.queryParams("semestercrn"))*/ ) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("errormessage", "Parameters are empty or missing");
      return moshi.adapter(mapStringObjectType).toJson(responseMap);
    }

    try {
      String semestercrn = request.queryParams("semestercrn");
      String sortMode = request.queryParams("sortMode");
      String filterMode = request.queryParams("filterMode");
      int filterKey = Integer.parseInt(request.queryParams("filterKey"));

      List<Review> reviewList = this.storageHandler.getReviews(semestercrn);

      // TODO: figure out karma stuff
      switch (sortMode) {
        case "karmaA":
          reviewList.sort(Comparator.comparingLong(r -> r.karma));
          break;
        case "karmaD":
          reviewList.sort(Comparator.comparingLong(r -> -r.karma));
          break;
        case "ratingA":
          reviewList.sort(Comparator.comparingDouble(r -> r.courseRating));
          break;
        case "ratingD":
          reviewList.sort(Comparator.comparingDouble(r -> -r.courseRating));
          break;
        case "yearA":
          reviewList.sort(Comparator.comparingDouble(r -> r.year));
          break;
        case "yearD":
          reviewList.sort(Comparator.comparingDouble(r -> -r.year));
          break;
        case "0":
          System.out.println("unsorted review list");
          break;
        default:
          throw new IllegalArgumentException("Invalid sort mode: " + sortMode);
      }

      switch (filterMode) {
        case "karma":
          reviewList =
              reviewList.stream()
                  .filter(review -> review.karma >= filterKey)
                  .collect(Collectors.toList());
          break;
        case "concentrator":
          reviewList =
              reviewList.stream()
                  .filter(review -> review.concentrator == (filterKey == 1))
                  .collect(Collectors.toList());
          break;
        case "ratingLess":
          reviewList =
              reviewList.stream()
                  .filter(review -> review.courseRating <= filterKey)
                  .collect(Collectors.toList());
          break;
        case "ratingMore":
          reviewList =
              reviewList.stream()
                  .filter(review -> review.courseRating >= filterKey)
                  .collect(Collectors.toList());
          break;
        case "year":
          reviewList =
              reviewList.stream()
                  .filter(review -> review.year == filterKey)
                  .collect(Collectors.toList());
          break;
        case "0":
          System.out.println("unfiltered review list");
          break;
        default:
          throw new IllegalArgumentException("Invalid filter mode" + filterMode);
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
