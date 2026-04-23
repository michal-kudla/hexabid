import { Injectable } from '@angular/core';
import {
  AuctionPriceBreakdownResponse,
  AuctionsApi,
  Configuration,
  DepositWadiumRequest,
  Money,
  RefundWadiumRequest,
  ResponseError,
  WadiumRefundResponse,
  WadiumResponse
} from '../generated/auction-contract';
import { ApiProblemDetail } from '../contracts/auction-api.models';

@Injectable({ providedIn: 'root' })
export class PricingApiService {
  private readonly client = new AuctionsApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async getPriceBreakdown(auctionId: string): Promise<AuctionPriceBreakdownResponse> {
    return this.execute(
      () => this.client.getAuctionPrice({ auctionId }),
      'Nie udało się pobrać kalkulacji ceny.'
    );
  }

  async depositWadium(auctionId: string, amount: Money): Promise<WadiumResponse> {
    return this.execute(
      () =>
        this.client.depositWadium({
          auctionId,
          depositWadiumRequest: { amount } as DepositWadiumRequest
        }),
      'Nie udało się wpłacić wadium.'
    );
  }

  async refundWadium(auctionId: string, partyId: string): Promise<WadiumRefundResponse> {
    return this.execute(
      () =>
        this.client.refundWadium({
          auctionId,
          refundWadiumRequest: { partyId } as RefundWadiumRequest
        }),
      'Nie udało się zwrócić wadium.'
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
        return new Error('Ta operacja wymaga zalogowania.');
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
