package edu.brown.cs.student.moderator.OpenAIModeration;

import com.squareup.moshi.Json;

public class ModerationRequest {

  @Json(name = "model")
  public String model;

  @Json(name = "input")
  public String input;

  public ModerationRequest(String model, String input) {
    this.model = model;
    this.input = input;
  }
}
