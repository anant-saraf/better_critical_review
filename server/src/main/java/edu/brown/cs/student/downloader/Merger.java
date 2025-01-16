package edu.brown.cs.student.downloader;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.Professor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// In case there are multiple course and professor jsons, merge them into one
public class Merger {
  private static final String dataFolder = "data";

  public static void main(String[] args) throws IOException {
    File coursesFolder = new File(dataFolder + "/courses");
    File professorsFolder = new File(dataFolder + "/professors");
    Map<String, Course> courses = new LinkedHashMap<>();
    Map<String, Professor> professors = new LinkedHashMap<>();

    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<Collection<Course>> courseListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Course.class));

    JsonAdapter<Collection<Professor>> professorListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Professor.class));

    for (File fileEntry : Objects.requireNonNull(coursesFolder.listFiles())) {
      if (!fileEntry.isDirectory() && fileEntry.getName().startsWith("courses")) {

        String contents = new String(Files.readAllBytes(fileEntry.toPath()));
        Collection<Course> currentCourses = courseListAdapter.fromJson(contents);

        assert currentCourses != null;

        for (Course course : currentCourses) {
          Course mapCourseEntry = courses.get(course.code());
          if (mapCourseEntry == null) {
            courses.put(course.code(), course);
          } else {
            mapCourseEntry.crn().addAll(course.crn());
            mapCourseEntry.no().putAll(course.no());
            mapCourseEntry.semester().putAll(course.semester());
            mapCourseEntry.instructorID().putAll(course.instructorID());
          }
        }
      }
    }

    for (File fileEntry : Objects.requireNonNull(professorsFolder.listFiles())) {
      if (!fileEntry.isDirectory() && fileEntry.getName().startsWith("professors")) {
        String contents = new String(Files.readAllBytes(fileEntry.toPath()));
        Collection<Professor> currentProfessors = professorListAdapter.fromJson(contents);
        assert currentProfessors != null;

        for (Professor professor : currentProfessors) {
          Professor mapProfessorEntry = professors.get(professor.id());
          if (mapProfessorEntry != null
              && !Objects.equals(mapProfessorEntry.name(), professor.name())) {
            System.out.println(
                "Warning: different names for same ID: "
                    + professor.id()
                    + " "
                    + professor.name()
                    + " and "
                    + mapProfessorEntry.name());
          }
          professors.put(professor.id(), professor);
        }
      }
    }

    try (PrintWriter out = new PrintWriter(dataFolder + "/courses/merged-courses.json")) {
      out.print(courseListAdapter.toJson(courses.values()));
    }
    try (PrintWriter out = new PrintWriter(dataFolder + "/professors/merged-professors.json")) {
      out.print(professorListAdapter.toJson(professors.values()));
    }
  }
}
