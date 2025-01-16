package edu.brown.cs.student.main.server.datasource;

import java.util.List;
import java.util.Map;

// crn is the unique course/section code, so no, semester and instructorID are mapped from crn
// semester corresponds to srcdb for the CAB queries
// title is mapped from semester because title of the course can change by semester
public record Course(
    String code,
    String title,
    List<String> crn,
    Map<String, String> no,
    Map<String, String> semester,
    Map<String, List<String>> instructorID) {}
