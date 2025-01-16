export interface Course {
  code: string;
  title: string;
  crn: Array<string>;
  semester: Map<string, string>;
}

export interface ReviewableCourse {
  courseName: string;
  code: string;
  semester: string;
  crn: string;
  section: string;
  reviewStatus: "PUBLISHED" | "PENDING" | "REVIEW_NOW";
}

interface Professor {
  id: string;
  name: string;
}

export interface ProfessorCourses {
  name: string;
  offerings: Array<CourseOffering>;
}

export interface ReviewData {
  karma: string;
  reviewText: string;
  concentrator: string;
  courseRating: string;
  professorRating: string;
  hours: string;
  year: string;
  semestercrn: string;
  uniqueID: string;
  prettyTitle?: string;
}

export interface ModerationReviewData {
  karma: string;
  reviewText: string;
  concentrator: string;
  courseRating: string;
  professorRating: string;
  hours: string;
  year: string;
  semestercrn: string;
  uniqueID: string;
  prettyTitle?: string;
  isApproved: boolean;
  submitTime: string;
}

export interface CourseOffering {
  code: string;
  title: string;
  crn: string;
  no: string;
  semester: string;
  professors: Array<Professor>;
  otherOfferings: Array<string>;
}

export interface SearchResult {
  courses: Array<Course>;
  professors: Array<Professor>;
}

const baseURL: string = "http://localhost:3232/";

export async function callApi(
  getToken: () => Promise<string | null>,
  endpoint: string
): Promise<Response> {
  const token = await getToken();
  if (token === null) {
    throw new Error("No token has been supplied.");
  }
  return await fetch(baseURL + endpoint, {
    headers: { Authorization: "Bearer " + token },
  });
}

export async function search(
  getToken: () => Promise<string | null>,
  term: string
): Promise<SearchResult> {
  const response = await callApi(getToken, `search?query=${term}`);
  if (!response.ok) {
    throw new Error("Server responded with status " + response.statusText);
  }
  const json = await response.json();
  if (json.result != "success") {
    throw new Error(json.errormessage);
  }
  const courses = JSON.parse(json.courses);
  const professors = JSON.parse(json.professors);
  return { courses: courses, professors: professors };
}

export async function getUserCourses(
  getToken: () => Promise<string | null>
): Promise<ReviewableCourse[]> {
  const response = await callApi(getToken, "userProfile");
  if (!response.ok) {
    throw new Error(`Failed to fetch user profile: ${response.statusText}`);
  }

  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.error || "Failed to process user profile request.");
  }

  return json.courses as ReviewableCourse[];
}

export async function addUserCourse(
  getToken: () => Promise<string | null>,
  semesterCrn: string
): Promise<void> {
  const response = await callApi(
    getToken,
    `addCourse?semestercrn=${encodeURIComponent(semesterCrn)}`
  );
  if (!response.ok) {
    throw new Error(`Failed to fetch user profile: ${response.statusText}`);
  }

  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.error || "Failed to process user profile request.");
  }
}

export async function getOffering(
  getToken: () => Promise<string | null>,
  code: string,
  crn: string
): Promise<CourseOffering> {
  const response = await callApi(getToken, `offering?code=${code}&crn=${crn}`);
  if (!response.ok) {
    throw new Error("Server responded with status " + response.statusText);
  }
  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.errormessage);
  }
  const offering = JSON.parse(json.offering);
  return {
    code: offering.code,
    title: offering.title,
    crn: offering.crn,
    no: offering.no,
    semester: offering.semester,
    otherOfferings: offering.otherOfferings,
    professors: offering.professors,
  };
}

export function semesterToPrettyString(semester: string): string {
  if (semester.length != 6) {
    throw new Error("invalid semester string");
  }
  let year = parseInt(semester.substring(0, 4));
  let prettyName = "";
  const sem = semester.substring(4);
  switch (sem) {
    case "00":
      prettyName = "Summer ";
      break;
    case "10":
      prettyName = "Fall ";
      break;
    case "15":
      prettyName = "Winter ";
      year++;
      break;
    case "20":
      prettyName = "Spring ";
      year++;
      break;
  }
  return prettyName + year.toString();
}

export async function getCourseDescription(
  getToken: () => Promise<string | null>,
  crn: string,
  semester: string
): Promise<string> {
  const response = await callApi(
    getToken,
    `description?crn=${crn}&semester=${semester}`
  );
  if (!response.ok) {
    throw new Error("Server responded with status " + response.statusText);
  }
  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.errormessage);
  }
  return json.description;
}

export async function getOfferingReviews(
  getToken: () => Promise<string | null>,
  semestercrn: string
): Promise<Array<ReviewData>> {
  const response = await callApi(
    getToken,
    `get-reviews?semestercrn=${semestercrn}&sortMode=0&filterMode=0&filterKey=0`
  );

  if (!response.ok) {
    throw new Error("Server responded with status " + response.statusText);
  }

  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.errormessage);
  }

  const parsedReviews: any[] = JSON.parse(json.reviews);

  // Map over the parsed reviews to ensure they match the Review interface
  const reviews: Array<ReviewData> = parsedReviews.map((review) => ({
    courseRating: review.courseRating,
    professorRating: review.professorRating,
    hours: review.hours,
    year: review.year,
    reviewText: review.reviewText,
    semestercrn: review.semestercrn,
    karma: review.karma,
    concentrator: review.concentrator,
    uniqueID: review.uniqueID,
  }));

  return reviews;
}

export async function getUnmoderatedReviews(
  getToken: () => Promise<string | null>
): Promise<Array<ModerationReviewData>> {
  const response = await callApi(getToken, `get-unmoderated`);

  if (!response.ok) {
    throw new Error("Server responded with status " + response.statusText);
  }

  const json = await response.json();
  if (json.result !== "success") {
    throw new Error(json.errormessage);
  }

  const parsedReviews: any[] = JSON.parse(json.reviews);

  // Map over the parsed reviews to ensure they match the Review interface
  const reviews: Array<ModerationReviewData> = parsedReviews.map((review) => ({
    courseRating: review.courseRating,
    professorRating: review.professorRating,
    hours: review.hours,
    year: review.year,
    reviewText: review.reviewText,
    semestercrn: review.semestercrn,
    karma: review.karma,
    concentrator: review.concentrator,
    uniqueID: review.uniqueID,
    prettyTitle: review.prettyTitle,
    isApproved: review.isApproved,
    submitTime: new Date(review.submitTime).toLocaleString(),
  }));

  return reviews;
}

export async function getProfessorData(
  getToken: () => Promise<string | null>,
  professorID: string
): Promise<ProfessorCourses> {
  const response = await callApi(getToken, `professor?id=${professorID}`);
  const json = await response.json();
  return { name: json.name, offerings: JSON.parse(json.offerings) };
}

export async function uploadReview(
  getToken: () => Promise<string | null>,
  reviewData: ReviewData
): Promise<string> {
  //constructing query parameters from reviewData
  const queryParams = new URLSearchParams({
    karma: reviewData.karma,
    reviewText: reviewData.reviewText,
    concentrator: reviewData.concentrator,
    courseRating: reviewData.courseRating,
    professorRating: reviewData.professorRating,
    hours: reviewData.hours,
    year: reviewData.year,
    semestercrn: reviewData.semestercrn,
  }).toString();

  const response = await callApi(getToken, `upload-review?${queryParams}`);
  const json = await response.json();

  return json.result;
}

export async function moderateReview(
  getToken: () => Promise<string | null>,
  reviewID: string,
  approve: boolean
): Promise<string> {
  const response = await callApi(
    getToken,
    `moderate?reviewID=${reviewID}&approve=${approve}`
  );
  const json = await response.json();

  return json.result;
}

export async function KarmaChange(
  getToken: () => Promise<string | null>,
  up: boolean,
  semestercrn: string,
  uniqueID: string
): Promise<string> {
  //constructing query parameters from reviewData

  const response = await callApi(
    getToken,
    `karma?upvote=${up}&semestercrn=${semestercrn}&uniqueID=${uniqueID}`
  );
  const json = await response.json();

  return json.karma;
}
