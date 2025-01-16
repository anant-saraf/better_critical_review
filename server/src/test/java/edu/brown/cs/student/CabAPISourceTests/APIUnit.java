package edu.brown.cs.student.CabAPISourceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.brown.cs.student.main.server.datasource.CabAPISource;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.CourseOffering;
import edu.brown.cs.student.main.server.datasource.Professor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class APIUnit {

  CabAPISource datasource;

  // TODO: Add tests for getCourseOffering
  @Test
  public void testAPIUnit() {

    try {
      datasource =
          new CabAPISource(
              Path.of("data/mocks/CourseDatasetMock.json"),
              Path.of("data/professors/professors.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // checking if crnExists works
    assert (datasource.crnExists("202310", "18216"));
    assert (!datasource.crnExists("202410", "18217"));

    // simple test for if searchCourses works using course code
    List<Course> courses = datasource.searchCourses("AFRI", 10);
    assert (!courses.isEmpty());
    assert (courses.get(0).code().contains("AFRI"));
    assert (courses.size() == 2);

    // simple test for if maxResults is limited
    courses = datasource.searchCourses("AFRI", 1);
    assert (courses.size() == 1);

    // simple test for searching via professor name
    List<Professor> professors = datasource.searchProfessors("Jinying", 10);
    assert (!professors.isEmpty());
    assert (professors.get(0).name().equals("Jinying Li"));
    assert (professors.get(0).id().equals("10028"));

    // searching via course name
    courses = datasource.searchCourses("Mechanics", 10);
    assert (!courses.isEmpty());
    assert (courses.get(0).code().equals("ENGN 0310"));

    // checking that getCrnCode works across semesters
    String code = datasource.getCrnCode("202310", "17465");
    assert code.equals("CSCI 0112");
    assert code.equals(datasource.getCrnCode("202410", "18062"));

    // simple test for getprofname
    assertEquals("Tim Nelson", datasource.getProfessorName("2784"));
    List<CourseOffering> profOfferings = datasource.getProfessorCourses("2784");
    assert (!profOfferings.isEmpty());

    /* checking that the code for all offerings for that professor only matches the courses they
    are teaching in our dataset */

    for (CourseOffering offering : profOfferings) {
      if (!(offering.code().equals("CSCI 0112") || offering.code().equals("CSCI 0320"))) {
        Assertions.fail("Prof offering not accurate");
      }
      if (!(offering.title().equals("Computing Foundations: Program Organization")
          || offering.title().equals("Introduction to Software Engineering"))) {
        Assertions.fail("Prof offering not accurate");
      }
    }

    CourseOffering offering1 = datasource.getCourseOffering("CSCI 0320", "20231017460");

    // TODO: add tests for getCourseOffering

    assertThrows(IllegalArgumentException.class, () -> datasource.getProfessorCourses("55"));
    assertThrows(IllegalArgumentException.class, () -> datasource.getCrnCode("6", "20302"));
    assertThrows(NullPointerException.class, () -> datasource.searchProfessors(null, 10));
    assertThrows(NullPointerException.class, () -> datasource.searchCourses(null, 10));

    assertThrows(NullPointerException.class, () -> datasource.searchProfessors("Jinying", 0));
    assertThrows(NullPointerException.class, () -> datasource.searchCourses("MCM", 0));

    assertThrows(NullPointerException.class, () -> datasource.searchProfessors("a", 10));
    assertThrows(NullPointerException.class, () -> datasource.searchCourses("b", 10));
  }
}
