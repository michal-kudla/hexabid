import { expect, test, type Page } from '@playwright/test';

async function snapshot(page: Page, name: string) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
}

async function openFirstAuctionFromMarketplace(page: Page) {
  await page.goto('/');
  await expect(page.getByRole('heading', { level: 1, name: 'Odkryj unikalne przedmioty na żywym rynku licytacji' })).toBeVisible();

  const auctionLink = page.locator('a.card-link[href*="/auction/"]').first();
  await expect(auctionLink, 'Brak aukcji demo na rynku - uruchom backend z profilem local').toBeVisible({ timeout: 15_000 });
  await snapshot(page, '01-marketplace');

  await auctionLink.click();
  await expect(page.locator('app-auction-details-page')).toBeVisible();
  await snapshot(page, '02-auction-details');
}

async function loginAsDevUser(page: Page, username: string) {
  await page.goto('/oauth2/authorization/dev');
  await expect(page.getByRole('heading', { level: 1, name: 'Hexabid Dev Auth' })).toBeVisible();
  await page.locator(`a[href*="username=${username}"]`).click();
  await expect(page).toHaveURL('http://localhost:14200/');
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
    const errorState = page.locator('.error');
    await expect(cards.first().or(emptyState).or(errorState)).toBeVisible();
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
      await expect(
        page
          .getByRole('heading', { name: 'Reguły rozliczenia' })
          .or(page.getByRole('heading', { name: 'Nie udało się pobrać kalkulacji' }))
      ).toBeVisible();
      await snapshot(page, '06-pricing-page');
    });
  });

  test('sprzedający: formularz wystawiania aukcji obsługuje konfiguracje pricing', async ({ page }) => {
    await page.goto('/sell');
    await expect(page.getByRole('heading', { level: 1, name: 'Konfiguracja aukcji' })).toBeVisible();

    await page.locator('.step-tab').nth(3).click();
    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();
    await expect(page.getByText('Konfiguracja ceny')).toBeVisible();

    await page.locator('select[formcontrolname="wadiumStrategy"]').selectOption({ label: 'Procentowe' });
    await expect(page.getByText('Stawka wadium (np. 0.05 = 5%)')).toBeVisible();

    await page.locator('select[formcontrolname="isExcisable"]').selectOption({ label: 'Tak' });
    await expect(page.getByText('Stawka akcyzy')).toBeVisible();

    await page.locator('select[formcontrolname="isImported"]').selectOption({ label: 'Tak' });
    await expect(page.getByText('Stawka cła')).toBeVisible();
    await snapshot(page, '07-sell-pricing-config');
  });

  test('sprzedający: utworzenie szkicu i uruchomienie aukcji odblokowuje licytację', async ({ page }) => {
    await loginAsDevUser(page, 'seller-marek');

    await page.goto('/sell');
    await expect(page.getByRole('heading', { level: 1, name: 'Konfiguracja aukcji' })).toBeVisible();

    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString().slice(0, 16);
    await page.getByLabel('Tytuł aukcji').fill(`E2E szkic do aktywacji ${Date.now()}`);
    await page.getByLabel('Cena wywoławcza').fill('100.00');
    await page.getByLabel('Koniec aukcji').fill(endsAt);
    await snapshot(page, '08-sell-draft-form');

    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();

    await page.getByRole('button', { name: 'Zapisz szkic aukcji' }).click();
    await expect(page).toHaveURL(/\/auction\/[^/]+$/);
    const auctionUrl = page.url();
    await expect(page.locator('.badge', { hasText: 'Szkic' })).toBeVisible();
    await snapshot(page, '09-created-draft-details');

    const activationResponse = page.waitForResponse(response =>
      response.url().includes('/api/auctions/') &&
      response.url().endsWith('/activate') &&
      response.status() === 200
    );
    await page.getByRole('button', { name: 'Opublikuj i uruchom aukcję' }).click();
    await activationResponse;

    await expect(page.locator('.badge', { hasText: 'Aktywna' })).toBeVisible();
    await snapshot(page, '10-activated-auction-details');

    await loginAsDevUser(page, 'bidder-ola');
    await page.goto(auctionUrl);
    await expect(page.locator('.badge', { hasText: 'Aktywna' })).toBeVisible();
    await expect(page.locator('app-auction-bid-panel')).toBeVisible();
    await snapshot(page, '11-bidder-view');
  });
});
