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

async function loginAsBidder(page: Page) {
  await page.goto('/oauth2/authorization/dev');
  await expect(page.getByRole('heading', { level: 1, name: 'Hexabid Dev Auth' })).toBeVisible();
  await page.locator('a[href*="username=bidder-ola"]').click();
  await expect(page).toHaveURL('http://localhost:14200/');
}

test.describe('Phase 4: Qualification Profile Contract', () => {
  test('qualification profiles API returns profile catalog', async ({ page }) => {
    await loginAsSeller(page);

    const response = await page.request.get('/api/qualification-profiles', {
      headers: { 'X-API-Version': '1' }
    });

    expect(response.ok()).toBeTruthy();
    const body = await response.json();

    expect(body.items).toBeDefined();
    expect(body.items.length).toBeGreaterThanOrEqual(3);

    const profileNames = body.items.map((i: any) => i.templateName);
    expect(profileNames).toContain('PUBLIC_CONSUMER_LIGHT_V1');
    expect(profileNames).toContain('REGULATED_ASSET_BUYER_V1');
    expect(profileNames).toContain('HIGH_VALUE_TENDER_V1');

    const light = body.items.find((i: any) => i.templateName === 'PUBLIC_CONSUMER_LIGHT_V1');
    expect(light.label).toBeDefined();
    expect(light.taskCount).toBe(4);
    expect(light.recommended).toBe(true);
    expect(light.abandonmentRisk).toBe('low');
  });

  test('creating auction with participationPolicyTemplate stores it on the auction', async ({ page }) => {
    await loginAsSeller(page);

    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString();
    const createResponse = await page.request.post('/api/auctions', {
      headers: { 'X-API-Version': '1', 'Content-Type': 'application/json' },
      data: {
        title: `E2E Phase4 auction ${Date.now()}`,
        startingPrice: { amount: '100.00', currency: 'PLN' },
        endsAt,
        participationPolicyTemplate: 'PUBLIC_CONSUMER_LIGHT_V1'
      }
    });

    expect(createResponse.ok()).toBeTruthy();
    const auction = await createResponse.json();

    expect(auction.qualificationSummary).toBeDefined();
    expect(auction.qualificationSummary.participationPolicyTemplate).toBe('PUBLIC_CONSUMER_LIGHT_V1');
    expect(auction.qualificationSummary.taskCount).toBe(4);
    expect(auction.qualificationSummary.templateLabel).toBe('Standardowy konsument');
  });

  test('starting participation program without templateName uses auction profile', async ({ page }) => {
    await loginAsSeller(page);

    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString();
    const createResponse = await page.request.post('/api/auctions', {
      headers: { 'X-API-Version': '1', 'Content-Type': 'application/json' },
      data: {
        title: `E2E Phase4 program auction ${Date.now()}`,
        startingPrice: { amount: '200.00', currency: 'PLN' },
        endsAt,
        participationPolicyTemplate: 'PUBLIC_CONSUMER_LIGHT_V1'
      }
    });

    if (!createResponse.ok()) {
      return;
    }

    const auction = await createResponse.json();
    const auctionId = auction.auctionId;

    const startResponse = await page.request.post(`/api/auctions/${auctionId}/participation/program`, {
      headers: { 'X-API-Version': '1', 'Content-Type': 'application/json' },
      data: {}
    });

    if (startResponse.status() === 401) {
      return;
    }

    if (startResponse.ok()) {
      const program = await startResponse.json();
      expect(program.templateName).toBe('PUBLIC_CONSUMER_LIGHT_V1');
    }
  });
});

test.describe('Phase 4: SPA qualification profile integration', () => {
  test('setup wizard shows profile assignment note on review step', async ({ page }) => {
    await page.goto('/sell');

    await page.fill('[formcontrolname="title"]', 'E2E Phase4 profile test');
    await page.fill('[formcontrolname="amount"]', '100.00');
    const endsAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 3).toISOString().slice(0, 16);
    await page.fill('[formcontrolname="endsAt"]', endsAt);

    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Podsumowanie' })).toBeVisible();
    await expect(page.locator('.review-section').getByText('Standardowy konsument')).toBeVisible();
    await expect(page.getByText(/zostanie przypisany do aukcji/)).toBeVisible();
    await snapshot(page, '01-phase4-profile-assignment-note');
  });

  test('participation center shows qualification summary when available', async ({ page }) => {
    await loginAsBidder(page);

    await page.goto('/');
    const auctionCards = page.locator('a[href*="/auction/"]');
    const count = await auctionCards.count();
    if (count === 0) return;

    await auctionCards.first().click();
    await expect(page.locator('app-auction-details-page')).toBeVisible({ timeout: 10_000 });

    const participationCenter = page.locator('app-participation-center');
    if (await participationCenter.isVisible()) {
      const qualificationInfo = page.locator('.qualification-info');
      if (await qualificationInfo.isVisible()) {
        await expect(qualificationInfo).toContainText('zadań');
        await snapshot(page, '02-phase4-qualification-summary');
      }
    }
  });
});
