import { expect, test } from "@playwright/test";
import { setupClerkTestingToken, clerk } from "@clerk/testing/playwright";

/**
  The general shapes of tests in Playwright Test are:
    1. Navigate to a URL
    2. Interact with the page
    3. Assert something about the page against your expectations
  Look for this pattern in the tests below!
 */

const url = "http://localhost:8000";

test.beforeEach(async ({ page }) => {
  await page.goto(url);
  setupClerkTestingToken({ page });
  await page.goto(url);
  await clerk.loaded({ page });
  await expect(
    page.getByRole("heading", { name: "Sign in to a @brown.edu" })
  ).toBeVisible();
  const loginButton = page.getByRole("button", { name: "Sign In" });
  await expect(loginButton).toBeVisible();
});



test("access is constrained to @brown.edu emails", async ({ page }) => {
  // First login attempt with @brown.edu email
  await clerk.signOut({ page });
  await page.getByRole("button", { name: "Sign in" }).click();
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Email address").fill("notreal@brown.edu");
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Password", { exact: true }).fill("notrealnotreal");
  await page.getByRole("button", { name: "Continue" }).click();


  // Sign out to prepare for the next login attempt
  await page.getByRole("button", { name: "Sign out" }).click();

 await expect(
   page.getByRole("heading", { name: "Sign in to a @brown.edu" })
 ).toBeVisible();


   await page.getByRole("button", { name: "Sign In" }).click();
   await page.getByPlaceholder("Enter your email address").click();
   await page
     .getByPlaceholder("Enter your email address")
     .fill("notreal@example.com");
   await page.getByRole("button", { name: "Continue", exact: true }).click();
await expect(page.getByText("You do not have permission to")).toBeVisible();
await expect(page.locator('[id="__next"]')).toContainText(
  "You do not have permission to access this page. Please contact support if you believe this is an error."
)
});
