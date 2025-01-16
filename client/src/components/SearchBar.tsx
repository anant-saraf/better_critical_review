import React from "react";

// Define the types for the searchType prop
type SearchBarProps = {
  searchType: "all" | "course" | "professor";
  searchTerm: string; // To bind the value of the search input
  onSearchTermChange: (e: React.ChangeEvent<HTMLInputElement>) => void; // Handle input change
};

const SearchBar: React.FC<SearchBarProps> = ({
  searchType,
  searchTerm,
  onSearchTermChange,
}) => {
  return (
    <div>
      <input
        type="text"
        value={searchTerm} // Bind the value to the state
        onChange={onSearchTermChange} // Update the state when the user types
        placeholder={`Search based on ${searchType.toLowerCase()}...`} // Dynamic placeholder based on search type
        className="search-bar"
      />
    </div>
  );
};

export default SearchBar;
