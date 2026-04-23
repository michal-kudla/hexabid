import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PricingFacade } from './pricing.facade';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-pricing-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, EmptyStateComponent],
  templateUrl: './pricing-page.component.html',
  styleUrl: './pricing-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [PricingFacade]
})
export class PricingPageComponent {
  readonly facade = inject(PricingFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly auctionLink = computed(() => `/auction/${this.auctionId}`);

  readonly wadiumForm = new FormGroup({
    amount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] })
  });

  readonly refundForm = new FormGroup({
    partyId: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  private auctionId: string | null = null;

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.auctionId = params.get('auctionId');
      if (this.auctionId) {
        void this.facade.loadBreakdown(this.auctionId);
      }
    });
  }

  submitWadium(): void {
    if (this.wadiumForm.invalid || !this.auctionId) {
      this.wadiumForm.markAllAsTouched();
      return;
    }

    const { amount, currency } = this.wadiumForm.getRawValue();
    void this.facade.depositWadium(this.auctionId, amount, currency);
    this.wadiumForm.controls.amount.reset('');
  }

  submitRefund(): void {
    if (this.refundForm.invalid || !this.auctionId) {
      this.refundForm.markAllAsTouched();
      return;
    }

    const { partyId } = this.refundForm.getRawValue();
    void this.facade.refundWadium(this.auctionId, partyId);
    this.refundForm.controls.partyId.reset('');
  }
}
