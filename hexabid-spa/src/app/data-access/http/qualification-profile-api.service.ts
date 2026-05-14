import { Injectable } from '@angular/core';
import { ApiProblemDetail } from '../contracts/auction-api.models';
import {
  QualificationApi,
  Configuration,
  ResponseError,
  QualificationProfileListResponse,
  QualificationProfileSummary
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class QualificationProfileApiService {
  private readonly client = new QualificationApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async browseProfiles(category?: string): Promise<QualificationProfileListResponse> {
    return this.execute(
      () => this.client.browseQualificationProfiles({
        category: category ?? undefined
      }),
      'Nie udało się pobrać katalogu profili kwalifikacyjnych.'
    );
  }

  toMessage(error: unknown, fallback: string): string {
    return error instanceof Error ? error.message : fallback;
  }

  private async execute<T>(operation: () => Promise<T>, fallback: string): Promise<T> {
    try {
      return await operation();
    } catch (error) {
      throw await this.normalizeError(error, fallback);
    }
  }

  private async normalizeError(error: unknown, fallback: string): Promise<Error> {
    if (error instanceof ResponseError) {
      if (error.response.status === 401) {
        return new Error('Ta operacja wymaga zalogowania w backendzie OAuth2.');
      }
      try {
        const problem = (await error.response.clone().json()) as ApiProblemDetail;
        return new Error(problem.detail ?? fallback);
      } catch {
        return new Error(fallback);
      }
    }
    return error instanceof Error ? error : new Error(fallback);
  }
}
