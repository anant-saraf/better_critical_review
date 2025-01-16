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

  // this logs in/out via _Clerk_, not via actual component interaction
  await clerk.signIn({
    page,
    signInParams: {
      strategy: "password",
      password: process.env.E2E_CLERK_USER_PASSWORD!,
      identifier: process.env.E2E_CLERK_USER_USERNAME_BROWN!,
    },
  });
});


test("test", async ({ page }) => {
  await page.goto("http://localhost:8000/");
  await page.getByPlaceholder("Search based on all...").click();
  await page.getByPlaceholder("Search based on all...").fill("digital media");
  await page.getByRole("button", { name: ">" }).click();
  const page1Promise = page.waitForEvent("popup");
  await page.getByRole("link", { name: "Digital Media" }).click();
  const page1 = await page1Promise;
  await expect(
    page1.getByRole("cell", { name: "first comment for test!" })
  ).toBeVisible();
  await expect(page1.getByRole("cell", { name: "hi" })).toBeVisible();
  await expect(page1.getByText("This course introduces")).toBeVisible();
  await expect(page1.getByText("Course Rating: 3.5 out of")).toBeVisible();
});

// test("I cannot sign in with a non-brown or risd email", async ({ page }) => {
//   // Notice: http, not https! Our front-end is not set up for HTTPs.
//   await page.goto(url);
//   setupClerkTestingToken({ page });
//   await page.goto(url);
//   await clerk.loaded({ page });
//   const loginButton = page.getByRole("button", { name: "Sign In" });
//   await expect(loginButton).toBeVisible();

//   // this logs in/out via _Clerk_, not via actual component interaction
//   await clerk.signIn({
//     page,
//     signInParams: {
//       strategy: "password",
//       password: process.env.E2E_CLERK_USER_PASSWORD_WRONG!,
//       identifier: process.env.E2E_CLERK_USER_USERNAME_WRONG!,
//     },
//   });
// });

// /**
//  * Don't worry about the "async" yet. We'll cover it in more detail
//  * for the next sprint. For now, just think about "await" as something
//  * you put before parts of your test that might take time to run,
//  * like any interaction with the page.
//  */
// test("on page load, i see a login button", async ({ page }) => {
//   // Notice: http, not https! Our front-end is not set up for HTTPs.
//   await page.goto("http://localhost:8000/");
//   await expect(page.getByLabel("Login")).toBeVisible();
// });

// test("on page load, i dont see the input box until login", async ({ page }) => {
//   // Notice: http, not https! Our front-end is not set up for HTTPs.
//   await page.goto("http://localhost:8000/");
//   await expect(page.getByLabel("Sign Out")).not.toBeVisible();
//   await expect(page.getByLabel("dropdown")).not.toBeVisible();

//   // click the login button
//   await page.getByLabel("Login").click();
//   await expect(page.getByLabel("Sign Out")).toBeVisible();
//   await expect(page.getByLabel("dropdown")).toBeVisible();
// });

// test("on page load, i see a submit button", async ({ page }) => {
  
  
//   // TODO 5 WITH TA: Fill this in!
// });

// test("after I click the submit button, i see the dropdown text in the output area", async ({
//   page,
// }) => {
//   // TODO 5 WITH TA: Fill this in to test your button push functionality!
// });
