# Better Critical Review

## Overview

**Better Critical Review** is a web application designed to allow Brown University students to leave reviews for any class at Brown. This project was developed in collaboration with three teammates, utilizing real course data scraped from the Courses@Brown website. The platform aims to provide students with a resource to share and access detailed feedback about courses and professors, helping them make informed decisions during course selection.

## Key Features
- **Course Reviews:** Students can leave detailed reviews for any class at Brown, including feedback on professors and course structure.
- **Search Functionality:** A robust search algorithm enables students to quickly find courses and professors by name, department, or other criteria.
- **Real-Time Data:** Reviews and course information are dynamically updated using Firebase integration.
- **User Interaction:** Features like karma scores and average review data provide a comprehensive view of course feedback.

## My Contributions
I collaborated with a team of four to deliver a scalable and reliable solution. My key contributions included:

### Backend Development
- **Server API:** Developed a server API backend using Java, SparkJava, and Firebase to scrape, store, and manage reviews and data for over 2,700 courses and 1,600 professors.
- **Web Scraping:** Scraped and deserialized large-scale course and professor data from the Courses@Brown website to create a comprehensive, searchable directory for user reviews.
- **Data Management:** Optimized API queries and data processing to ensure concurrency-safe and efficient data retrieval for a user-centric application.

### Frontend Integration
- **Database Integration:** Integrated Firebase with both the frontend and backend to handle data storage and retrieval for:
  - Storing and displaying all reviews associated with a course.
  - Updating logic for features such as karma scores and the display of average review statistics.
- **Responsive Design:** Collaborated with the team to ensure functionality for a responsive front-end using TypeScript and React, providing dynamic course reviews and seamless user interaction.

### Testing and Reliability
- **Testing Coverage:** Wrote and maintained unit and integration tests for the server, achieving 75-100% test coverage.
- **End-to-End Testing:** Conducted end-to-end testing using Playwright to ensure high reliability and user satisfaction.
- **Logic Enhancements:** Implemented functionality to dynamically update review-related data, ensuring a smooth and intuitive user experience.

## Technologies Used
- **Frontend:** React.js, TypeScript
- **Backend:** Java, SparkJava, Firebase
- **Testing:** JUnit, Playwright

## Collaboration and Learning
This project provided me with valuable experience in:
- Collaborating within a team to tackle complex challenges.
- Applying web scraping techniques to collect large datasets.
- Designing and optimizing a concurrency-safe API for large-scale data management.
- Writing reliable and comprehensive tests to ensure application robustness.

---

Created by Anant Saraf, Ilija Ivanov, Grace Liu, and Zamora McBride
