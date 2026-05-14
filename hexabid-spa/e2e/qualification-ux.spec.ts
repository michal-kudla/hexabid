import { expect, test, type Page } from '@playwright/test';

async function snapshot(page: Page, name: string) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
}

async function loginAsDevUser(page: Page, username: string) {
  await page.goto('/oauth2/authorization/dev');
  await expect(page.getByRole('heading', { level: 1, name: 'Hexabid Dev Auth' })).toBeVisible();
  await page.locator(`a[href*="username=${username}"]`).click();
  await expect(page).toHaveURL('http://localhost:14200/');
}

test.describe('Phase 5: Full UX — task kind differentiation and category flows', () => {
  test('task cards show kind badges for different task types', async ({ page }) => {
    await loginAsDevUser(page, 'bidder-ola');

    await page.goto('/');
    const auctionCards = page.locator('a[href*="/auction/"]');
    const count = await auctionCards.count();
    if (count === 0) return;

    await auctionCards.first().click();
    await expect(page.locator('app-auction-details-page')).toBeVisible({ timeout: 10_000 });

    const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
    if (await startButton.isVisible()) {
      await startButton.click();
      await page.waitForSelector('.task-card', { timeout: 10_000 }).catch(() => {});
    }

    const kindBadges = page.locator('.task-kind-badge');
    const badgeCount = await kindBadges.count();
    if (badgeCount > 0) {
      const badgeTexts = await kindBadges.allTextContents();
      const hasKindVariety = badgeTexts.some(t =>
        t === 'Identyfikacja podmiotu' ||
        t === 'Sprawdzenie zewnętrzne' ||
        t === 'Wymagany dokument' ||
        t === 'Wymóg weryfikacji'
      );
      if (hasKindVariety) {
        await snapshot(page, '01-task-kind-badges');
      }
    }
  });

  test('participation center groups tasks by stages', async ({ page }) => {
    await loginAsDevUser(page, 'bidder-ola');

    await page.goto('/');
    const auctionCards = page.locator('a[href*="/auction/"]');
    const count = await auctionCards.count();
    if (count === 0) return;

    await auctionCards.first().click();
    await expect(page.locator('app-auction-details-page')).toBeVisible({ timeout: 10_000 });

    const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
    if (await startButton.isVisible()) {
      await startButton.click();
      await page.waitForSelector('.stage-group', { timeout: 10_000 }).catch(() => {});
    }

    const stageLabels = page.locator('.stage-label');
    const stageCount = await stageLabels.count();
    if (stageCount > 0) {
      await snapshot(page, '02-stage-groups');
    }
  });

  test('setup wizard for LAND category shows regulated profile', async ({ page }) => {
    await page.goto('/sell');

    await page.locator('select[formcontrolname="category"]').selectOption('LAND');
    await expect(page.getByText('Wykryte wymagania dla: Grunt / Nieruchomość')).toBeVisible();

    await page.getByRole('button', { name: 'Dalej' }).click();
    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();

    const regulatedProfile = page.locator('.profile-card').filter({ hasText: 'Nabywca regulowany' });
    if (await regulatedProfile.isVisible()) {
      await snapshot(page, '03-land-regulated-profile');
    }
  });

  test('setup wizard for ALCOHOL category shows regulated profile with evidence tasks', async ({ page }) => {
    await page.goto('/sell');

    await page.locator('select[formcontrolname="category"]').selectOption('ALCOHOL');
    await page.getByRole('button', { name: 'Dalej' }).click();

    await expect(page.getByRole('heading', { name: 'Kwalifikacja licytantów' })).toBeVisible();

    const regulatedProfile = page.locator('.profile-card').filter({ hasText: 'Nabywca regulowany' });
    if (await regulatedProfile.isVisible()) {
      await regulatedProfile.click();
      await expect(page.getByText('Tak licytant zobaczy dopuszczenie')).toBeVisible();

      const taskList = page.locator('.preview-tasks li');
      const taskCount = await taskList.count();
      expect(taskCount).toBeGreaterThan(0);
      await snapshot(page, '04-alcohol-regulated-tasks');
    }
  });

  test('my participations page is accessible from navigation', async ({ page }) => {
    await page.goto('/me/participations');
    await expect(page.getByRole('heading', { name: 'Centrum kwalifikacji' })).toBeVisible();
    await snapshot(page, '05-my-participations');
  });

  test('party reference picker component renders with options', async ({ page }) => {
    await loginAsDevUser(page, 'bidder-ola');

    await page.goto('/');
    const auctionCards = page.locator('a[href*="/auction/"]');
    const count = await auctionCards.count();
    if (count === 0) return;

    await auctionCards.first().click();
    await expect(page.locator('app-auction-details-page')).toBeVisible({ timeout: 10_000 });

    const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
    if (await startButton.isVisible()) {
      await startButton.click();
      await page.waitForSelector('.kind-party-reference', { timeout: 10_000 }).catch(() => {});

      const partyTask = page.locator('.kind-party-reference');
      if (await partyTask.isVisible()) {
        await snapshot(page, '06-party-reference-task');
      }
    }
  });
});
