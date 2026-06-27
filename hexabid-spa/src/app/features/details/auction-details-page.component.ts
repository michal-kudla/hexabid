import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuctionDetailsFacade } from './auction-details.facade';
import { SessionFacade } from '../../core/session/session.facade';
import { RulesFacade } from '../rules/rules.facade';
import { ParticipationFacade } from '../participation/participation.facade';
import { RulesPanelComponent } from '../rules/rules-panel.component';
import { DocumentSubmitComponent } from '../rules/document-submit.component';
import { ParticipationCenterComponent } from '../participation/participation-center.component';
import { AuctionBidPanelComponent } from './auction-bid-panel.component';
import { AuctionStatus, DocumentType, DocumentStatus, RulePhase } from '../../data-access/generated/auction-contract';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-auction-details-page',
  imports: [CommonModule, RouterLink, EmptyStateComponent, RulesPanelComponent, DocumentSubmitComponent, ParticipationCenterComponent, AuctionBidPanelComponent],
  templateUrl: './auction-details-page.component.html',
  styleUrl: './auction-details-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [AuctionDetailsFacade, RulesFacade, ParticipationFacade]
})
export class AuctionDetailsPageComponent {
  readonly facade = inject(AuctionDetailsFacade);
  readonly rulesFacade = inject(RulesFacade);
  readonly participationFacade = inject(ParticipationFacade);
  readonly session = inject(SessionFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const auctionId = params.get('auctionId');
      if (auctionId) {
        void this.facade.loadAuction(auctionId);
        void this.rulesFacade.evaluateRules(auctionId);
        void this.participationFacade.loadProgram(auctionId);
      }
    });

    this.destroyRef.onDestroy(() => {
      void this.facade.destroy();
    });
  }

  onBidSubmit(event: { amount: string; currency: string }): void {
    this.facade.submitBid(event.amount, event.currency);
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

  isSeller(): boolean {
    const auction = this.facade.auction();
    const profile = this.session.profile();
    return !!auction && !!profile && auction.sellerId === profile.partyId;
  }
}
