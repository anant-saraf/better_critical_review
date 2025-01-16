import React, { useState } from "react";
import { Link } from "react-router-dom"; // Import Link for navigation
import { SignedIn, SignedOut, SignInButton, SignOutButton, UserButton } from "@clerk/clerk-react";

const Review = () => {
  {/* FEATURES TO IMPLEMENT IN THE FUTURE */}
  // const [moderationChoice, setModerationChoice] = useState(""); // State to track moderation choice
  // const [showCourseTable, setShowCourseTable] = useState(false); // State to show/hide course table
  // const [showProfessorTable, setShowProfessorTable] = useState(false); // State to show/hide professor table

  // const handleModerationChange = (event) => {
  //   setModerationChoice(event.target.value); // Update state based on selected option
  // };

  // const handleReviewCourses = () => {
  //   setShowCourseTable(true); // Show the course table when the button is clicked
  // };

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
            <h3>Sign in to a <strong>@brown.edu</strong> account to leave reviews.</h3>
          </div>
        </SignedOut>
      </div>

      {/* ---------- WHEN SIGNED IN ---------- */} 
      <div className="sign-in">
        <SignedIn>
          <div className="title">
            <h1>The Critical Review, But Better</h1>
            <h3>All reviews will be moderated and only published once approved.</h3>
          </div>

          <div className="empty-profile">
            <h3>
              <Link to="/profile"><button>Review</button></Link>
            </h3>
          </div>

          {/* FEATURES TO IMPLEMENT IN THE FUTURE */}
          {/* <div className="review-courses">
            {coursesTable.length > 0 ? (
              <div className="user-courses">

                {/* ---------- user courses table ---------- 
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>My Courses</th>
                        <th>Semester</th>
                      </tr>
                    </thead>
                    <tbody>
                      {coursesTable.map((course, index) => (
                        <tr key={index}>
                          <td>
                            <Link
                              to={{
                                pathname: "/form",
                              }}
                              state={{ courseName: course.courseName }}
                            >
                              {course.courseName}
                            </Link>
                          </td>
                          <td>N/A</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* ---------- add courses ---------- 
                <div className="full-profile">
                  <h3>
                    <Link to="/profile"><button>Add Courses</button></Link>
                  </h3>
                </div>
              </div> 
            ) : (
              <div className="empty-profile">
                <p>
                  Your profile is currently empty. 
                  <br />
                  Save courses to your profile to review them. 
                </p>
                <h3>
                  <Link to="/profile"><button>View Profile</button></Link>
                </h3>
              </div>
            )}
          </div> */}
        </SignedIn>
      </div>
    </div>
  );
};

export default Review;
