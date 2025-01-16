package edu.brown.cs.student.main.server.reviews;

import java.time.OffsetDateTime;

public class Review {

  public String semestercrn; // course name
  public String userID; // university id
  public String reviewText;
  public double courseRating;
  public double professorRating;
  public long karma;
  public boolean concentrator;
  public double hours;
  public long year;
  public boolean isApproved;
  public String uniqueID;
  public OffsetDateTime submitTime;
  public String prettyTitle;

  public Review(
      String semestercrn,
      String userID,
      String reviewText,
      double courseRating,
      double professorRating,
      long karma,
      boolean concentrator,
      double hours,
      long year,
      boolean isApproved,
      String uniqueID,
      OffsetDateTime submitTime) {
    this.semestercrn = semestercrn;
    this.userID = userID;
    this.reviewText = reviewText;
    this.courseRating = courseRating;
    this.professorRating = professorRating;
    this.karma = karma;
    this.hours = hours;
    this.concentrator = concentrator;
    this.year = year;
    this.isApproved = isApproved;
    this.uniqueID = uniqueID;
    this.submitTime = submitTime;
  }

  public void setApproved(boolean approved) {
    this.isApproved = approved;
  }

  public void setPrettyTitle(String prettyTitle) {
    this.prettyTitle = prettyTitle;
  }
}
