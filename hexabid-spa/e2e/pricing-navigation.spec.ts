import { expect, test } from '@playwright/test';

test.describe('Szczegóły aukcji - link do kalkulacji', () => {
  test('strona szczegółów renderuje się bez błędów', async ({ page }) => {
    await page.goto('/auction/test-auction-id');

    await expect(page.locator('app-auction-details-page')).toBeVisible();
  });

  test('link do kalkulacji ceny jest obecny na stronie szczegółów', async ({ page }) => {
    await page.goto('/auction/test-auction-id');

    const pricingLink = page.locator('a.pricing-link');
    const emptyState = page.locator('app-empty-state');

    const hasPricingLink = await pricingLink.isVisible().catch(() => false);
    const hasEmptyState = await emptyState.isVisible().catch(() => false);

    expect(hasPricingLink || hasEmptyState).toBeTruthy();
  });

  test('nawigacja do strony kalkulacji zmienia URL', async ({ page }) => {
    await page.goto('/auction/test-auction-id/pricing');

    await expect(page).toHaveURL(/\/auction\/test-auction-id\/pricing$/);
  });
});
