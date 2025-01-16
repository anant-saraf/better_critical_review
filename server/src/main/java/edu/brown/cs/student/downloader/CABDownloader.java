package edu.brown.cs.student.downloader;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.datasource.CabCourseResponse;
import edu.brown.cs.student.main.server.datasource.CabSearchResponse;
import edu.brown.cs.student.main.server.datasource.CabSearchResult;
import edu.brown.cs.student.main.server.datasource.Course;
import edu.brown.cs.student.main.server.datasource.DatasourceException;
import edu.brown.cs.student.main.server.datasource.Professor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okio.Buffer;

public class CABDownloader {

  private final Map<String, Course> courses;
  private final Map<String, Professor> professors;
  private final Pattern professorRegex = Pattern.compile("data-id=\"(.*?)\".*?>(.*?)<");

  public CABDownloader() {
    this.courses = new LinkedHashMap<>();
    this.professors = new LinkedHashMap<>();
  }

  // semester corresponds to srcdb
  public void download(String semester) throws IOException, DatasourceException {
    System.out.println("Downloading semester " + semester);
    URL requestURL =
        new URL("https://cab.brown.edu/api/?page=fose&route=search&is_ind_study=N&is_canc=N");
    HttpURLConnection clientConnection =
        connect(
            requestURL,
            "{\"other\":{\"srcdb\":\""
                + semester
                + "\"},\"criteria\":[{\"field\":\"is_ind_study\","
                + "\"value\":\"N\"},{\"field\":\"is_canc\",\"value\":\"N\"}]}");
    Moshi moshi = new Moshi.Builder().build();
    // read data
    JsonAdapter<CabSearchResponse> adapter = moshi.adapter(CabSearchResponse.class);
    CabSearchResponse jsonData =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    // check if data is empty
    if (jsonData == null) {
      throw new NullPointerException();
    }
    int courseCount = jsonData.results.size();
    int courseCounter = 1;
    int notAdded = 0;
    for (CabSearchResult row : jsonData.results) {
      if (row.code == null
          || row.crn == null
          || row.no == null
          || row.srcdb == null
          || row.title == null
          || row.code.isEmpty()
          || row.crn.isEmpty()
          || row.no.isEmpty()
          || row.srcdb.isEmpty()
          || row.title.isEmpty()) {
        courseCounter++;
        notAdded++;
        continue;
      }
      if (row.no.charAt(0) != 'S') {
        // lab section
        courseCounter++;
        notAdded++;
        continue;
      }
      if (row.instr == null
          || row.instr.isEmpty()
          || Objects.equals(row.instr, "TBD")
          || Objects.equals(row.instr, "TBA")) {
        // no professor for this course, ignore it
        courseCounter++;
        notAdded++;
        continue;
      }
      Course currentEntry = this.courses.get(row.code);
      if (currentEntry == null) {
        // Entry doesn't exist, create a new course in the map
        List<String> crnList = new ArrayList<>();
        Map<String, String> noMap = new LinkedHashMap<>();
        Map<String, String> semesterMap = new LinkedHashMap<>();
        Map<String, List<String>> instructorMap = new LinkedHashMap<>();

        crnList.add(row.crn);
        noMap.put(row.crn, row.no);
        semesterMap.put(row.crn, row.srcdb);
        instructorMap.put(row.crn, addProfessorsForCourse(row.crn, row.srcdb));
        this.courses.put(
            row.code, new Course(row.code, row.title, crnList, noMap, semesterMap, instructorMap));
      } else {
        // Entry already exists in courses map, add another edition to it
        currentEntry.crn().add(row.crn);
        currentEntry.no().put(row.crn, row.no);
        currentEntry.semester().put(row.crn, row.srcdb);
        currentEntry.instructorID().put(row.crn, addProfessorsForCourse(row.crn, row.srcdb));
      }
      System.out.println("Processed course " + courseCounter + "/" + courseCount);
      courseCounter++;
    }
    System.out.println("Didn't add " + notAdded + " courses.");
  }

  private List<String> addProfessorsForCourse(String crn, String srcdb) throws IOException {
    URL requestURL = new URL("https://cab.brown.edu/api/?page=fose&route=details");
    HttpURLConnection clientConnection =
        connect(
            requestURL,
            "{\"key\":\"crn:"
                + crn
                + "\",\"srcdb\":\""
                + srcdb
                + "\",\"userWithRolesStr\":\"!!!!!!\"}");
    Moshi moshi = new Moshi.Builder().build();
    // read data
    JsonAdapter<CabCourseResponse> adapter = moshi.adapter(CabCourseResponse.class);
    CabCourseResponse jsonData =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    if (jsonData == null) {
      throw new NullPointerException();
    }
    if (jsonData.instructordetail_html() == null || jsonData.instructordetail_html().isEmpty()) {
      throw new DatasourceException("Professor is empty");
    }
    ArrayList<String> professors = new ArrayList<>();
    Matcher regexMatcher = professorRegex.matcher(jsonData.instructordetail_html());
    while (regexMatcher.find()) {
      if (regexMatcher.groupCount() == 2) {
        // The first group is the professor ID, the second is the professor's name
        Professor professor = new Professor(regexMatcher.group(1), regexMatcher.group(2));
        professors.add(professor.id());
        this.professors.put(professor.id(), professor);
      }
    }
    return professors;
  }

  /**
   * Helper method for requesting a specific URL. Copied from the class livecode example.
   *
   * @param requestURL URL to request
   * @return HTTPURLConnection
   * @throws DatasourceException if the response code isn't OK
   * @throws IOException if there is some other error calling the API
   */
  private HttpURLConnection connect(URL requestURL, String payload)
      throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection clientConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    clientConnection.setRequestProperty("Content-Type", "application/json");
    clientConnection.setRequestProperty("Accept", "application/json");
    if (payload != null && !payload.isEmpty()) {
      clientConnection.setDoOutput(true);
      OutputStream stream = clientConnection.getOutputStream();
      stream.write(payload.getBytes(StandardCharsets.UTF_8));
      stream.close();
    }
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200) {
      if (clientConnection.getResponseCode() == 429) {
        System.out.println("Rate limited");
        try {
          Thread.sleep(10 * 1000);
        } catch (Exception ignored) {

        }
        return connect(requestURL, payload);
      }
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }

  public Collection<Course> getCourses() {
    return courses.values();
  }

  public Collection<Professor> getProfessors() {
    return professors.values();
  }
}
