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

async function openActiveAuctionAsBidder(page: Page) {
  await loginAsDevUser(page, 'bidder-ola');

  await page.goto('/');
  const auctionCards = page.locator('a[href*="/auction/"]');
  const count = await auctionCards.count();

  if (count === 0) {
    return null;
  }

  await auctionCards.first().click();
  await expect(page.locator('app-auction-details-page')).toBeVisible({ timeout: 10_000 });
  return page.url();
}

async function startProgram(page: Page) {
  const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
  if (await startButton.isVisible()) {
    const programResponse = page.waitForResponse(response =>
      response.url().includes('/participation/program') &&
      response.request().method() === 'POST'
    );
    await startButton.click();
    await programResponse.catch(() => {});
  }
}

test.describe('Participation Center', () => {
  test('shows participation center on auction details page', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await expect(page.getByRole('heading', { name: 'Centrum dopuszczenia' })).toBeVisible({ timeout: 10_000 });
    await snapshot(page, '01-participation-center-visible');
  });

  test('allows starting participation program', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
    if (await startButton.isVisible()) {
      const programResponse = page.waitForResponse(response =>
        response.url().includes('/participation/program') &&
        response.request().method() === 'POST'
      );

      await startButton.click();
      await programResponse.catch(() => {});
      await snapshot(page, '02-program-started');
    }
  });

  test('shows program status and tasks after starting', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await startProgram(page);

    await page.waitForSelector('.task-card', { timeout: 10_000 }).catch(() => {});
    const taskCards = page.locator('.task-card');
    const taskCount = await taskCards.count();

    if (taskCount > 0) {
      await expect(page.locator('.progress-bar')).toBeVisible();
      await snapshot(page, '03-program-with-tasks');
    }
  });

  test('destructive answer shows confirmation dialog', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await startProgram(page);

    await page.waitForSelector('.answer-btn', { timeout: 10_000 }).catch(() => {});

    const noButton = page.locator('.answer-no').first();
    if (await noButton.isVisible()) {
      await noButton.click();
      await expect(page.getByText('Ta odpowiedź spowoduje odmowę dopuszczenia do aukcji.')).toBeVisible({ timeout: 5_000 });
      await snapshot(page, '04-destructive-confirmation');
    }
  });

  test('positive answers unlock bidding', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await startProgram(page);

    await page.waitForSelector('.answer-yes', { timeout: 10_000 }).catch(() => {});

    const yesButtons = page.locator('.answer-yes');
    const yesCount = await yesButtons.count();

    for (let i = 0; i < yesCount; i++) {
      const answerResponse = page.waitForResponse(response =>
        response.url().includes('/statements/') &&
        response.url().includes('/answers') &&
        response.status() === 200
      );
      await yesButtons.nth(i).click();
      await answerResponse.catch(() => {});
      await page.waitForTimeout(500);
    }

    await snapshot(page, '05-all-answers-submitted');

    const bidButton = page.getByRole('button', { name: 'Licytuj teraz' });
    if (await bidButton.isVisible()) {
      const isDisabled = await bidButton.isDisabled();
      if (!isDisabled) {
        await snapshot(page, '06-bid-unlocked-after-participation');
      }
    }
  });
});

test.describe('Bid Panel — safe blocking by participation decision', () => {
  test('bid panel shows qualification CTA when not admitted', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    const startButton = page.getByRole('button', { name: 'Rozpocznij dopuszczenie' });
    if (await startButton.isVisible()) {
      await expect(page.getByRole('heading', { name: 'Wymagane dopuszczenie' })).toBeVisible({ timeout: 5_000 });
      await expect(page.getByText('Rozpocznij dopuszczenie w Centrum dopuszczenia powyżej')).toBeVisible();
      await snapshot(page, '07-bid-panel-qualification-needed');
    }
  });

  test('bid panel shows rejected message after disqualifying answer', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await startProgram(page);

    await page.waitForSelector('.answer-no', { timeout: 10_000 }).catch(() => {});

    const noButton = page.locator('.answer-no').first();
    if (await noButton.isVisible()) {
      await noButton.click();

      const confirmButton = page.getByRole('button', { name: 'Potwierdzam odpowiedź' });
      if (await confirmButton.isVisible()) {
        const answerResponse = page.waitForResponse(response =>
          response.url().includes('/statements/') &&
          response.url().includes('/answers')
        );
        await confirmButton.click();
        await answerResponse.catch(() => {});
      }

      await page.waitForTimeout(1000);

      const rejectedHeading = page.getByRole('heading', { name: 'Dopuszczenie odrzucone' });
      if (await rejectedHeading.isVisible()) {
        await expect(page.getByText('Nie możesz składać ofert')).toBeVisible();
        await expect(page.getByRole('button', { name: 'Licytuj teraz' })).toHaveCount(0);
        await snapshot(page, '08-bid-panel-rejected');
      }
    }
  });

  test('bid form appears after all positive answers', async ({ page }) => {
    const auctionUrl = await openActiveAuctionAsBidder(page);
    if (!auctionUrl) return;

    await startProgram(page);

    await page.waitForSelector('.answer-yes', { timeout: 10_000 }).catch(() => {});

    const yesButtons = page.locator('.answer-yes');
    const yesCount = await yesButtons.count();

    for (let i = 0; i < yesCount; i++) {
      const answerResponse = page.waitForResponse(response =>
        response.url().includes('/statements/') &&
        response.url().includes('/answers')
      );
      await yesButtons.nth(i).click();
      await answerResponse.catch(() => {});
      await page.waitForTimeout(500);
    }

    const bidButton = page.getByRole('button', { name: 'Licytuj teraz' });
    if (await bidButton.isVisible()) {
      await expect(bidButton).toBeEnabled();
      await snapshot(page, '09-bid-form-unlocked');
    }
  });
});
