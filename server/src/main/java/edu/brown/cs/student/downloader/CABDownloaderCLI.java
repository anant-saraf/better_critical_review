package edu.brown.cs.student.downloader;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.Professor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class CABDownloaderCLI {

  // What semesters to scrape from CAB
  private static final String[] semestersToDownload = {"202310", "202315", "202320", "202400"};

  public static void main(String[] args) throws IOException {
    CABDownloader downloader = new CABDownloader();
    for (String semester : semestersToDownload) {
      downloader.download(semester);
    }

    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Collection<Course>> courseListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Course.class));

    JsonAdapter<Collection<Professor>> professorListAdapter =
        moshi.adapter(Types.newParameterizedType(Collection.class, Professor.class));

    try (PrintWriter out = new PrintWriter("data/courses/courses.json")) {
      out.print(courseListAdapter.toJson(downloader.getCourses()));
    }
    try (PrintWriter out = new PrintWriter("data/professors/professors.json")) {
      out.print(professorListAdapter.toJson(downloader.getProfessors()));
    }

    // Print two helper files to see what semesters we have downloaded
    try (PrintWriter out = new PrintWriter("data/courses/semesters")) {
      for (String semester : semestersToDownload) {
        out.print(semester + ",");
      }
    }
    try (PrintWriter out = new PrintWriter("data/professors/semesters")) {
      for (String semester : semestersToDownload) {
        out.print(semester + ",");
      }
    }
  }
}
