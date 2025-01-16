// main.tsx
import React from "react";
import ReactDOM from "react-dom/client";
import { ClerkProvider } from "@clerk/clerk-react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import App from "./components/App";
import Review from "./components/Review";
import Profile from "./components/Profile";
import MockCourse from "./components/MockCourse"; // Import MockCourse
import MockProfessor from "./components/MockProfessor"; // Import MockProfessor
import Course from "./components/Course";
import Form from "./components/Form";
import Moderate from "./components/Moderate";
import { CourseProvider } from "./components/CourseContext";
import Professor from "./components/Professor";

//   TODO: MODIFY FORM TO THIS: <Route path="/form/:semestercrn" element={<Form />} />

const PUBLISHABLE_KEY = import.meta.env["VITE_CLERK_PUBLISHABLE_KEY"];

const rootElement = document.getElementById("root") as HTMLElement;

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <ClerkProvider publishableKey={PUBLISHABLE_KEY} afterSignOutUrl="/">
      <Router>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/review" element={<Review />} />
          <Route
            path="/profile"
            element={
              <CourseProvider>
                <Profile />
              </CourseProvider>
            }
          />
          <Route path="/course" element={<MockCourse />} /> {/* TODO: remove */}
          <Route path="/courses/:courseID/:crn" element={<Course />} />
          <Route path="/professor/:professorID" element={<Professor />} />
          <Route path="/form" element={<Form />} />
          <Route path="/moderate" element={<Moderate />} />
        </Routes>
      </Router>
    </ClerkProvider>
  </React.StrictMode>
);
