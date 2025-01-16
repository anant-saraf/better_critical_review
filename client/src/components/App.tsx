import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom"; // Make sure Link is imported
import "../styles/App.css";
import SearchBar from "./SearchBar";
import {
  SignedIn,
  SignedOut,
  SignInButton,
  SignOutButton,
  UserButton,
  useAuth,
} from "@clerk/clerk-react";
import { search, SearchResult } from "./api/Api";
import { CourseProvider } from "./CourseContext";

function App() {
  const { getToken } = useAuth();

  const [showFaq, setShowFaq] = useState(false);
  const [searchType, setSearchType] = useState<"all" | "course" | "professor">(
    "all"
  );
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult | null>(null);
  const [currentSearchTerm, setCurrentSearchTerm] = useState("");
  const [searchMessage, setSearchMessage] = useState(""); // New state for search message
  const [isDarkMode, setIsDarkMode] = useState(false);

  const handleFaqClick = () => {
    setShowFaq(!showFaq);
  };

  const handleSearchTypeChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    if (
      event.target.value !== "all" &&
      event.target.value !== "course" &&
      event.target.value !== "professor"
    ) {
      return;
    }
    setSearchType(event.target.value);
  };

  const handleSearchTermChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setSearchTerm(event.target.value);
    setSearchMessage(""); // Reset message when the user types
  };

  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = searchTerm.trim();

    if (trimmed.length <= 1) {
      setSearchMessage("Please enter a more specific search term");
      setSearchResults(null); // Clear search results when showing the message
      return;
    }

    if (trimmed.length > 100) {
      setSearchMessage("Please enter a shorter search term");
      setSearchResults(null); // Clear search results when showing the message
      return;
    }

    // If valid length, proceed with search
    setSearchMessage(""); // Reset message
    setCurrentSearchTerm(searchTerm);
    setSearchResults(await search(getToken, trimmed));
  };

    // Toggle light/dark mode
    const toggleTheme = () => {
      setIsDarkMode(!isDarkMode);
    };
  
    // Apply dark mode class to body when dark mode is enabled
    useEffect(() => {
      document.body.classList.toggle("dark-mode", isDarkMode);
    }, [isDarkMode]);

  return (
    <div className="container">
      {/* ---------- HEADER with NAVIGATION + SIGN-IN ---------- */}
      <div className="header">
        <div className="navigation">
          {!showFaq ? (
            <>
              <Link to="/review">
                <button>Review</button>
              </Link>
              <p>|</p>
              <button onClick={handleFaqClick}>About</button>
              <p>|</p>
              <Link to="/profile">
                <button>Profile</button>
              </Link>
            </>
          ) : (
            <button onClick={handleFaqClick}>
              <span>&#8592;</span>
              {/* <-- arrow */} Return
            </button>
          )}
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
          {!showFaq ? (
            <div className="title">
              <h1>The Critical Review, But Better</h1>
              <h3>
                Sign in to a <strong>@brown.edu</strong> account to continue.
              </h3>
            </div>
          ) : (
            <div className="about">
              <h1>What is The Critical Review, But Better?</h1>
              <div className="lore">
                <p>
                  This website was designed for a final project in CSCI 0320: Introduction to Software Engineering. With no relation to the actual web developement team behind <strong>The Critcal Review</strong>, we (a group of four sleep-deprived students) coded this website to allow students to leave reviews for classes offered by Brown University. Only students with a @brown.edu email will be able to view and leave reviews. All reviews are moderated before posted. 
                </p>
                <p>
                  Please note that information such as course name, semester offered, professor name, and course description are taken from what is displayed in <strong><a href="https://cab.brown.edu" target="_blank" rel="noopener noreferrer">Courses @ Brown (C@B)</a></strong>. We collect this data infrequently, so information might vary from the listings on C@B. 
                </p>
                <p>For any questions, please contact us at notreal@brown.edu.</p>
              </div>
              <div className="faq">
                <h3>How does this thing even work?</h3>
                <p>
                  Using the search feature on the landing page, users can view Brown courses offered from Fall 2023 to Fall 2024 and see reviews left by other students about the course. To leave a review, students can add up to five courses on their profile page and fill out a review for the classes they have taken. <strong>The Critical Review, But Better</strong> relies on reviews left in good faith! Please do not leave false reviews or reviews on classes you have not taken. The moderation team will ban users who abuse the website. 
                </p>
                <h3>I made a mistake. How do I edit a review?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
                <h3>I made a mistake. How do I edit the courses on my profile?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
                <h3>How do I review more/older classes?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
              </div>
            </div>
          )}
        </SignedOut>
      </div>

      {/* ---------- WHEN SIGNED IN ---------- */}
      <div className="sign-in">
        <SignedIn>
          {!showFaq ? (
            <div className="title">
              <h1>The Critical Review, But Better</h1>

              <div className="search">
                <div className="search-type">
                  <label htmlFor="searchType">Search for: </label>
                  <select
                    id="searchType"
                    value={searchType}
                    onChange={handleSearchTypeChange}
                  >
                    <option value="all">All</option>
                    <option value="course">Courses</option>
                    <option value="professor">Professors</option>
                  </select>
                </div>

                {/* ---------- search bar and button ---------- */}
                <form onSubmit={handleSearchSubmit} className="search-bar">
                  <div className="search-bar-input">
                    <SearchBar
                      searchType={searchType}
                      searchTerm={searchTerm}
                      onSearchTermChange={handleSearchTermChange}
                    />
                  </div>

                  <div className="search-button">
                    <button type="submit">{">"}</button>
                  </div>
                </form>
              </div>

              {/* ---------- current search message ---------- */}
              <div className="current-search">
                {/* Display search message if present */}
                {searchMessage && <p>{searchMessage}</p>}

                {/* Display search results if available */}
                {searchResults && !searchMessage && (
                  <>
                    <p>
                      Currently searching for:{" "}
                      <strong>{currentSearchTerm}</strong>
                    </p>

                    {/* ---------- for courses ---------- */}
                    {searchType !== "professor" && (
                      <div className="course-table">
                        <h2>Courses</h2>
                        {searchResults.courses.length === 0 ? (
                          <p>No results found</p>
                        ) : (
                          <div className="table-wrapper">
                            <table>
                              <thead>
                                <tr>
                                  <th>Code</th>
                                  <th>Course Name</th>
                                </tr>
                              </thead>
                              <tbody>
                                {searchResults.courses.map(
                                  (course, rowIndex) => (
                                    <tr key={rowIndex}>
                                      <td>
                                        <a
                                          href={`/courses/${course.code}/${
                                            course.semester[course.crn[0]] +
                                            course.crn[0]
                                          }`}
                                          target="_blank"
                                          rel="noopener noreferrer"
                                        >
                                          {course.code}
                                        </a>
                                      </td>
                                      <td>
                                        <a
                                          href={`/courses/${course.code}/${
                                            course.semester[course.crn[0]] +
                                            course.crn[0]
                                          }`}
                                          target="_blank"
                                          rel="noopener noreferrer"
                                        >
                                          {course.title}
                                        </a>
                                      </td>
                                    </tr>
                                  )
                                )}
                              </tbody>
                            </table>
                          </div>
                        )}
                      </div>
                    )}

                    {/* ---------- for professors ---------- */}
                    {searchType !== "course" && (
                      <div className="professor-table">
                        <h2>Professors</h2>
                        {searchResults.professors.length === 0 ? (
                          <p>No results found</p>
                        ) : (
                          <div className="table-wrapper">
                            <table>
                              <thead>
                                <tr>
                                  <th>Professor</th>
                                </tr>
                              </thead>
                              <tbody>
                                {searchResults.professors.map(
                                  (professor, rowIndex) => (
                                    <tr key={rowIndex}>
                                      <td>
                                        <a
                                          href={`/professor/${professor.id}`}
                                          target="_blank"
                                          rel="noopener noreferrer"
                                        >
                                          {professor.name}
                                        </a>
                                      </td>
                                    </tr>
                                  )
                                )}
                              </tbody>
                            </table>
                          </div>
                        )}
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          ) : (
            <div className="about">
              <h1>What is The Critical Review, But Better?</h1>
              <div className="lore">
                <p>
                  This website was designed for a final project in CSCI 0320: Introduction to Software Engineering. With no relation to the actual web developement team behind <strong>The Critcal Review</strong>, we (a group of four sleep-deprived students) coded this website to allow students to leave reviews for classes offered by Brown University. Only students with a @brown.edu email will be able to view and leave reviews. All reviews are moderated before posted. 
                </p>
                <p>
                  Please note that information such as course name, semester offered, professor name, and course description are taken from what is displayed in <strong><a href="https://cab.brown.edu" target="_blank" rel="noopener noreferrer">Courses @ Brown (C@B)</a></strong>. We collect this data infrequently, so information might vary from the listings on C@B. 
                </p>
                <p>For any questions, please contact us at notreal@brown.edu.</p>
              </div>
              <div className="faq">
                <h3>How does this thing even work?</h3>
                <p>
                  Using the search feature on the landing page, users can view Brown courses offered from Fall 2023 to Fall 2024 and see reviews left by other students about the course. To leave a review, students can add up to five courses on their profile page and fill out a review for the classes they have taken. <strong>The Critical Review, But Better</strong> relies on reviews left in good faith! Please do not leave false reviews or reviews on classes you have not taken. The moderation team will ban users who abuse the website. 
                </p>
                <h3>I made a mistake. How do I edit a review?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
                <h3>I made a mistake. How do I edit the courses on my profile?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
                <h3>How do I review more/older classes?</h3>
                <p>
                  You can't, sorry. This feature will possibly be added in the future. 
                </p>
              </div>
            </div>
          )}
          <h2>
            <Link to="/moderate">Moderate</Link>
          </h2>
        </SignedIn>
      </div>
      
      <button
        className="theme-toggle"
        onClick={toggleTheme}
        aria-label="Toggle Light/Dark Mode"
      >
        {isDarkMode ? "🌙" : "🌞"}
      </button>

      <div id="modal-root"></div>
    </div>
  );
}

export default App;
