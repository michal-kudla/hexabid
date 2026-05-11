import { Injectable } from '@angular/core';
import { ApiProblemDetail } from '../contracts/auction-api.models';
import {
  ParticipationApi,
  Configuration,
  ResponseError,
  StatementProgramView,
  ParticipationDecisionView,
  SubmitStatementAnswerResponse
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class ParticipationApiService {
  private readonly client = new ParticipationApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async getProgram(auctionId: string): Promise<StatementProgramView> {
    return this.execute(
      () => this.client.getParticipationProgram({ auctionId }),
      'Nie udało się pobrać programu dopuszczenia.'
    );
  }

  async startProgram(auctionId: string, templateName: string): Promise<StatementProgramView> {
    return this.execute(
      () => this.client.startParticipationProgram({
        auctionId,
        startParticipationProgramRequest: { templateName }
      }),
      'Nie udało się rozpocząć programu dopuszczenia.'
    );
  }

  async getDecision(auctionId: string): Promise<ParticipationDecisionView> {
    return this.execute(
      () => this.client.getParticipationDecision({ auctionId }),
      'Nie udało się pobrać decyzji dopuszczenia.'
    );
  }

  async submitAnswer(
    auctionId: string,
    statementCode: string,
    answerValue: string
  ): Promise<SubmitStatementAnswerResponse> {
    return this.execute(
      () => this.client.submitStatementAnswer({
        auctionId,
        statementCode,
        submitStatementAnswerRequest: { answerValue }
      }),
      'Nie udało się złożyć odpowiedzi.'
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
