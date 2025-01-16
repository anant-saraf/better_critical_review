import React, { useState } from "react";
import { Link } from "react-router-dom"; // Import Link for navigation
import {
  SignedIn,
  SignedOut,
  SignInButton,
  SignOutButton,
  UserButton,
  useAuth,
} from "@clerk/clerk-react";
import { useEffect } from "react";
import {
  getUnmoderatedReviews,
  moderateReview,
  ModerationReviewData,
  ReviewData,
  uploadReview,
} from "./api/Api";
import { useLocation } from "react-router-dom";


const Moderate = () => {
  const [concentrator, setConcentrator] = useState("false");
  const { getToken } = useAuth();
  const [reviews, setReviews] = useState<ModerationReviewData[]>([]);

  useEffect(() => {
    async function fetchData() {
      setReviews(await getUnmoderatedReviews(getToken));
    }
    fetchData();
  }, []);

  const handleApprovalChange = (index: number) => {
    const updatedReviews = [...reviews];
    updatedReviews[index].isApproved = !updatedReviews[index].isApproved;
    setReviews(updatedReviews);
  };

  const handleSubmitChange = async (index: number) => {
    const updatedReviews = [...reviews];
    await moderateReview(
      getToken,
      reviews[index].uniqueID,
      reviews[index].isApproved
    );
    const [processedReview] = updatedReviews.splice(index, 1); // Remove the submitted review
    setReviews(updatedReviews);

    console.log("Processed Review:", processedReview);
  };

  // const handleSubmitChange = (index: number) => {
  //   const updatedReviews = [...reviews];
  //   updatedReviews[index].submit = !updatedReviews[index].submit;
  //   setReviews(updatedReviews);
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
              <h3>
                Sign in to a <strong>@brown.edu</strong> account to moderate.
              </h3>
            </div>
          </SignedOut>
        </div>
          
      
        {/* ---------- WHEN SIGNED In ---------- */}
        <div className="sign-in">
          <SignedIn>
          <h1>Approve or Deny Reviews</h1>
          <table>
            <thead>
              <tr>
                <th>Submission Time</th>
                <th>Course</th>
                <th>Review</th>
                <th>Status</th>
                <th>Submit</th>
              </tr>
            </thead>
            <tbody>
              {reviews.map((review, index) => (
                <tr key={index}>
                  <td>{review.submitTime.toLocaleString()}</td>
                  <td>{review.prettyTitle}</td>
                  <td>{review.reviewText}</td>
                  <td>
                    <button onClick={() => handleApprovalChange(index)}>
                      {review.isApproved ? "Approved" : "Not Approved"}
                    </button>
                  </td>
                  <td>
                    <button
                      onClick={() => {
                        handleSubmitChange(index);
                      }}
                    >
                      Submit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </SignedIn>
        </div>
    </div>
  );

  
};

export default Moderate;
