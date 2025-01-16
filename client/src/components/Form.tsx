import React, { useState } from "react";
import { Link } from "react-router-dom";
import { SignedIn, SignedOut, useAuth } from "@clerk/clerk-react";
import { useLocation } from "react-router-dom";
import { uploadReview } from "./api/Api";

type RatingProps = {
  name: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
};

const Rating: React.FC<RatingProps> = ({ name, onChange }) => (
  <div className="rating">
    {[1, 2, 3, 4, 5].map((value) => (
      <label key={value}>
        <input
          type="radio"
          name={name}
          value={value}
          onChange={onChange} // Pass the handler correctly
        />
        {value}
      </label>
    ))}
  </div>
);

const Form = () => {
  const [reviewText, setReviewText] = useState("");
  const [year, setYear] = useState("");
  const [courseRating, setCourseRating] = useState("0");
  const [professorRating, setProfessorRating] = useState("0");
  const [concentrator, setConcentrator] = useState("false");
  const [hours, setHours] = useState("0");
  const [submitted, setSubmitted] = useState(false); // New state to track form submission
  const location = useLocation();
  const { getToken } = useAuth();

  const courseName = location.state?.courseName || "Course"; // Fallback if no course name is passed
  const semesterCrn = location.state?.semesterCrn || "0";
  if (semesterCrn === "0" || courseName === "Course") {
    return <div>Course to review not supplied.</div>;
  }

  const handleReviewTextChange = (
    event: React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    setReviewText(event.target.value);
  };

  const handleSubmitReview = () => {
    const reviewData = {
      courseName: courseName,
      reviewText: reviewText,
      year: year,
      courseRating: courseRating,
      professorRating: professorRating,
      concentrator: concentrator,
      hours: hours,
      karma: "0",
      semestercrn: semesterCrn,
      uniqueID: "",
    };

    // Retrieve existing reviews from local storage
    const existingReviews = JSON.parse(localStorage.getItem("reviews") || "[]");

    // Add new review
    existingReviews.push(reviewData);

    // Save back to local storage
    localStorage.setItem("reviews", JSON.stringify(existingReviews));

    uploadReview(getToken, reviewData);

    // Set submitted state to true after submission
    setSubmitted(true);
  };

  return (
    <div className="container">
      {/* ---------- HEADER with NAVIGATION + SIGN-IN ---------- */}
      <div className="header">
        <div className="navigation">
          <Link to="/">
            <button>
              <span>&#8592;</span> Quit
            </button>
          </Link>
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
            <h1>Review for {courseName}</h1>
          </div>
          {/* ---------- submitted ---------- */}
          {submitted ? (
            <div className="submitted">
              <h3>Thank you for submitting your review!</h3>
              <p>Your feedback is greatly appreciated.</p>
            </div>
          ) : (
            <div className="review-form">
              <h3>Reviews are not saved until submitted.</h3>
              {/* Grade Level Section */}
              <div className="form-section">
                <div className="radio-group">
                  <strong>Your grade level:</strong>
                  {[
                    "Freshman",
                    "Sophomore",
                    "Junior",
                    "Senior",
                    "Graduate Student",
                  ].map((level, index) => (
                    <label key={index} className="radio-label">
                      <input
                        type="radio"
                        name="gradeLevel"
                        value={index + 1}
                        onChange={(e) => setYear(e.target.value)}
                      />{" "}
                      {level}
                    </label>
                  ))}
                </div>
              </div>

              {/* Concentration Section */}
              <div className="form-section">
                <div className="radio-group">
                  <strong>Is this class in your concentration?</strong>
                  <label className="radio-label">
                    <input
                      type="radio"
                      name="concentration"
                      value="true"
                      onChange={(e) => setConcentrator(e.target.value)}
                    />{" "}
                    Yes, I am a concentrator
                  </label>
                  <label className="radio-label">
                    <input
                      type="radio"
                      name="concentration"
                      value="false"
                      onChange={(e) => setConcentrator(e.target.value)}
                    />{" "}
                    No, I am not a concentrator
                  </label>
                </div>
              </div>

              {/* Hours per Week Section */}
              <div className="form-section">
                <strong>
                  Average hours per week you spent on this class outside of
                  classtime:
                </strong>
                <input
                  type="number"
                  name="hoursPerWeek"
                  min="0"
                  placeholder="e.g., 5"
                  onChange={(e) => setHours(e.target.value)}
                />
              </div>

              {/* Course Feedback Section */}
              <div className="form-section">
                <div className="course-rating">
                  <p>Overall course rating:</p>
                  <Rating
                    name="courseOverall"
                    onChange={(e) => setCourseRating(e.target.value)}
                  />
                </div>

                <div className="instructor-rating">
                  <p>Overall instructor rating:</p>
                  <Rating
                    name="instructorOverall"
                    onChange={(e) => setProfessorRating(e.target.value)}
                  />
                </div>
              </div>

              {/* Comment Section */}
              <div className="form-section">
                <textarea
                  value={reviewText}
                  onChange={handleReviewTextChange}
                  placeholder="Write your review here..."
                  rows={5}
                  cols={40}
                />
              </div>

              {/* Submit Button */}
              <div className="form-section">
                <button onClick={handleSubmitReview}>Submit Review</button>
              </div>
            </div>
          )}
        </SignedIn>
      </div>
    </div>
  );
};

export default Form;
