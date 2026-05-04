import { expect, test } from '@playwright/test';

test.describe('Rules Panel on Auction Details', () => {
  test('shows rules section on auction details page', async ({ page }) => {
    await page.goto('/');
    const auctionCards = page.locator('article.auction-card a, .card a, a[href*="/auction/"]');
    const count = await auctionCards.count();

    if (count > 0) {
      await auctionCards.first().click();
      await expect(page.getByRole('heading', { name: 'Reguły aukcyjne' })).toBeVisible({ timeout: 10000 });
    }
  });

  test('shows document submission form on auction details page', async ({ page }) => {
    await page.goto('/');
    const auctionCards = page.locator('article.auction-card a, .card a, a[href*="/auction/"]');
    const count = await auctionCards.count();

    if (count > 0) {
      await auctionCards.first().click();
      await expect(page.getByRole('heading', { name: 'Złóż dokument' })).toBeVisible({ timeout: 10000 });
      await expect(page.getByRole('button', { name: 'Złóż dokument' })).toBeVisible();
    }
  });

  test('document type selector has all options', async ({ page }) => {
    await page.goto('/');
    const auctionCards = page.locator('article.auction-card a, .card a, a[href*="/auction/"]');
    const count = await auctionCards.count();

    if (count > 0) {
      await auctionCards.first().click();
      await page.waitForSelector('select', { timeout: 10000 });
      const docTypeSelect = page.locator('select').first();
      const options = await docTypeSelect.locator('option').allTextContents();
      expect(options.some(o => o.includes('akcyzowe') || o.includes('EXCISE'))).toBeTruthy();
    }
  });
});

test.describe('Rules Panel on Pricing Page', () => {
  test('shows settlement rules on pricing page', async ({ page }) => {
    await page.goto('/');
    const auctionCards = page.locator('article.auction-card a, .card a, a[href*="/auction/"]');
    const count = await auctionCards.count();

    if (count > 0) {
      await auctionCards.first().click();
      const pricingLink = page.getByRole('link', { name: /kalkulacj/i });
      if (await pricingLink.isVisible()) {
        await pricingLink.click();
        await expect(page.getByRole('heading', { name: 'Reguły rozliczenia' })).toBeVisible({ timeout: 10000 });
      }
    }
  });
});

test.describe('Rules Status Indicators', () => {
  test('rule items display with status colors', async ({ page }) => {
    await page.goto('/');
    const auctionCards = page.locator('article.auction-card a, .card a, a[href*="/auction/"]');
    const count = await auctionCards.count();

    if (count > 0) {
      await auctionCards.first().click();
      await page.waitForSelector('.rule-item', { timeout: 10000 }).catch(() => {});
      const ruleItems = page.locator('.rule-item');
      const ruleCount = await ruleItems.count();
      if (ruleCount > 0) {
        const firstRule = ruleItems.first();
        const classes = await firstRule.getAttribute('class') || '';
        const hasStatus = ['rule-satisfied', 'rule-pending', 'rule-violated'].some(c => classes.includes(c));
        expect(hasStatus).toBeTruthy();
      }
    }
  });
});
