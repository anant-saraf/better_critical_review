package edu.brown.cs.student.main.server.reviews;

public class ReviewableCourse {

  private String courseName;
  private String code;
  private String semester;
  private String crn;
  private String section;
  private ReviewStatus reviewStatus;

  // Constructor
  public ReviewableCourse(
      String courseName,
      String code,
      String semester,
      String crn,
      String section,
      ReviewStatus reviewStatus) {
    this.courseName = courseName;
    this.code = code;
    this.semester = semester;
    this.crn = crn;
    this.section = section;
    this.reviewStatus = reviewStatus;
  }

  // Getters and Setters
  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getSemester() {
    return semester;
  }

  public void setSemester(String semester) {
    this.semester = semester;
  }

  public String getCrn() {
    return crn;
  }

  public void setCrn(String crn) {
    this.crn = crn;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public void setReviewStatus(ReviewStatus reviewStatus) {
    this.reviewStatus = reviewStatus;
  }

  // toString for easier debugging and logging
  @Override
  public String toString() {
    return "ReviewableCourse{"
        + "courseName='"
        + courseName
        + '\''
        + ", code='"
        + code
        + '\''
        + ", semester='"
        + semester
        + '\''
        + ", crn='"
        + crn
        + '\''
        + ", section='"
        + section
        + '\''
        + ", reviewStatus="
        + reviewStatus
        + '}';
  }
}
