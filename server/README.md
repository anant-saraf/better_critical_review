# Project Details
- Team members: Christine Parker & Ilija Ivanov
- Contributors: We used some code from the gearup and the lecture repo as a template for setting up the server and testing
- Repo: https://github.com/cs0320-f24/server-cparke28-iivanov

# Design Choices
- We used handlers for each endpoint
- Load, view, and search all have a shared state (searcher), which allows for information about the file to be shared between them
- We added methods that made it easier to load the csv in the searcher class, as well as a getter for the entire file including headers
- We used Google Guava to implement caching the acs api responses
- We used a proxy class for caching and passed the mock/real datasource classes into this proxy
- We used a hashmap to keep track of the state codes so we don't have to query multiple times for them

# Errors/Bugs
None that we know of

# Tests
- loadcsv with a successful test, one without parameters, a nonexistent file, file outside of data/, and loading multiple
- viewcsv with successful test, a file that hasn't been loaded
- searchcsv with the column name given, with the column index given, with no column given, with the target not found
- mixed interaction between csv endpoints
- mock broadband with a successful run, one with an empty query, and one with a failing query
- broadband with a successful run (different than mock data) and an invalid query, query with additional parameters

# How to
Run the server with the endpoints: loadcsv, viewcsv, searchcsv, or broadband.
Loadcsv takes queries for the filepath and if the file has headers.
Search csv takes the target word and a column identifier for the search.
Broadband takes queries for the state and county (along with any additional parameters needed from the ACS API), and viewcsv doesn't take any query parameters.

After inputting these queries, the server will return the appropriate response values.




CAB API:

Scraped fall 2024: 2013 sections list size, 665 courses not added, 1357 courses

Search for courses: https://cab.brown.edu/api/?page=fose&route=search&is_ind_study=N&is_canc=N
Payload: {"other":{"srcdb":"202420"},"criteria":[{"field":"is_ind_study","value":"N"},{"field":"is_canc","value":"N"}]}
The results field from the response will get us what we want

What we need from CAB API:
code
crn
title
no - ex. S01 and S02 , professor could be different
srcdb is the semester
202400 - Summer 2024
202410 - Fall 2024
202415 - Winter 2025
202420 - Spring 2025
Any term in the current academic year - 999999
There are different entries for different sections for the same class
If searching for any term in the current academic year, there are different entries for the class if it's offered in both semesters

Search for a specific course: https://cab.brown.edu/api/?page=fose&route=details
Payload: {"group":"code:APMA 1650","key":"crn:28057","srcdb":"202420","matched":"crn:28057","userWithRolesStr":"!!!!!!"}
crn code changes by year, it is required for searching on the API

Details (of each course):
Professor Name- instructordetail_html
Looks like
<div class="instructor"><div class="instructor-detail"><div class="instructor-name"><h4><a href="#" data-action="search" data-search-data-provider="search-by-instructor" data-id="2581">Matthew Guterl</a></h4></div><p class="truncate"><a href="mailto:matthew_guterl@brown.edu" target="_blank">matthew_guterl@brown.edu</a></p></div><div class="instructor-info">Professor of Africana Studies and American Studies<br/><a href="https://vivo.brown.edu/display/mguterl" target="_blank">Areas of Research</a></div></div>
we want to store search-by-instructor data id so that we can map professor names to IDs

Make sure to map multiple CRNs to same prof object so that we don't have multiple entries for it

Regex to get the ID and professor name: data\-id="(.*?)".*?>(.*?)<
group 1 will be the id and group 2 will be the name

