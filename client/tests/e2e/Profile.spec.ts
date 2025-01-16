import { expect, test } from "@playwright/test";
import { setupClerkTestingToken, clerk } from "@clerk/testing/playwright";

// Before each test, make sure the page is loaded 
test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
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

// Test for the "Profile" button navigation when signed out
test('i can navigate to Profile page from App, signed out', async ({ page }) => {
  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');
  await expect(page.locator('text=Sign in to a @brown.edu account to view profile.')).toBeVisible();
});

// Test for the "Profile" button navigation when signed in
test('i can navigate to Profile page from App, signed in', async ({ page }) => {
  await signInWithClerk({ page }); 

  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');
  await expect(page.locator('text=My Profile and Classes Taken')).toBeVisible();
});


// Test for search functionality: search term too short
test('if search term too short, i get a message', async ({ page }) => {
    await signInWithClerk({ page }); 

  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');
  
  await page.locator('input[type="text"]').fill("a"); 
  await page.locator('button[type="submit"]').click();
  
  await expect(page.locator('text=Please enter a more specific search term')).toBeVisible();
});

// Test for search functionality: search term too long
test('if search term too long, i get a message', async ({ page }) => {
    await signInWithClerk({ page }); 

  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');

  await page.locator('input[type="text"]').fill("a".repeat(101)); 
  await page.locator('button[type="submit"]').click();

  await expect(page.locator('text=Please enter a shorter search term')).toBeVisible();
});

// Test for search functionality: successful search
test('if search is successful, i see results and tables are visible', async ({ page }) => {
    await signInWithClerk({ page }); 

  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');

  await page.locator('input[type="text"]').fill("hi"); 
  await page.locator('button[type="submit"]').click();

  await expect(page.locator('.search-results table')).toBeVisible();
  await expect(page.locator('.user-courses table')).toBeVisible();
});

// Test for searching with no results (edge case)
test('if no results found, i do not see results', async ({ page }) => {
    await signInWithClerk({ page }); 

  await page.locator('text=Profile').click();
  await expect(page).toHaveURL('http://localhost:8000/profile');

  await page.locator('input[type="text"]').fill("noresultsforthissearchterm"); 
  await page.locator('button[type="submit"]').click();
  
  await expect(page.locator('.search-results table')).not.toBeVisible();
  await expect(page.locator('.user-courses table')).toBeVisible();
});
