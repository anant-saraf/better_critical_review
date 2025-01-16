package edu.brown.cs.student;

import edu.brown.cs.student.main.server.datasource.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationService {

  // Method to get similar courses based on search term
  public static List<Course> getSimilarCourses(String searchTerm, List<Course> allCourses) {
    return allCourses.stream()
        // TODO: Decide other attributes to base similar courses on other than just department
        // (maybe stats?)
        .filter(course -> course.code().toLowerCase().contains(searchTerm.toLowerCase()))
        .collect(Collectors.toList());
  }

  // Method to fetch popular courses as a fallback
  public static List<Course> getPopularCourses() {
    List<Course> popularCourses = new ArrayList<>();
    /*TODO: Decide how we want to do this, could hardcode most poplar courses for department for
    semester (freshman dif. than upperclassman for ex)*/
    //    popularCourses.add(new Course(1, "Introduction to Java", "Learn the basics of Java
    // programming."));
    //    popularCourses.add(new Course(2, "World History 101", "Explore significant events in world
    // history."));
    //    return popularCourses;
    return null;
  }

  // Main recommendation method
  public static List<Course> generateRecommendations(String searchTerm, List<Course> allCourses) {
    List<Course> similarCourses = getSimilarCourses(searchTerm, allCourses);
    return similarCourses.isEmpty() ? getPopularCourses() : similarCourses;
  }
}
