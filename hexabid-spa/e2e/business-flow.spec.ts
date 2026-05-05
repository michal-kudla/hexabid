import { expect, test, type Page } from '@playwright/test';

async function snapshot(page: Page, name: string) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
}

async function openFirstAuctionFromMarketplace(page: Page) {
  await page.goto('/');
  await expect(page.getByRole('heading', { level: 1, name: 'Rynek aukcyjny' })).toBeVisible();

  const auctionLink = page.locator('a[href*="/auction/"]').first();
  await expect(auctionLink, 'Brak aukcji demo na rynku - uruchom backend z profilem local').toBeVisible({ timeout: 15_000 });
  await snapshot(page, '01-marketplace');

  await auctionLink.click();
  await expect(page.locator('app-auction-details-page')).toBeVisible();
  await snapshot(page, '02-auction-details');
}

test.describe('Business flow smoke: kluczowe scenariusze GUI SPA', () => {
  test('katalog produktów + filtrowanie strategii jest dostępne', async ({ page }) => {
    await page.goto('/products');

    await expect(page.getByRole('heading', { level: 1, name: 'Katalog Produktów' })).toBeVisible();
    await expect(page.getByPlaceholder('Szukaj produktu...')).toBeVisible();

    const strategySelect = page.locator('select.strategy-select');
    await expect(strategySelect).toBeVisible();
    await strategySelect.selectOption('UNIQUE');

    const cards = page.locator('a.product-card');
    const emptyState = page.getByText('Brak produktów w katalogu.');
    await expect(cards.first().or(emptyState)).toBeVisible();
    await snapshot(page, '03-products-catalog-filtered');
  });

  test('tworzenie partii produktu: formularz inventory jest kompletny', async ({ page }) => {
    await page.goto('/inventory/batches/new');

    await expect(page.getByRole('heading', { level: 1, name: 'Utwórz partię' })).toBeVisible();
    await expect(page.locator('#productId')).toBeVisible();
    await expect(page.locator('#name')).toBeVisible();
    await expect(page.locator('#quantity')).toBeVisible();
    await expect(page.locator('#unit')).toBeVisible();

    await page.fill('#name', `E2E-PARTIA-${Date.now()}`);
    await page.fill('#quantity', '12.5');
    await page.selectOption('#unit', 'kg');
    await snapshot(page, '04-create-batch-form');
  });

  test('aukcja: rynek -> szczegóły -> reguły + dokumenty -> pricing', async ({ page }) => {
    await openFirstAuctionFromMarketplace(page);

    await test.step('weryfikacja panelu reguł oraz formularza dokumentu', async () => {
      await expect(page.getByRole('heading', { name: 'Reguły aukcyjne' })).toBeVisible();
      await expect(page.getByRole('heading', { name: 'Złóż dokument' })).toBeVisible();

      const documentTypeSelect = page.locator('select').first();
      await expect(documentTypeSelect).toBeVisible();
      const options = await documentTypeSelect.locator('option').allTextContents();
      expect(options.length, 'Brak opcji typu dokumentu').toBeGreaterThan(1);
      await snapshot(page, '05-rules-and-documents');
    });

    await test.step('przejście do kalkulacji i reguł rozliczenia', async () => {
      const pricingLink = page.getByRole('link', { name: /kalkulacj/i });
      await expect(pricingLink, 'Brak linku do kalkulacji ceny na szczegółach aukcji').toBeVisible();
      await pricingLink.click();

      await expect(page).toHaveURL(/\/auction\/[^/]+\/pricing$/);
      await expect(page.locator('app-pricing-page')).toBeVisible();
      await expect(page.getByRole('heading', { name: 'Reguły rozliczenia' })).toBeVisible();
      await snapshot(page, '06-pricing-page');
    });
  });

  test('sprzedający: formularz wystawiania aukcji obsługuje konfiguracje pricing', async ({ page }) => {
    await page.goto('/sell');
    await expect(page.getByRole('heading', { level: 1, name: 'Wystaw aukcję przez SPA' })).toBeVisible();

    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();
    await expect(page.getByText('Konfiguracja ceny (PricingConfig)')).toBeVisible();

    await page.locator('select[formcontrolname="wadiumStrategy"]').selectOption('PERCENTAGE');
    await expect(page.getByText('Stawka wadium (np. 0.05 = 5%)')).toBeVisible();

    await page.locator('select[formcontrolname="isExcisable"]').selectOption('true');
    await expect(page.getByText('Stawka akcyzy')).toBeVisible();

    await page.locator('select[formcontrolname="isImported"]').selectOption('true');
    await expect(page.getByText('Stawka cła')).toBeVisible();
    await snapshot(page, '07-sell-pricing-config');
  });
});
