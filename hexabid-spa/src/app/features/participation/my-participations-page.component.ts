import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MyParticipationsFacade, ParticipationEntryVm } from './my-participations.facade';
import { SessionFacade } from '../../core/session/session.facade';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import { toAuctionBrowsePageVm } from '../../data-access/mappers/auction-view.mapper';

@Component({
  selector: 'app-my-participations-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './my-participations-page.component.html',
  styleUrl: './my-participations-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MyParticipationsFacade]
})
export class MyParticipationsPageComponent {
  readonly facade = inject(MyParticipationsFacade);
  readonly session = inject(SessionFacade);
  private readonly api = inject(AuctionsApiService);

  constructor() {
    void this.load();
  }

  private async load(): Promise<void> {
    const profile = this.session.profile();
    if (!profile) return;

    try {
      const myBids = await this.api.browseMyBids({ limit: 20 });
      const page = toAuctionBrowsePageVm(myBids);
      const auctionIds = page.items.map(i => i.auctionId);

      if (auctionIds.length > 0) {
        await this.facade.loadParticipations(auctionIds);
      }
    } catch {
      // no auctions
    }
  }

  statusTone(entry: ParticipationEntryVm): string {
    const dec = entry.decision;
    if (!dec) return 'info';
    switch (dec.status) {
      case 'ADMITTED': return 'success';
      case 'ADMITTED_WITH_CONDITIONS': return 'warning';
      case 'REJECTED': return 'error';
      default: return 'info';
    }
  }

  statusLabel(entry: ParticipationEntryVm): string {
    const dec = entry.decision;
    if (!dec) return 'Brak programu';
    return dec.statusLabel;
  }
}
