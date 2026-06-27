import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import { WadiumStrategy, PricingConfigExciseTypeEnum } from '../../data-access/generated/auction-contract';
import type { CreateAuctionRequest, PricingConfig, Money } from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-auction-create-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auction-create-page.component.html',
  styleUrl: './auction-create-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuctionCreatePageComponent {
  private readonly router = inject(Router);
  private readonly api = inject(AuctionsApiService);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly showPricing = signal(false);

  readonly WadiumStrategy = WadiumStrategy;
  readonly ExciseType = PricingConfigExciseTypeEnum;

  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(4)] }),
    amount: new FormControl('100.00', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] }),
    endsAt: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    wadiumStrategy: new FormControl<WadiumStrategy | ''>('', { nonNullable: true }),
    wadiumRate: new FormControl('', { nonNullable: true }),
    wadiumFixedAmount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    vatRate: new FormControl('0.23', { nonNullable: true, validators: [Validators.required] }),
    isExcisable: new FormControl(false, { nonNullable: true }),
    exciseRate: new FormControl('', { nonNullable: true }),
    exciseType: new FormControl<PricingConfigExciseTypeEnum>(PricingConfigExciseTypeEnum.PERCENTAGE, { nonNullable: true }),
    isImported: new FormControl(false, { nonNullable: true }),
    customsDutyRate: new FormControl('', { nonNullable: true })
  });

  togglePricing(): void {
    this.showPricing.update((v) => !v);
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    try {
      const value = this.form.getRawValue();

      const request: CreateAuctionRequest = {
        title: value.title,
        startingPrice: {
          amount: value.amount,
          currency: value.currency
        } as Money,
        endsAt: new Date(value.endsAt).toISOString()
      };

      if (this.showPricing() && value.wadiumStrategy) {
        const pricingConfig: PricingConfig = {
          vatRate: value.vatRate,
          isExcisable: value.isExcisable,
          isImported: value.isImported
        };

        if (value.wadiumStrategy === WadiumStrategy.PERCENTAGE && value.wadiumRate) {
          pricingConfig.wadiumStrategy = value.wadiumStrategy;
          pricingConfig.wadiumRate = value.wadiumRate;
        } else if (value.wadiumStrategy === WadiumStrategy.FIXED && value.wadiumFixedAmount) {
          pricingConfig.wadiumStrategy = value.wadiumStrategy;
          pricingConfig.wadiumFixedAmount = { amount: value.wadiumFixedAmount, currency: value.currency } as Money;
        }

        if (value.isExcisable && value.exciseRate) {
          pricingConfig.exciseRate = value.exciseRate;
          pricingConfig.exciseType = value.exciseType;
        }

        if (value.isImported && value.customsDutyRate) {
          pricingConfig.customsDutyRate = value.customsDutyRate;
        }

        request.pricingConfig = pricingConfig;
      }

      const created = await this.api.createAuction(request);
      await this.router.navigate(['/auction', created.auctionId]);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się utworzyć aukcji.'));
    } finally {
      this.submitting.set(false);
    }
  }
}
