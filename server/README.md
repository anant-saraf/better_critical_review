


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

