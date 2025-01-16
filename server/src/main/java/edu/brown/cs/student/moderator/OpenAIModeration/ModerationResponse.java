package edu.brown.cs.student.moderator.OpenAIModeration;

import com.squareup.moshi.Json;
import java.util.List;

public class ModerationResponse {
  @Json(name = "results")
  public List<ModerationResult> results;
}
