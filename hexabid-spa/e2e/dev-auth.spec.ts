import { expect, test, type Page } from '@playwright/test';

async function snapshot(page: Page, name: string) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
}

test.describe('Dev auth flow', () => {
  test('wybór konta dev przez SPA loguje użytkownika i odblokowuje dashboard', async ({ page }) => {
    await page.goto('/oauth2/authorization/dev');
    await expect(page.getByRole('heading', { level: 1, name: 'Hexabid Dev Auth' })).toBeVisible();
    await snapshot(page, '01-dev-auth-account-picker');

    const profileResponse = page.waitForResponse(response =>
      response.url() === 'http://localhost:14200/api/me' && response.status() === 200
    );
    await page.getByRole('link', { name: 'Login as this user' }).first().click();

    await expect(page).not.toHaveURL(/\/login\?error/);
    await expect(page).toHaveURL('http://localhost:14200/');
    await profileResponse;

    await page.goto('/dashboard');
    await expect(page.getByRole('heading', { level: 1, name: 'Anna Developer' })).toBeVisible();
    await expect(page.getByText('Zaloguj się, aby otworzyć dashboard')).toHaveCount(0);
    await expect(page.locator('.profile-meta').getByText('dev', { exact: true })).toBeVisible();
    await snapshot(page, '02-dev-auth-dashboard');
  });
});
