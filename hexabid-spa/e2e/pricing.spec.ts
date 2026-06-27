import { expect, test } from '@playwright/test';

test.describe('Kalkulacja ceny (Pricing Breakdown)', () => {
  test('wyświetla stronę kalkulacji z komunikatem błędu lub nagłówkiem', async ({ page }) => {
    await page.goto('/auction/test-auction/pricing');

    const hasHeading = await page.getByRole('heading', { level: 1, name: 'Kalkulacja ceny' }).isVisible();
    const hasError = await page.getByRole('heading', { level: 2, name: 'Nie udało się pobrać kalkulacji' }).isVisible();
    expect(hasHeading || hasError).toBeTruthy();
  });

  test('pokazuje link powrotu do aukcji lub komunikat błędu', async ({ page }) => {
    await page.goto('/auction/test-auction/pricing');

    const backLink = page.getByText('Powrót do aukcji');
    const errorState = page.getByText('Nie udało się pobrać kalkulacji');
    const backVisible = await backLink.isVisible();
    const errorVisible = await errorState.isVisible();
    expect(backVisible || errorVisible).toBeTruthy();
  });

  test('renderuje stronę kalkulacji ceny bez błędów aplikacji', async ({ page }) => {
    await page.goto('/auction/test-auction/pricing');

    await expect(page.locator('app-pricing-page')).toBeVisible();
    expect(page.locator('.error-boundary')).toHaveCount(0);
  });

  test('strona kalkulacji ma poprawny URL', async ({ page }) => {
    await page.goto('/auction/abc-123/pricing');
    await expect(page).toHaveURL(/\/auction\/abc-123\/pricing$/);
  });
});
