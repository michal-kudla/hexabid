import { Injectable, computed, inject, signal } from '@angular/core';
import { AppEndpoints } from '../config/app-endpoints';
import { toProfileVm } from '../../data-access/mappers/auction-view.mapper';
import { SessionApiService } from '../../data-access/http/session-api.service';
import { ProfileVm } from '../../data-access/contracts/auction-api.models';
import {
  AuthApi,
  Configuration
} from '../../data-access/generated/auth-contract';

export interface LoginProvider {
  id: string;
  label: string;
  href: string;
}

@Injectable({ providedIn: 'root' })
export class SessionFacade {
  private readonly api = inject(SessionApiService);
  private readonly endpoints = inject(AppEndpoints);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly profile = signal<ProfileVm | null>(null);
  readonly loginProviders = signal<LoginProvider[]>([]);
  readonly isAuthenticated = computed(() => this.profile() !== null);

  constructor() {
    void this.refresh();
    void this.loadLoginProviders();
  }

  async refresh(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const profile = await this.api.getCurrentUserProfile();
      this.profile.set(profile ? toProfileVm(profile) : null);
    } catch (err: unknown) {
      this.error.set(this.api.toMessage(err, 'Nie udało się odczytać sesji użytkownika.'));
      this.profile.set(null);
    } finally {
      this.loading.set(false);
    }
  }

  async logout(): Promise<void> {
    await fetch('/logout', {
      method: 'POST',
      credentials: 'include'
    });

    this.profile.set(null);
    window.location.assign('/');
  }

  private async loadLoginProviders(): Promise<void> {
    try {
      const authApi = new AuthApi(new Configuration({ basePath: '', credentials: 'include' }));
      const providers = await authApi.getAuthProviders();
      this.loginProviders.set(
        providers.map(p => ({
          id: p.registrationId,
          label: p.name,
          href: p.loginUrl
        }))
      );
    } catch {
      this.loginProviders.set(this.endpoints.loginProviders);
    }
  }
}
