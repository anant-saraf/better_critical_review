package edu.brown.cs.student.main.server.datasource;

import java.util.List;

// otherOfferings is a map from crn to semester
public record CourseOffering(
    String code,
    String title,
    String crn,
    String no,
    String semester,
    List<String> otherOfferings,
    List<Professor> professors) {}
