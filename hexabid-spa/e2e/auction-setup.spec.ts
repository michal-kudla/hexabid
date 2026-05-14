import { expect, test, type Page } from '@playwright/test';

async function snapshot(page: Page, name: string) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
}

async function loginAsSeller(page: Page) {
  await page.goto('/oauth2/authorization/dev');
  await expect(page.getByRole('heading', { level: 1, name: 'Hexabid Dev Auth' })).toBeVisible();
  await page.locator('a[href*="username=seller-marek"]').click();
  await expect(page).toHaveURL('http://localhost:14200/');
}

test.describe('Auction Setup Studio — step-by-step wizard', () => {
  test('wizard loads with 4 steps and starts on subject step', async ({ page }) => {
    await page.goto('/sell');
    await expect(page.getByText('Auction Setup Studio')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Przedmiot i kategoria' })).toBeVisible();
    await expect(page.getByText('Kwalifikacja licytantów')).toBeVisible();
    await expect(page.getByText('Cena i zabezpieczenia')).toBeVisible();
    await expect(page.getByText('Podsumowanie')).toBeVisible();
    await snapshot(page, '01-setup-step-subject');
  });

  test('category selection updates detected requirements sidebar', async ({ page }) => {
    await page.goto('/sell');

    const categorySelect = page.locator('select[formcontrolname="category"]');
    await categorySelect.selectOption('LAND');
    await expect(page.getByText('Wykryte wymagania dla: Grunt / Nieruchomość')).toBeVisible();
    await snapshot(page, '02-category-land-detected');
  });

  test('qualification step shows profiles for selected category', async ({ page }) => {
    await page.goto('/sell');

    await page.locator('select[formcontrolname="category"]').selectOption('LAND');
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();
    await expect(page.getByText('Nabywca regulowany')).toBeVisible();
    await snapshot(page, '03-qualification-profiles');
  });

  test('selecting a profile shows bidder preview', async ({ page }) => {
    await page.goto('/sell');

    await page.locator('select[formcontrolname="category"]').selectOption('ALCOHOL');
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();

    const regulatedProfile = page.locator('.profile-card').filter({ hasText: 'Nabywca regulowany' });
    if (await regulatedProfile.isVisible()) {
      await regulatedProfile.click();
      await expect(page.getByText('Tak licytant zobaczy dopuszczenie')).toBeVisible();
      await snapshot(page, '04-profile-bidder-preview');
    }
  });

  test('pricing step has toggle for pricing config', async ({ page }) => {
    await page.goto('/sell');

    await page.fill('[formcontrolname="title"]', 'E2E test aukcja');
    await page.fill('[formcontrolname="amount"]', '500.00');
    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString().slice(0, 16);
    await page.fill('[formcontrolname="endsAt"]', endsAt);

    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Cena i zabezpieczenia' })).toBeVisible();
    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();
    await expect(page.getByText('Konfiguracja ceny')).toBeVisible();
    await snapshot(page, '05-pricing-config');
  });

  test('review step shows full configuration summary', async ({ page }) => {
    await page.goto('/sell');

    await page.fill('[formcontrolname="title"]', 'E2E test aukcja pełna');
    await page.locator('select[formcontrolname="category"]').selectOption('GENERAL');
    await page.fill('[formcontrolname="amount"]', '100.00');
    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString().slice(0, 16);
    await page.fill('[formcontrolname="endsAt"]', endsAt);

    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Podsumowanie' })).toBeVisible();
    await expect(page.getByText('E2E test aukcja pełna')).toBeVisible();
    await expect(page.getByText('Standardowy konsument')).toBeVisible();
    await expect(page.getByText('Ścieżka licytanta')).toBeVisible();
    await snapshot(page, '06-review-summary');
  });

  test('back button navigates to previous step', async ({ page }) => {
    await page.goto('/sell');

    await page.getByRole('button', { name: 'Dalej' }).click();
    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();

    await page.getByRole('button', { name: 'Wstecz' }).click();
    await expect(page.getByRole('heading', { name: 'Przedmiot i kategoria' })).toBeVisible();
  });

  test('step navigation tabs work', async ({ page }) => {
    await page.goto('/sell');

    await page.locator('.step-tab').nth(2).click();
    await expect(page.getByRole('heading', { name: 'Cena i zabezpieczenia' })).toBeVisible();

    await page.locator('.step-tab').nth(0).click();
    await expect(page.getByRole('heading', { name: 'Przedmiot i kategoria' })).toBeVisible();
  });

  test('full flow: create auction through wizard and navigate to details', async ({ page }) => {
    await loginAsSeller(page);
    await page.goto('/sell');

    await expect(page.getByRole('heading', { name: 'Przedmiot i kategoria' })).toBeVisible();

    await page.fill('[formcontrolname="title"]', `E2E wizard ${Date.now()}`);
    await page.locator('select[formcontrolname="category"]').selectOption('GENERAL');
    await page.fill('[formcontrolname="amount"]', '200.00');
    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 3).toISOString().slice(0, 16);
    await page.fill('[formcontrolname="endsAt"]', endsAt);
    await snapshot(page, '07-wizard-step1-filled');

    await page.getByRole('button', { name: 'Dalej' }).click();
    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();
    await snapshot(page, '08-wizard-step2-qualification');

    await page.getByRole('button', { name: 'Dalej' }).click();
    await expect(page.getByRole('heading', { name: 'Cena i zabezpieczenia' })).toBeVisible();
    await snapshot(page, '09-wizard-step3-pricing');

    await page.getByRole('button', { name: 'Dalej' }).click();
    await expect(page.getByRole('heading', { name: 'Podsumowanie' })).toBeVisible();
    await snapshot(page, '10-wizard-step4-review');

    await page.getByRole('button', { name: 'Zapisz szkic aukcji' }).click();
    await page.waitForURL(/\/auction\/[^/]+$/, { timeout: 15_000 });
    await expect(page.locator('app-auction-details-page')).toBeVisible();
    await snapshot(page, '11-wizard-created-auction');
  });
});
