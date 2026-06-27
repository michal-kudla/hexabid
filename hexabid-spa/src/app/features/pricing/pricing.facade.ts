import { Injectable, inject, signal } from '@angular/core';
import {
  PriceBreakdownVm,
  WadiumDepositVm,
  WadiumRefundVm
} from '../../data-access/contracts/pricing-api.models';
import { PricingApiService } from '../../data-access/http/pricing-api.service';
import {
  toPriceBreakdownVm,
  toWadiumDepositVm,
  toWadiumRefundVm
} from '../../data-access/mappers/pricing-view.mapper';

@Injectable()
export class PricingFacade {
  private readonly api = inject(PricingApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly breakdown = signal<PriceBreakdownVm | null>(null);
  readonly wadiumDepositing = signal(false);
  readonly wadiumRefunding = signal(false);
  readonly wadiumDeposit = signal<WadiumDepositVm | null>(null);
  readonly wadiumRefund = signal<WadiumRefundVm | null>(null);

  async loadBreakdown(auctionId: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const response = await this.api.getPriceBreakdown(auctionId);
      this.breakdown.set(toPriceBreakdownVm(response));
    } catch (error) {
      this.breakdown.set(null);
      this.error.set(this.api.toMessage(error, 'Nie udało się pobrać kalkulacji ceny.'));
    } finally {
      this.loading.set(false);
    }
  }

  async depositWadium(auctionId: string, amount: string, currency: string): Promise<void> {
    this.wadiumDepositing.set(true);

    try {
      const response = await this.api.depositWadium(auctionId, { amount, currency });
      this.wadiumDeposit.set(toWadiumDepositVm(response));
      await this.loadBreakdown(auctionId);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się wpłacić wadium.'));
    } finally {
      this.wadiumDepositing.set(false);
    }
  }

  async refundWadium(auctionId: string, partyId: string): Promise<void> {
    this.wadiumRefunding.set(true);

    try {
      const response = await this.api.refundWadium(auctionId, partyId);
      this.wadiumRefund.set(toWadiumRefundVm(response));
      await this.loadBreakdown(auctionId);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się zwrócić wadium.'));
    } finally {
      this.wadiumRefunding.set(false);
    }
  }
}
