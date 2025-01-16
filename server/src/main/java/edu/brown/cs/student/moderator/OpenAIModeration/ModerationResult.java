package edu.brown.cs.student.moderator.OpenAIModeration;

import java.util.Map;

public record ModerationResult(
    boolean flagged, Map<String, Boolean> categories, Map<String, Double> categoryScores) {}
