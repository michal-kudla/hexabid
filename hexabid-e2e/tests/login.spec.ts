import { test, expect } from '@playwright/test';

test('Logowanie uzytkownika i weryfikacja uwierzytelnienia', async ({ page }) => {
  // Przejdź na stronę główną
  await page.goto('/');

  // Oczekujemy, że z jakiegoś powodu niezalogowany użytkownik trafi na widok powitalny 
  // lub od razu kliknie 'Zaloguj'
  // Ponieważ w Hexabid mamy auth poprzez OAuth2 i własny Dev Profile, logowanie
  // zwykle objawia się kliknięciem w przycisk logowania.
  
  // Zakładamy, że aplikacja ma link lub przycisk logowania. Jeśli od razu wymusza login - 
  // trafimy na stronę autoryzacji. Poniższy skrypt uderza prosto do endpointu deweloperskiego.
  
  await page.goto('http://localhost:18080/dev-auth?redirect=http://localhost:14002/');
  
  // W profilu dev wyświetli się lista opcji z danymi (użytkownicy developerscy)
  // Wybieramy na przykład 'user1' klikając w button logujący do mocka
  const loginButton = page.locator('button', { hasText: /Login jako user/i }).first();
  if (await loginButton.isVisible()) {
      await loginButton.click();
  }

  // Po zalogowaniu zostaniemy przeniesieni z powrotem na port 4200.
  await page.waitForURL('http://localhost:14002/**');

  // Weryfikacja: Szukamy elementu, który świadczy o zalogowaniu, np. ikony profilu albo tekstu
  const profileSection = page.locator('nav').filter({ hasText: /Wyloguj|Mój profil/i });
  await expect(profileSection).toBeVisible({ timeout: 10000 });
});
