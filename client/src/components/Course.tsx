import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom"; // Ensure Link is imported from react-router-dom
import "../styles/App.css";
import {
  SignedIn,
  SignedOut,
  SignInButton,
  SignOutButton,
  useAuth,
  UserButton,
} from "@clerk/clerk-react";
import {
  CourseOffering,
  getCourseDescription,
  getOffering,
  getOfferingReviews,
  KarmaChange,
  ReviewData,
  semesterToPrettyString,
} from "./api/Api";
import { Pie } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { useCourseContext } from "./CourseContext";

// Register chart.js components
ChartJS.register(ArcElement, Tooltip, Legend);

const Course = () => {
  const [reviewList, setReviewList] = useState<Array<ReviewData> | null>(null);
  const { getToken } = useAuth();
  const { courseID, crn } = useParams();
  if (
    courseID === undefined ||
    courseID.length === 0 ||
    crn === undefined ||
    crn.length === 0
  ) {
    return (
      <div>
        <h1>
          Invalid course code or CRN: {courseID}, {crn}
        </h1>
      </div>
    );
  }
  const [courseData, setCourseData] = useState<CourseOffering | undefined>();
  const [otherData, setOtherData] = useState<Array<CourseOffering>>([]);
  const [courseDescription, setCourseDescription] = useState<
    string | undefined
  >(undefined);

  useEffect(() => {
    const fetchData = async () => {
      const data = await getOffering(getToken, courseID, crn);
      const otherOfferings = await Promise.all(
        data.otherOfferings.map(
          async (val) => await getOffering(getToken, courseID, val)
        )
      );
      setCourseData(data);
      setOtherData(otherOfferings);

      setCourseDescription(
        await getCourseDescription(
          getToken,
          crn.substring(6),
          crn.substring(0, 6)
        )
      );

      console.log("successfully fetched data");
    };

    // TODO: Error handling
    fetchData();
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      if (courseData !== undefined) {
        const reviews = await getOfferingReviews(
          getToken,
          courseData?.semester + courseData?.crn
        );
        setReviewList(reviews);
      }
    };
    fetchData();
  }, [courseData]);

  const [averageCourseRating, setAverageCourseRating] = useState<number>(0);
  const [averageProfessorRating, setAverageProfessorRating] =
    useState<number>(0);
  const [averageHours, setAverageHours] = useState<number>(0);

  const [classYearCounts, setClassYearCounts] = useState({
    Freshmen: 0,
    Sophomore: 0,
    Junior: 0,
    Senior: 0,
    Graduate: 0,
  });

  const [concentratorCount, setConcentratorCount] = useState(0);
  const [nonConcentratorCount, setNonConcentratorCount] = useState(0);

  const yearMapping = {
    1: "Freshmen",
    2: "Sophomore",
    3: "Junior",
    4: "Senior",
    5: "Graduate",
  };

  useEffect(() => {
    if (reviewList && reviewList.length > 0) {
      // Calculate averages
      const totalCourseRating = reviewList.reduce(
        (sum, review) => sum + Number(review.courseRating),
        0
      );
      const totalProfessorRating = reviewList.reduce(
        (sum, review) => sum + Number(review.professorRating),
        0
      );
      const totalHours = reviewList.reduce(
        (sum, review) => sum + Number(review.hours),
        0
      );

      setAverageCourseRating(totalCourseRating / reviewList.length);
      setAverageProfessorRating(
        Math.round(totalProfessorRating / reviewList.length)
      );
      setAverageHours(Math.round(totalHours / reviewList.length));

      // Count concentrators and non-concentrators
      const concentrators = reviewList.filter(
        (review) => review.concentrator === "true"
      ).length;
      const nonConcentrators = reviewList.length - concentrators;

      setConcentratorCount(concentrators);
      setNonConcentratorCount(nonConcentrators);

      //count class years based on mapping
      const yearCounts = reviewList.reduce(
        (counts, review) => {
          const yearName = yearMapping[Number(review.year)];
          if (yearName && counts[yearName] !== undefined) {
            counts[yearName] += 1;
          }
          return counts;
        },
        { Freshmen: 0, Sophomore: 0, Junior: 0, Senior: 0, Graduate: 0 }
      );

      setClassYearCounts(yearCounts);
    } else {
      // Reset everything if reviewList is empty or null
      setAverageCourseRating(0);
      setAverageProfessorRating(0);
      setAverageHours(0);
      setConcentratorCount(0);
      setNonConcentratorCount(0);
      setClassYearCounts({
        Freshmen: 0,
        Sophomore: 0,
        Junior: 0,
        Senior: 0,
        Graduate: 0,
      });
    }
  }, [reviewList]);

  const handleKarmaChange = async (
    upvote: boolean,
    semestercrn: string,
    uniqueID: string
  ) => {
    try {
      const newKarma = await KarmaChange(
        getToken,
        upvote,
        semestercrn,
        uniqueID
      );

      // Update the reviewList with the new karma
      setReviewList(
        (prevReviews) =>
          prevReviews?.map((review) =>
            review.uniqueID === uniqueID
              ? { ...review, karma: newKarma }
              : review
          ) || null
      );
    } catch (error) {
      console.error("Error updating karma:", error);
    }
  };

  // Bar color gradient from light grey (1 star) to dark grey (5 stars)
  const colors = ["#DEDEDE", "#CCCCCC", "#999999", "#666666", "#222222"];

  // Demographic Mock Data
  const demographicData = {
    labels: ["Freshmen", "Sophomore", "Junior", "Senior", "Graduate"],
    datasets: [
      {
        data: [
          classYearCounts.Freshmen,
          classYearCounts.Sophomore,
          classYearCounts.Junior,
          classYearCounts.Senior,
          classYearCounts.Graduate,
        ],
        backgroundColor: [
          "#FF6384",
          "#36A2EB",
          "#FFCE56",
          "#6B8E23",
          "#A17BB9",
        ],
        hoverBackgroundColor: [
          "#FF3B4D",
          "#4F90D4",
          "#FFB734",
          "#8B9A1F",
          "#301934",
        ],
      },
    ],
  };

  // Concentrator Mock Data
  const concentratorData = {
    labels: ["Concentrator", "Non-Concentrator"],
    datasets: [
      {
        data: [concentratorCount, nonConcentratorCount],
        backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#6B8E23"],
        hoverBackgroundColor: ["#FF3B4D", "#4F90D4", "#FFB734", "#8B9A1F"],
      },
    ],
  };

  // // Requirement Mock Data
  // const requirementData = {
  //   labels: ["Yes", "No"],
  //   datasets: [
  //     {
  //       data: [5, 0],
  //       backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#6B8E23"],
  //       hoverBackgroundColor: ["#FF3B4D", "#4F90D4", "#FFB734", "#8B9A1F"],
  //     },
  //   ],
  // };

  return (
    <div className="container">
      {/* ---------- HEADER with NAVIGATION + SIGN-IN ---------- */}
      <div className="header">
        <div className="navigation">
          <Link to="/">
            <button>
              <span>&#8592;</span>
              {/* <-- arrow */} Return
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
              Sign in to a <strong>@brown.edu</strong> account to continue.
            </h3>
          </div>
        </SignedOut>
      </div>

      {/* ---------- WHEN SIGNED IN ---------- */}
      <div className="sign-in">
        <SignedIn>
          <div className="course-info">
            <h1>
              {courseData?.code}: {courseData?.title}
            </h1>

            {/* ---------- semester ---------- */}
            <div className="semester-select">
              <label> Viewing reviews for: </label>
              <div className="semester-type">
                {courseData === undefined ? (
                  <></>
                ) : courseData.otherOfferings.length === 0 ? (
                  (courseData.no, semesterToPrettyString(courseData.semester))
                ) : (
                  <select
                    onChange={(e) => {
                      window.history.pushState(
                        null,
                        "Course",
                        otherData[e.target.selectedIndex - 1].semester +
                          otherData[e.target.selectedIndex - 1].crn
                      );
                      let newOtherData = otherData.concat(courseData);
                      newOtherData.splice(e.target.selectedIndex - 1, 1);
                      newOtherData = newOtherData.sort((a, b) =>
                        a.crn.localeCompare(b.crn)
                      );
                      setCourseData(otherData[e.target.selectedIndex - 1]);
                      setOtherData(newOtherData);
                      e.target.selectedIndex = 0;
                    }}
                  >
                    <option>
                      {courseData.no},{" "}
                      {semesterToPrettyString(courseData.semester)}
                    </option>
                    {otherData.map((offering) => (
                      <option>
                        {offering.no},{" "}
                        {semesterToPrettyString(offering.semester)}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            </div>
          </div>

          {/* ---------- split columns ---------- */}
          <div className="columns">
            {/* ---------- LEFT COLUMN ---------- */}
            <div className="left-column">
              {/* ---------- professor ---------- */}
              <div className="professor">
                <h3>
                  {courseData?.professors.length === 1 ? (
                    <a
                      href={`/professor/${courseData.professors[0].id}`}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Professor: {courseData.professors[0].name}
                    </a>
                  ) : (
                    <a>
                      Professors:{" "}
                      {courseData?.professors.map((prof, idx) => (
                        <a
                          href={`/professor/${prof.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          {idx === courseData?.professors.length - 1
                            ? prof.name
                            : prof.name + ", "}
                        </a>
                      ))}
                    </a>
                  )}
                </h3>
              </div>

              {/* ---------- course description ---------- */}
              <div className="course-description">
                <h3>Course Description:</h3>
                <p>
                  {courseDescription === undefined
                    ? "Loading..."
                    : courseDescription}
                </p>
              </div>

              {/* ---------- comments ---------- */}
              <div className="comments">
                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                  <thead>
                    <tr>
                      <th>User Stats</th>
                      <th>Reviews</th>
                      <th>Likes</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reviewList &&
                      reviewList.map((review) => (
                        <tr
                          key={review.courseRating}
                          style={{ borderBottom: "1px solid #ddd" }}
                        >
                          <td>
                            <p>
                              <strong>Year: </strong>
                              {yearMapping[Number(review.year)]}
                            </p>
                            <p>
                              <strong>Concentrator: </strong>
                              {review.concentrator ? "Y" : "N"}
                            </p>
                            <p>
                              <strong>Course Rating: </strong>
                              {review.courseRating}/5
                            </p>
                            <p>
                              <strong>Professor Rating: </strong>
                              {review.professorRating}/5
                            </p>
                            <p>
                              <strong>Hours Spent: </strong>
                              {review.hours} per week
                            </p>
                          </td>
                          <td>{review.reviewText}</td>
                          <td>
                            <button
                              onClick={() =>
                                handleKarmaChange(
                                  true,
                                  review.semestercrn,
                                  review.uniqueID
                                )
                              }
                              style={{
                                background: "none",
                                border: "none",
                                cursor: "pointer",
                                color: "green",
                              }}
                            >
                              ↑
                            </button>
                            {Number(review.karma)}{" "}
                            {/* TODO: Check if my math/logic is correct pls thanku */}
                            <button
                              onClick={() =>
                                handleKarmaChange(
                                  false,
                                  review.semestercrn,
                                  review.uniqueID
                                )
                              }
                              style={{
                                background: "none",
                                border: "none",
                                cursor: "pointer",
                                color: "red",
                              }}
                            >
                              ↓
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* ---------- RIGHT COLUMN ---------- */}
            <div className="right-column">
              {/* ---------- statistics ---------- */}
              <div className="statistics">
                <h3>Statistics</h3>
                <p>
                  <strong>Course Rating: </strong>
                  {averageCourseRating.toFixed(2)} out of 5.00
                </p>
                <p>
                  <strong>Professor Rating: </strong>
                  {averageProfessorRating.toFixed(2)} out of 5.00
                </p>
                <p>
                  <strong>Average Hours: </strong>
                  {averageHours.toFixed(2)} hours per week
                </p>

                <p>
                  <strong>Reviews: </strong>
                  {reviewList && reviewList.length}
                </p>
              </div>

              {/* ---------- demographics ---------- */}
              <div className="demographics">
                <h3>Year Breakdown: </h3>
                <div style={{ width: "300px", height: "300px" }}>
                  <Pie data={demographicData} />
                </div>

                <h3>Concentrator Breakdown: </h3>
                <div style={{ width: "300px", height: "300px" }}>
                  <Pie data={concentratorData} />
                </div>
                {/* <h3>Required class</h3>
                  <div style={{ width: "300px", height: "300px" }}>
                    <Pie data={requirementData} />
                  </div> */}
              </div>
            </div>
          </div>
        </SignedIn>
      </div>
    </div>
  );
};

export default Course;

/* ---------------------------------------- */

{
  /* <div className="data">
    {Object.keys(mockRatings).map((category) => {
      const ratings = mockRatings[category];
      return (
        <div key={category} style={{ marginBottom: "30px" }}>
          <h3>{category}</h3>
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              marginBottom: "10px",
            }}
          >
            {ratings.map((rating, index) => {
              const barWidth = (rating / totalReviews) * 500; // Calculate bar width as percentage of total reviews
              return (
                <div
                  key={index}
                  style={{
                    backgroundColor: colors[index],
                    width: `${Math.round(barWidth)}px`,
                    height: "20px",
                    margin: "0 2px",
                    transition: "width 0.3s ease",
                  }}
                />
              );
            })}
          </div>
        </div>
      );
    })}
  </div> */
}
