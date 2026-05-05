import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuctionDetailsFacade } from './auction-details.facade';
import { SessionFacade } from '../../core/session/session.facade';
import { RulesFacade } from '../rules/rules.facade';
import { RulesPanelComponent } from '../rules/rules-panel.component';
import { DocumentSubmitComponent } from '../rules/document-submit.component';
import { AuctionStatus, DocumentType, DocumentStatus, RulePhase } from '../../data-access/generated/auction-contract';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-auction-details-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, EmptyStateComponent, RulesPanelComponent, DocumentSubmitComponent],
  templateUrl: './auction-details-page.component.html',
  styleUrl: './auction-details-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [AuctionDetailsFacade, RulesFacade]
})
export class AuctionDetailsPageComponent {
  readonly facade = inject(AuctionDetailsFacade);
  readonly rulesFacade = inject(RulesFacade);
  readonly session = inject(SessionFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly bidForm = new FormGroup({
    amount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] })
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const auctionId = params.get('auctionId');
      if (auctionId) {
        void this.facade.loadAuction(auctionId);
        void this.rulesFacade.evaluateRules(auctionId);
      }
    });

    this.destroyRef.onDestroy(() => {
      void this.facade.destroy();
    });
  }

  submitBid(): void {
    if (this.bidForm.invalid) {
      this.bidForm.markAllAsTouched();
      return;
    }

    const { amount, currency } = this.bidForm.getRawValue();
    this.facade.submitBid(amount, currency);
    this.bidForm.controls.amount.reset('');
  }

  onDocumentSubmitted(event: { documentType: DocumentType; status: DocumentStatus }): void {
    const auctionId = this.route.snapshot.paramMap.get('auctionId');
    if (auctionId) {
      void this.rulesFacade.submitDocument(auctionId, event.documentType, event.status);
    }
  }

  activateAuction(): void {
    void this.facade.activateAuction();
  }

  canActivateAuction(): boolean {
    const auction = this.facade.auction();
    const profile = this.session.profile();
    return !!auction && !!profile && auction.sellerId === profile.partyId && (
      auction.status === AuctionStatus.DRAFT || auction.status === AuctionStatus.PUBLISHED
    );
  }

  hasBiddingBlocks(): boolean {
    return this.rulesFacade.evaluation()?.evaluations.some(
      evaluation => evaluation.phase === RulePhase.BIDDING && evaluation.hasBlockingViolations
    ) ?? false;
  }

  hasSettlementBlocks(): boolean {
    return this.rulesFacade.evaluation()?.evaluations.some(
      evaluation => evaluation.phase === RulePhase.SETTLEMENT && evaluation.hasBlockingViolations
    ) ?? false;
  }

  bidDisabledReason(): string | null {
    const auction = this.facade.auction();
    if (!auction) {
      return null;
    }
    if (auction.status === AuctionStatus.DRAFT || auction.status === AuctionStatus.PUBLISHED) {
      return 'Ta aukcja jest jeszcze przygotowywana przez sprzedającego. Licytacja ruszy po publikacji i starcie aukcji.';
    }
    if (!auction.isOpen) {
      return 'Ta aukcja nie przyjmuje teraz ofert, ponieważ nie jest w statusie aktywnym.';
    }
    if (auction.sellerId === this.session.profile()?.partyId) {
      return 'Jesteś sprzedającym tej aukcji. Sprzedający nie może licytować własnej oferty.';
    }
    if (this.hasBiddingBlocks()) {
      return 'Najpierw spełnij blokujące warunki licytacji, na przykład KYC albo wadium.';
    }
    return null;
  }
}
