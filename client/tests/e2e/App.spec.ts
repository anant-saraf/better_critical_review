import { expect, test } from "@playwright/test";
import { setupClerkTestingToken, clerk } from "@clerk/testing/playwright";

// Before each test, make sure the page is loaded 
test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
});

// Test for the "Sign In" button visibility and the behavior when signed out
test("on page load, i see prompt to sign in", async ({ page }) => {
  await expect(page.locator('text=Sign in to a @brown.edu account to continue.')).toBeVisible(); 
});

// Test for the Clerk setup  and simulate user sign-in 
test('i can sign in with Clerk', async ({ page }) => {
  setupClerkTestingToken({ page });
  await clerk.loaded({ page });
  await expect(page.getByRole("heading", { name: "Sign in to a @brown.edu" })).toBeVisible();
  const loginButton = page.getByRole("button", { name: "Sign In" });
  await expect(loginButton).toBeVisible();
  await loginButton.click();

  // Use valid credentials for the @brown.edu domain
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Email address").fill("notreal@brown.edu");
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Password", { exact: true }).fill("notrealnotreal");
  await page.getByRole("button", { name: "Continue" }).click();
  
  // Assert that the Sign Out button is visible after the user has signed in
  const signOutButton = page.getByRole('button', { name: 'Sign Out' });
  await expect(signOutButton).toBeVisible();
});

// Test for the "Review" and "About" buttons
test('i can see the Review button and About button', async ({ page }) => {
  await expect(page.locator('text=Review')).toBeVisible(); 
  await expect(page.locator('text=About')).toBeVisible(); 
});

// Test for the FAQ toggle functionality
test('i can toggle to About and FAQ text, signed out', async ({ page }) => {
  await expect(page.locator('text=What is The Critical Review, But Better?')).not.toBeVisible();
  await page.locator('text=About').click();
  await expect(page.locator('text=What is The Critical Review, But Better?')).toBeVisible();
  await page.locator('text=Return').click();
  await expect(page.locator('text=What is The Critical Review, But Better?')).not.toBeVisible();
});

// Helper function to sign in before running any search test
const signInWithClerk = async ({ page }) => {
  setupClerkTestingToken({ page });
  await clerk.loaded({ page });
  

  const loginButton = page.getByRole("button", { name: "Sign In" });
  await loginButton.click();


  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Email address").fill("notreal@brown.edu");
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Password", { exact: true }).fill("notrealnotreal");
  await page.getByRole("button", { name: "Continue" }).click();

  const signOutButton = page.getByRole('button', { name: 'Sign Out' });
  // await expect(signOutButton).toBeVisible();
};

// Test for search functionality: search term too short
test('if search term too short, i get a message', async ({ page }) => {
  await signInWithClerk({ page }); 
  
  await page.locator('input[type="text"]').fill("a"); 
  await page.locator('button[type="submit"]').click();
  
  await expect(page.locator('text=Please enter a more specific search term')).toBeVisible();
});

// Test for search functionality: search term too long
test('if search term too long, i get a message', async ({ page }) => {
  await signInWithClerk({ page }); 

  await page.locator('input[type="text"]').fill("a".repeat(101)); 
  await page.locator('button[type="submit"]').click();

  await expect(page.locator('text=Please enter a shorter search term')).toBeVisible();
});

// Test for search functionality: successful search
test('if search is successful, i see results and tables are visible', async ({ page }) => {
  await signInWithClerk({ page }); 

  await page.locator('input[type="text"]').fill("hi"); 
  await page.locator('button[type="submit"]').click();

  await expect(page.locator('text=Currently searching for: hi')).toBeVisible();

  await expect(page.locator('.course-table table')).toBeVisible();
  await expect(page.locator('.professor-table table')).toBeVisible();
});

// Test for searching with no results (edge case)
test('if no results found, i do not see results', async ({ page }) => {
  await signInWithClerk({ page }); 

  await page.locator('input[type="text"]').fill("noresultsforthissearchterm"); 
  await page.locator('button[type="submit"]').click();
  
  await expect(page.locator('.course-table table')).not.toBeVisible();
  await expect(page.locator('.professor-table table')).not.toBeVisible();
});


// Test for navigating to the "Profile" page
test('i can navigate to Profile page from App', async ({ page }) => {
  await signInWithClerk({ page }); // Ensure user is signed in
  
  await page.locator('button', { hasText: 'Profile' }).click();
  await expect(page).toHaveURL('http://localhost:8000/profile');
});

// Test for the "Review" button navigation
test('i can navigate to Review page from App', async ({ page }) => {
  await page.locator('text=Review').click();
  await expect(page).toHaveURL('http://localhost:8000/review');
});

// Test for the "Sign Out" button behavior when signed in
test('i can sign out', async ({ page }) => {
  await signInWithClerk({ page }); 
  
  const signOutButton = page.getByRole('button', { name: 'Sign Out' });
  await signOutButton.click();
  
  await expect(page.getByRole('button', { name: 'Sign In' })).toBeVisible();
});

test('i can toggle to About and FAQ text, signed in', async ({ page }) => {
  await signInWithClerk({ page }); 
  
  await expect(page.locator('text=What is The Critical Review, But Better?')).not.toBeVisible();
  await page.locator('text=About').click();
  await expect(page.locator('text=What is The Critical Review, But Better?')).toBeVisible();
  await page.locator('text=Return').click();
  await expect(page.locator('text=What is The Critical Review, But Better?')).not.toBeVisible();
});

