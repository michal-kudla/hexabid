import { expect, test } from '@playwright/test';

test.describe('Tworzenie aukcji z konfiguracją ceny', () => {
  test('umożliwia rozwinięcie sekcji konfiguracji ceny', async ({ page }) => {
    await page.goto('/sell');

    await expect(page.getByRole('heading', { level: 1, name: 'Wystaw aukcję przez SPA' })).toBeVisible();

    const toggleButton = page.getByRole('button', { name: 'Dodaj konfigurację ceny' });
    await expect(toggleButton).toBeVisible();
    await toggleButton.click();

    await expect(page.getByText('Konfiguracja ceny (PricingConfig)')).toBeVisible();
    await expect(page.getByText('Strategia wadium')).toBeVisible();
    await expect(page.getByText('Stawka VAT')).toBeVisible();
  });

  test('pozwala wybrać strategię wadium i wpisać stawkę', async ({ page }) => {
    await page.goto('/sell');

    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();

    const strategySelect = page.locator('select[formcontrolname="wadiumStrategy"]');

    await strategySelect.selectOption('PERCENTAGE');
    await expect(page.getByText('Stawka wadium (np. 0.05 = 5%)')).toBeVisible();

    await strategySelect.selectOption('FIXED');
    await expect(page.getByText('Stała kwota wadium')).toBeVisible();
  });

  test('pokazuje pola akcyzy i cła po zaznaczeniu opcji', async ({ page }) => {
    await page.goto('/sell');

    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();

    const exciseSelect = page.locator('select[formcontrolname="isExcisable"]');
    await exciseSelect.selectOption('true');
    await expect(page.getByText('Stawka akcyzy')).toBeVisible();
    await expect(page.getByText('Typ akcyzy')).toBeVisible();

    const importSelect = page.locator('select[formcontrolname="isImported"]');
    await importSelect.selectOption('true');
    await expect(page.getByText('Stawka cła')).toBeVisible();
  });

  test('ukrywa sekcję konfiguracji po ponownym kliknięciu', async ({ page }) => {
    await page.goto('/sell');

    await page.getByRole('button', { name: 'Dodaj konfigurację ceny' }).click();
    await expect(page.getByText('Konfiguracja ceny (PricingConfig)')).toBeVisible();

    await page.getByRole('button', { name: 'Ukryj konfigurację ceny' }).click();
    await expect(page.getByText('Konfiguracja ceny (PricingConfig)')).not.toBeVisible();
  });
});
