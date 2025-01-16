import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
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
  getProfessorData,
  ProfessorCourses,
  semesterToPrettyString,
} from "./api/Api";

const Professor = () => {
  const [professorData, setProfessorData] = useState<ProfessorCourses>();
  const { getToken } = useAuth();
  const { professorID } = useParams();
  useEffect(() => {
    async function fetchData() {
      setProfessorData(await getProfessorData(getToken, professorID!!));
    }
    fetchData();
  }, []);
  return (
    <div className="container">
      {/* ---------- HEADER with NAVIGATION + SIGN-IN ---------- */}
      <div className="header">
        <div className="navigation">
          <Link to="/">
            <button>
              <span>&#8592;</span>{/* <-- arrow */} Return
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
          <div className="professor-info">
          <h1>
            {professorData === undefined
            ? "Loading..."
            : "Professor: " + professorData.name}
          </h1>
          </div>

          <div className="classes">
            <h3>
              {professorData === undefined
                ? "Loading..."
                : "Classes taught by " + professorData.name}
            </h3>
            {professorData === undefined ? (
              <></>
            ) : (
              <div className="table-wrapper">
                <table>
                  {/* ---------- table head ---------- */}
                  <thead>
                    <tr>
                      <th>Class Name</th>
                      <th>Semester</th>
                    </tr>
                  </thead>
                  {/* ---------- table body ---------- */}
                  <tbody>
                    {professorData.offerings.map((val) => (
                      <tr>
                        <td>
                          <a
                            href={`/courses/${val.code}/${val.semester}${val.crn}`}
                          >
                            {val.code}, {val.no}
                          </a>
                        </td>
                        <td>{semesterToPrettyString(val.semester)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </SignedIn>
      </div>

    </div>
  );
};

export default Professor;