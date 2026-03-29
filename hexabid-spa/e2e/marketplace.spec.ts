import { expect, test } from '@playwright/test';

test.describe('Marketplace', () => {
  test('pokazuje ekran rynku i główną nawigację', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByRole('link', { name: 'Hexabid' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Rynek' })).toBeVisible();
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Rynek aukcyjny');
    await expect(page.getByRole('heading', { level: 2, name: 'Widok rynku' })).toBeVisible();
  });

  test('umożliwia przejście do ekranu wystawiania aukcji', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'Wystaw aukcję' }).click();

    await expect(page).toHaveURL(/\/sell$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Wystaw aukcję przez SPA' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Opublikuj aukcję' })).toBeVisible();
  });

  test('pozwala wyczyścić filtry wyszukiwania', async ({ page }) => {
    await page.goto('/');

    const searchInput = page.getByPlaceholder('np. komiks, figurka, dzieło sztuki');
    await searchInput.fill('komiks');
    await page.getByRole('button', { name: 'Wyczyść' }).click();

    await expect(searchInput).toHaveValue('');
  });
});
