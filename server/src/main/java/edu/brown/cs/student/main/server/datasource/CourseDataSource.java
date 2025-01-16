package edu.brown.cs.student.main.server.datasource;

import java.util.List;

/** Interface implemented by CAB data sources - mock/real. */
public interface CourseDataSource {
  List<Course> searchCourses(String term, int maxResults);

  List<Professor> searchProfessors(String term, int maxResults);

  CourseOffering getCourseOffering(String code, String crn);

  boolean crnExists(String semester, String crn);

  String getCrnCode(String semester, String crn);

  List<CourseOffering> getProfessorCourses(String id);

  String getProfessorName(String id);

  CourseOffering getOfferingBySemesterCrn(String semesterCrn);
}
