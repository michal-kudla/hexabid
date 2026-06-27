import {
  ChangeDetectionStrategy,
  Component,
  input,
  output
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import type { AuctionDetailsVm } from '../../data-access/contracts/auction-api.models';
import type { ParticipationDecisionVm } from '../../data-access/contracts/participation-api.models';

export type BidPanelMode = 'bid' | 'qualification-needed' | 'rejected' | 'seller' | 'inactive';

@Component({
  selector: 'app-auction-bid-panel',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auction-bid-panel.component.html',
  styleUrl: './auction-bid-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuctionBidPanelComponent {
  readonly auction = input.required<AuctionDetailsVm>();
  readonly decision = input<ParticipationDecisionVm | null>(null);
  readonly isSeller = input(false);
  readonly bidSubmitting = input(false);
  readonly hasBiddingBlocks = input(false);

  readonly bidSubmit = output<{ amount: string; currency: string }>();

  readonly bidForm = new FormGroup({
    amount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] })
  });

  mode(): BidPanelMode {
    const auction = this.auction();
    const decision = this.decision();

    if (!auction.isOpen) {
      return 'inactive';
    }
    if (this.isSeller()) {
      return 'seller';
    }
    if (decision?.status === 'REJECTED') {
      return 'rejected';
    }
    if (!decision || (decision.status !== 'ADMITTED' && decision.status !== 'ADMITTED_WITH_CONDITIONS')) {
      return 'qualification-needed';
    }
    return 'bid';
  }

  submitBid(): void {
    if (this.bidForm.invalid) {
      this.bidForm.markAllAsTouched();
      return;
    }
    const { amount, currency } = this.bidForm.getRawValue();
    this.bidSubmit.emit({ amount, currency });
    this.bidForm.controls.amount.reset('');
  }

  disabledReason(): string | null {
    const auction = this.auction();
    if (auction.status === 'DRAFT' || auction.status === 'PUBLISHED') {
      return 'Ta aukcja jest jeszcze przygotowywana przez sprzedającego. Licytacja ruszy po publikacji i starcie aukcji.';
    }
    if (!auction.isOpen) {
      return 'Ta aukcja nie przyjmuje teraz ofert, ponieważ nie jest w statusie aktywnym.';
    }
    if (this.hasBiddingBlocks()) {
      return 'Najpierw spełnij blokujące warunki licytacji, na przykład KYC albo wadium.';
    }
    return null;
  }
}
