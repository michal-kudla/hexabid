import { Injectable } from '@angular/core';
import { ApiProblemDetail } from '../contracts/auction-api.models';
import {
  AuctionsApi,
  Configuration,
  ResponseError,
  RuleEvaluationResponse,
  RulePhase,
  SubmitDocumentRequest,
  SubmitDocumentResponse,
  DocumentType,
  DocumentStatus
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class RulesApiService {
  private readonly client = new AuctionsApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async evaluateRules(auctionId: string, phase?: RulePhase): Promise<RuleEvaluationResponse> {
    return this.execute(
      () => this.client.evaluateAuctionRules({ auctionId, phase }),
      'Nie udało się pobrać oceny reguł.'
    );
  }

  async submitDocument(
    auctionId: string,
    documentType: DocumentType,
    status: DocumentStatus
  ): Promise<SubmitDocumentResponse> {
    return this.execute(
      () => this.client.submitDocument({
        auctionId,
        submitDocumentRequest: { documentType, status } as SubmitDocumentRequest
      }),
      'Nie udało się złożyć dokumentu.'
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
