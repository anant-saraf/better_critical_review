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

// Test for the "Review" button navigation when signed out
test('i can navigate to Review page from App, signed out', async ({ page }) => {
  await page.locator('text=Review').click();
  await expect(page).toHaveURL('http://localhost:8000/review');
  await expect(page.locator('text=Sign in to a @brown.edu account to leave reviews.')).toBeVisible();
});

// Test for the "Review" button navigation when signed in
test('i can navigate to Review page from App, signed in', async ({ page }) => {
  await signInWithClerk({ page }); 

  await page.locator('text=Review').click();
  await expect(page).toHaveURL('http://localhost:8000/review');
  await expect(page.locator('text=All reviews will be moderated and only published once approved.')).toBeVisible();

  await page.locator('button', { hasText: 'Review' }).click();
});


