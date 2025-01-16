import React from "react";
import { Link } from "react-router-dom"; 
import "../styles/App.css";
import { SignedIn, SignedOut, SignInButton, SignOutButton, UserButton } from "@clerk/clerk-react";

const MockProfessor = () => {
  return (
    <div className="App-header">
      <div className="Header-menu">
        <Link to="/">Back to Search</Link>
      </div>

      <h1>Professor: Nim Telson</h1>

      <SignedIn>
        <div className="biography">
            <p>
              <strong>Status: </strong>
              Full time faculty (idk if we have this information we can remove it if we want or maybe show how many years they have taught at Brown?)
            </p>
            <p>
              <strong>About the professor: </strong>
              Nim Telson got his PhD from a school. Idk if this information is actually on CAB... we can remove this biography part if we want. 
            </p>
        </div>

        <div className="right-column">
          <div className="classes">
            <h3>Classes Taught by Professor Nim Telson</h3>
            <table>
              <thead>
                <tr>
                  <th>Class Name</th>
                  <th>Semester</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>CSCI 0320: Introduction to Software Engineering</td>
                  <td>Fall 2024</td>
                </tr>
                <tr>
                  <td>CSCI 0320: Introduction to Software Engineering</td>
                  <td>Spring 2025</td>
                </tr>
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

export default MockProfessor;
