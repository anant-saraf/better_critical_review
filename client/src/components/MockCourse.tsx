import React, { useState } from "react";
import { Link, useParams } from "react-router-dom"; // Ensure Link is imported from react-router-dom
import "../styles/App.css";
import {
  SignedIn,
  SignedOut,
  SignInButton,
  SignOutButton,
  UserButton,
} from "@clerk/clerk-react";

const Course = () => {
  // Initial bar widths: equal size for all bars
  const initialWidth = 500;
  const initialBarWidth = initialWidth / 5; // Divide the total width equally between the 5 bars
  const [widths, setWidths] = useState([
    initialBarWidth,
    initialBarWidth,
    initialBarWidth,
    initialBarWidth,
    initialBarWidth,
  ]);

  // Color gradient for bars, ranging from light grey to dark grey
  const colors = ["#DEDEDE", "#CCCCCC", "#999999", "#666666", "#222222"];

  // Function to handle the button click and increase the width of the corresponding bar
  const handleClick = (index: number) => {
    const totalWidth = 500; // Total width for all bars (this is fixed)
    const increment = 50; // Amount to increase the clicked bar's width

    setWidths((prevWidths) => {
      const newWidths = [...prevWidths];
      const totalCurrentWidth = prevWidths.reduce(
        (sum, width) => sum + width,
        0
      );

      // Calculate the space left for the other bars
      const spaceForOtherBars = totalWidth - newWidths[index] - increment;

      // Adjust the other bars proportionally
      if (spaceForOtherBars > 0) {
        // Increase the clicked bar's width
        newWidths[index] += increment;

        // Reduce the width of the other bars proportionally to keep the total width constant
        const totalOtherBarsWidth = prevWidths.reduce(
          (sum, width, idx) => (idx !== index ? sum + width : sum),
          0
        );

        // Proportional scaling factor
        const scalingFactor = spaceForOtherBars / totalOtherBarsWidth;

        // Apply the scaling factor to the other bars
        newWidths.forEach((width, idx) => {
          if (idx !== index) {
            newWidths[idx] = width * scalingFactor;
          }
        });
      }

      return newWidths;
    });
  };

  // Mock reviews data
  const reviews = [
    {
      id: 1,
      text: "Great course! Really informative and challenging, but rewarding.",
      upvotes: 10,
      downvotes: 2,
    },
    {
      id: 2,
      text: "The professor is excellent. However, the workload can be overwhelming.",
      upvotes: 8,
      downvotes: 4,
    },
    {
      id: 3,
      text: "Very interesting course, but could use more real-world applications.",
      upvotes: 12,
      downvotes: 3,
    },
    {
      id: 4,
      text: "Loved the course! Learned a lot, but the exams were tough.",
      upvotes: 15,
      downvotes: 1,
    },
    {
      id: 5,
      text: "Good class overall, but the pace was a bit fast at times.",
      upvotes: 7,
      downvotes: 5,
    },
  ];

  return (
    <div className="App-header">
      <div className="Header-menu">
        <Link to="/">Back to Search</Link>
      </div>
      <h1>CSCI 0320: Software Engineering</h1>
      <p>
        <strong>
          <a href="/professor" target="_blank" rel="noopener noreferrer">
            Professor: Nim Telson
          </a>
        </strong>
      </p>

      <SignedIn>
        <div className="left-column">
          <p>
            <strong>Summary: </strong>
            Hi. The following is dummy text borrowed from The Critical Review website.
            “Introduction to Software Engineering” (CSCI0320) is an intermediate
            computer science course. It is a foundational course intended to be
            taken after the introductory sequence, and reviewers highlighted
            “Program Design with Data Structures and Algorithms” (CSCI0200) as
            particularly helpful as a precursor.
          </p>

          {/* Grey Bar Ratio Example */}
          <div style={{ textAlign: "center", marginTop: "30px" }}>
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                marginBottom: "20px",
              }}
            >
              {widths.map((width, index) => (
                <div
                  key={index}
                  style={{
                    backgroundColor: colors[index],
                    width: `${Math.round(width)}px`, // Width is based on the state (in px)
                    height: "50px",
                    margin: "0 2px",
                    transition: "width 0.3s ease", // Smooth transition when adjusting the width
                  }}
                />
              ))}
            </div>
            <div>
              {["1", "2", "3", "4", "5"].map((label, index) => (
                <button
                  key={index}
                  onClick={() => handleClick(index)}
                  style={{ margin: "5px", padding: "10px 15px" }}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="right-column">
          <div className="statistics">
            <p>
              <strong>Course Rating: </strong>
              3.97 out of 5.0
            </p>
            <p>
              <strong>Professor Rating: </strong>
              4.91 out of 5.0
            </p>
            <p>
              <strong>Average Hours: </strong>
              11.80 hours per week
            </p>
            <p>
              <strong>Class Size: </strong>
              about 107 students
            </p>
            <p>
              <strong>Reviews: </strong>
              10
            </p>
          </div>

          <div className="comments">
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr>
                  <th>Review</th>
                  <th>Upvote</th>
                  <th>Downvote</th>
                </tr>
              </thead>
              <tbody>
                {reviews.map((review) => (
                  <tr key={review.id} style={{ borderBottom: "1px solid #ddd" }}>
                    <td>{review.text}</td>
                    <td>
                      <button
                        style={{
                          background: "none",
                          border: "none",
                          cursor: "pointer",
                          color: "green",
                        }}
                      >
                        ↑ {review.upvotes}
                      </button>
                    </td>
                    <td>
                      <button
                        style={{
                          background: "none",
                          border: "none",
                          cursor: "pointer",
                          color: "red",
                        }}
                      >
                        ↓ {review.downvotes}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </SignedIn>

      <SignedOut>
        <p>Sign in to view your profile.</p>
      </SignedOut>

      <SignedIn>
        <SignOutButton>Sign Out</SignOutButton>
      </SignedIn>

      <SignedOut>
        <SignInButton>Sign In</SignInButton>
      </SignedOut>

      <SignedIn>
        <UserButton />
      </SignedIn>
    </div>
  );
};

export default Course;
