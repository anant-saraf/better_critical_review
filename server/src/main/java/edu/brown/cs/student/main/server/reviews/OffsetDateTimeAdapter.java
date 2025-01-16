package edu.brown.cs.student.main.server.reviews;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeAdapter {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  @ToJson
  String toJson(OffsetDateTime dateTime) {
    return dateTime.format(formatter);
  }

  // Parse the String back to a LocalDate
  @FromJson
  OffsetDateTime fromJson(String dateTime) {
    return OffsetDateTime.parse(dateTime, formatter);
  }
}
