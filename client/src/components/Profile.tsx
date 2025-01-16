import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  SignedIn,
  SignedOut,
  SignInButton,
  SignOutButton,
  UserButton,
  useAuth,
} from "@clerk/clerk-react";
import {
  search,
  getOffering,
  Course,
  CourseOffering,
  semesterToPrettyString,
  ReviewableCourse,
} from "./api/Api";
import { CourseProvider, useCourseContext } from "./CourseContext";

const Profile: React.FC = () => {
  const { getToken } = useAuth();
  const { coursesTable, addTableCourse } = useCourseContext(); // Consume context here

  // Search state
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [searchResults, setSearchResults] = useState<Course[]>([]);
  const [offerings, setOfferings] = useState<Map<string, CourseOffering[]>>(
    new Map()
  );
  const [searchMessage, setSearchMessage] = useState<string>("");

  // Handle search term change
  const handleSearchTermChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    setSearchMessage(""); // Reset message on typing
  };

  // Handle search submission
  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedTerm = searchTerm.trim();

    if (trimmedTerm.length <= 1) {
      setSearchMessage("Please enter a more specific search term");
      setSearchResults([]); // Clear search results
      return;
    }

    if (trimmedTerm.length > 100) {
      setSearchMessage("Please enter a shorter search term");
      setSearchResults([]); // Clear search results
      return;
    }

    setSearchMessage(""); // Clear message on valid search
    const results = await search(getToken, trimmedTerm);
    setSearchResults(results.courses);

    if (results.courses.length === 0) {
      setSearchMessage("No results found for your search term");
    }
  };

  // Fetch course offerings for a particular course
  const getCourseOfferings = async (course: Course): Promise<void> => {
    if (offerings.get(course.code) !== undefined) {
      return;
    }

    const semesterCRNList = generateSemesterCRN(course.semester, course.crn); // Generate semesterCRN list

    // Fetch offerings for the course from the API
    const courseOfferings = await Promise.all(
      semesterCRNList.map(async (semcrn) => {
        try {
          const offering = await getOffering(getToken, course.code, semcrn);
          return offering;
        } catch (error) {
          console.error(`Error fetching offering for ${semcrn}`, error);
          return null;
        }
      })
    );

    setOfferings(
      new Map(
        offerings.set(
          course.code,
          courseOfferings.filter((offering) => offering !== null)
        )
      )
    );
  };

  // Generate semester + CRN strings
  const generateSemesterCRN = (
    semesterMap: Map<string, string>,
    crnList: string[]
  ): string[] => {
    const semesterCRNList: string[] = [];
    for (const crn of crnList) {
      const semester = semesterMap[crn];
      if (semester !== undefined) {
        semesterCRNList.push(`${semester}${crn}`);
      }
    }
    return semesterCRNList;
  };

  // Add a course to the profile table
  const addCourse = (courseCode: string) => {
    const selectElement = document.getElementById(
      courseCode
    ) as HTMLSelectElement;
    if (selectElement) {
      const semesterCrn = selectElement.selectedOptions[0].value;
      if (semesterCrn !== "not-selected") {
        const offering = offerings
          .get(courseCode)
          ?.find(
            (val) =>
              val.semester === semesterCrn.substring(0, 6) &&
              val.crn === semesterCrn.substring(6)
          );
        if (offering) {
          const reviewableCourse: ReviewableCourse = {
            courseName: offering.title,
            code: offering.code,
            semester: offering.semester,
            crn: offering.crn,
            section: offering.no,
            reviewStatus: "REVIEW_NOW",
          };
          addTableCourse(reviewableCourse); // Add course to the context
          setSearchTerm(""); // Clear search input
          setSearchResults([]); // Clear search results
        } else {
          console.log(`Could not find semesterCrn ${semesterCrn}`);
        }
      }
    }
  };

  return (
    <div className="container">
      {/* ---------- HEADER with NAVIGATION + SIGN-IN ---------- */}
      <div className="header">
        <div className="navigation">
          <Link to="/">
            <button>
              <span>&#8592;</span> Return
            </button>
          </Link>
        </div>
        <div className="sign-in-status">
          <SignedOut>
            <SignInButton>Sign In</SignInButton>
          </SignedOut>

          <SignedIn>
            <SignOutButton>Sign Out</SignOutButton>
            <UserButton />
          </SignedIn>
        </div>
      </div>

      {/* ---------- WHEN SIGNED OUT ---------- */}
      <div className="sign-out">
        <SignedOut>
          <div className="title">
            <h1>The Critical Review, But Better</h1>
            <h3>
              Sign in to a <strong>@brown.edu</strong> account to view profile.
            </h3>
          </div>
        </SignedOut>
      </div>

      {/* ---------- WHEN SIGNED IN ---------- */}
      <div className="sign-in">
        <SignedIn>
          <div className="title">
            <h1>My Profile and Classes Taken</h1>
            <h3>
              All reviews are marked as anonymous for the safety and privacy of
              our users.
            </h3>

            <div className="classes-taken">
              <p>
                Select a class to write a review for or edit an existing review.
              </p>

              {/* ---------- search bar ---------- */}
              <div className="search">
                <form onSubmit={handleSearchSubmit} className="search-bar">
                  <div className="search-bar-input">
                    <input
                      type="text"
                      value={searchTerm}
                      onChange={handleSearchTermChange}
                      placeholder="Search for a class to add..."
                    />
                  </div>
                  <div className="search-button">
                    <button type="submit">{">"}</button>
                  </div>
                </form>
              </div>

              {/* ---------- search message ---------- */}
              {searchMessage && <p>{searchMessage}</p>}

              {/* ---------- search results ---------- */}
              {searchResults.length > 0 && !searchMessage && (
                <div className="search-results">
                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Course Name</th>
                          <th>Semester Offered</th>
                          <th>Add</th>
                        </tr>
                      </thead>
                      <tbody>
                        {searchResults.map((result, index) => (
                          <tr key={index}>
                            <td>
                              {result.code} : {result.title}
                            </td>
                            <td>
                              <select
                                id={result.code}
                                onClick={() => getCourseOfferings(result)}
                              >
                                <option value={"not-selected"}>
                                  Select a semester...
                                </option>
                                {offerings
                                  .get(result.code)
                                  ?.map((offering, idx) => (
                                    <option
                                      key={idx}
                                      value={offering.semester + offering.crn}
                                    >
                                      {semesterToPrettyString(
                                        offering.semester
                                      )}
                                      , {offering.no}
                                    </option>
                                  ))}
                              </select>
                            </td>
                            <td>
                              <button onClick={() => addCourse(result.code)}>
                                +
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* ---------- user-courses ---------- */}
              <div className="user-courses">
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>My Courses</th>
                        <th>Review Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {coursesTable.map((course, index) => (
                        <tr key={index}>
                          <td>
                            {course.courseName},{" "}
                            {semesterToPrettyString(course.semester)},{" "}
                            {course.section}
                          </td>
                          <td>
                            {course.reviewStatus === "REVIEW_NOW" ? (
                              <strong>
                                <Link
                                  to={{
                                    pathname: "/form",
                                  }}
                                  state={{
                                    courseName: course.courseName,
                                    semesterCrn: course.semester + course.crn,
                                  }}
                                >
                                  Review Now
                                </Link>
                              </strong>
                            ) : course.reviewStatus === "PENDING" ? (
                              "Pending approval"
                            ) : (
                              "Published"
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <p>
                Pending reviews are sent to our moderation team before getting
                published. Moderation can take up to a week.
              </p>
            </div>
          </div>
        </SignedIn>
      </div>
    </div>
  );
};

export default Profile;
