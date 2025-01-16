package edu.brown.cs.student.main.server.datasource;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
// import org.apache.commons.text.similarity.LongestCommonSubsequence;

public class CabAPISource implements CourseDataSource {

  Map<String, Course> courseCodeMap;
  Map<String, Professor> professorIDMap;
  Map<String, String> semesterCrnCourseMap;
  Map<String, List<String>> semesterCrnProfessorMap;
  Map<String, List<String>> professorSemesterCrnMap;

  public CabAPISource(Path coursesPath, Path professorsPath) throws IOException {
    this.courseCodeMap = new LinkedHashMap<>(); // maps course codes to course objects
    this.professorIDMap = new LinkedHashMap<>(); // maps professor IDs to professor objects
    this.semesterCrnCourseMap = new HashMap<>(); // maps crns to course objects
    this.semesterCrnProfessorMap = new HashMap<>(); // maps crns to arraylist of professors
    this.professorSemesterCrnMap = new HashMap<>(); // maps professor IDs to arraylist of crns

    Moshi moshi = new Moshi.Builder().build();

    String coursesContents = new String(Files.readAllBytes(coursesPath));
    String professorsContents = new String(Files.readAllBytes(professorsPath));

    JsonAdapter<Collection<Course>> courseListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Course.class));
    JsonAdapter<Collection<Professor>> professorListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Professor.class));

    Collection<Course> courseCollection = courseListAdapter.fromJson(coursesContents);
    Collection<Professor> professorCollection = professorListAdapter.fromJson(professorsContents);

    if (courseCollection == null || professorCollection == null) {
      throw new NullPointerException("Invalid JSON for course or professor dataset provided.");
    }
    for (Professor professor : professorCollection) {
      this.professorIDMap.put(professor.id(), professor);
    }
    for (Course course : courseCollection) {
      courseCodeMap.put(course.code(), course);
      for (String crn : course.crn()) {
        semesterCrnCourseMap.put(course.semester().get(crn) + crn, course.code());
        semesterCrnProfessorMap.put(
            course.semester().get(crn) + crn, course.instructorID().get(crn));

        for (String instructorID : course.instructorID().get(crn)) {
          if (professorSemesterCrnMap.get(instructorID) == null) {
            professorSemesterCrnMap.put(
                instructorID, new ArrayList<>(List.of(course.semester().get(crn) + crn)));
          } else {
            professorSemesterCrnMap.get(instructorID).add(course.semester().get(crn) + crn);
          }
        }
      }
    }
  }

  @Override
  public List<Course> searchCourses(String term, int maxResults) throws NullPointerException {
    if (term == null || term.length() < 2 || maxResults < 1) {
      throw new NullPointerException("Invalid term or max results provided.");
    }

    term = term.trim().toLowerCase();
    ArrayList<Course> courses = new ArrayList<>();
    int resultCounter = 0;
    for (Course course : courseCodeMap.values()) {
      if (resultCounter >= maxResults) {
        break;
      }
      if (course.code().toLowerCase().contains(term)) {
        courses.add(course);
        resultCounter++;
        continue;
      }
      if (course.title().toLowerCase().contains(term.toLowerCase())) {
        courses.add(course);
        resultCounter++;
      }
    }

    // Longest common subsequence doesn't make sense because a search result will contain the search
    // term fully
    //    String finalTerm = term;
    //    LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
    //    courses.sort(
    //        Comparator.comparingInt(
    //            a -> {
    //              int maxLength = Integer.MIN_VALUE;
    //                maxLength =
    //                    max(
    //                        maxLength,
    //                        longestCommonSubsequence.apply(finalTerm, a.title().toLowerCase()));
    //              maxLength =
    //                  max(
    //                      maxLength,
    //                      longestCommonSubsequence.apply(finalTerm, a.code().toLowerCase()));
    //              return maxLength;
    //            }));
    return courses;
  }

  @Override
  public List<Professor> searchProfessors(String term, int maxResults) throws NullPointerException {
    if (term == null || term.length() < 2 || maxResults < 1) {
      throw new NullPointerException("Invalid term or max results provided.");
    }
    ArrayList<Professor> professorList = new ArrayList<>();
    term = term.trim().toLowerCase();

    int ctr = 0;
    for (Professor professor : professorIDMap.values()) {
      if (ctr >= maxResults) {
        break;
      }

      if (professor.name().toLowerCase().trim().contains(term)) {
        ctr++;
        professorList.add(professor);
      }
    }

    // Longest common subsequence doesn't make sense because a search result will contain the search
    // term fully
    //    String finalTerm = term;
    //    LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
    //    professorList.sort(
    //        Comparator.comparingInt(
    //            a -> longestCommonSubsequence.apply(a.name().toLowerCase(), finalTerm)));

    return professorList;
  }

  @Override
  public CourseOffering getCourseOffering(String code, String semestercrn) {
    if (!courseCodeMap.containsKey(code)
        || semestercrn.length() < 6
        || !courseCodeMap.get(code).crn().contains(semestercrn.substring(6))) {
      throw new NullPointerException("Invalid code or CRN provided.");
    }
    Course course = courseCodeMap.get(code);
    ArrayList<Professor> professors = new ArrayList<>();
    ArrayList<String> otherOfferings = new ArrayList<>();
    for (String professorID : semesterCrnProfessorMap.get(semestercrn)) {
      professors.add(professorIDMap.get(professorID));
    }
    String crn = semestercrn.substring(6); // strip off semester
    for (Entry<String, String> otherCrn : course.semester().entrySet()) {
      if (!Objects.equals(otherCrn.getKey(), crn)) {
        otherOfferings.add(otherCrn.getValue() + otherCrn.getKey());
      }
    }
    otherOfferings.sort(Comparator.reverseOrder());
    return new CourseOffering(
        code,
        course.title(),
        crn,
        course.no().get(crn),
        course.semester().get(crn),
        otherOfferings,
        professors);
  }

  @Override
  public boolean crnExists(String semester, String crn) {
    return this.semesterCrnCourseMap.containsKey(semester + crn);
  }

  @Override
  public String getCrnCode(String semester, String crn) {
    if (!crnExists(semester, crn)) {
      throw new IllegalArgumentException("Invalid semester or CRN provided.");
    }
    return this.semesterCrnCourseMap.get(semester + crn);
  }

  @Override
  public List<CourseOffering> getProfessorCourses(String id) {
    if (!professorSemesterCrnMap.containsKey(id)) {
      throw new IllegalArgumentException("Invalid professor ID provided.");
    }
    ArrayList<CourseOffering> offerings = new ArrayList<>();
    for (String semesterCrn : professorSemesterCrnMap.get(id)) {
      offerings.add(getCourseOffering(this.semesterCrnCourseMap.get(semesterCrn), semesterCrn));
    }
    offerings.sort(Comparator.comparing(CourseOffering::semester).reversed());
    return offerings;
  }

  @Override
  public String getProfessorName(String id) {
    if (!professorIDMap.containsKey(id)) {
      throw new NullPointerException("Invalid professor ID provided.");
    }
    return this.professorIDMap.get(id).name();
  }

  // otherOfferings will be empty
  @Override
  public CourseOffering getOfferingBySemesterCrn(String semesterCrn) {
    if (semesterCrn.length() < 6 || !this.semesterCrnCourseMap.containsKey(semesterCrn)) {
      throw new NullPointerException("Invalid semesterCrn provided.");
    }
    String crn = semesterCrn.substring(6);
    String courseCode = this.semesterCrnCourseMap.get(semesterCrn);
    Course course = this.courseCodeMap.get(courseCode);
    return new CourseOffering(
        courseCode,
        course.title(),
        crn,
        course.no().get(crn),
        course.semester().get(crn),
        null,
        null);
  }
}
