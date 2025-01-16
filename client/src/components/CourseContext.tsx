// CourseContext.tsx
import React, {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
} from "react";
import { addUserCourse, getUserCourses } from "./api/Api";
import { useAuth } from "@clerk/clerk-react";

export type ReviewableCourse = {
  courseName: string;
  code: string;
  semester: string;
  crn: string;
  section: string;
  reviewStatus: "PUBLISHED" | "PENDING" | "REVIEW_NOW";
};

type CourseContextType = {
  coursesTable: ReviewableCourse[];
  updateReviewStatus: (
    code: string,
    status: ReviewableCourse["reviewStatus"]
  ) => void;
  addTableCourse: (course: ReviewableCourse) => void;
};

const MAX_COURSE = 5;

const CourseContext = createContext<CourseContextType | undefined>(undefined);

export const CourseProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [coursesTable, setCourses] = useState<ReviewableCourse[]>([]);
  const { getToken } = useAuth();
  const addTableCourse = (course: ReviewableCourse) => {
    setCourses((prevCourses) => {
      const isDuplicate = prevCourses.some(
        (c) => c.code === course.code || c.courseName === course.courseName
      );

      if (isDuplicate) {
        alert("This course is already in your table!"); // Display a warning
        return prevCourses; // Do not add duplicate course
      }

      if (prevCourses.length >= MAX_COURSE) {
        alert("Course limit reached!");
        return prevCourses; // Do not add more courses
      }
      addUserCourse(getToken, course.semester + course.crn);
      return [...prevCourses, course];
    });
  };

  useEffect(() => {
    async function fetchCourses(): Promise<void> {
      const courses = await getUserCourses(getToken);
      if (courses !== undefined) {
        setCourses(courses);
        return;
      }
      throw new Error("Invalid type returned by user profile API");
    }
    fetchCourses();
  }, []);

  // Function to update review status
  const updateReviewStatus = (
    code: string,
    status: ReviewableCourse["reviewStatus"]
  ) => {
    setCourses((prevCourses) =>
      prevCourses.map((course) =>
        course.code === code ? { ...course, reviewStatus: status } : course
      )
    );
  };

  return (
    <CourseContext.Provider
      value={{ coursesTable, addTableCourse, updateReviewStatus }}
    >
      {children}
    </CourseContext.Provider>
  );
};

export const useCourseContext = (): CourseContextType => {
  const context = useContext(CourseContext);
  if (!context) {
    throw new Error("useCourseContext must be used within a CourseProvider");
  }
  return context;
};
