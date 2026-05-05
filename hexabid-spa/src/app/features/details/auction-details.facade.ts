import { Injectable, inject, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import {
  AuctionStatus,
  AuctionDetailsVm,
  AuctionRealtimeMessage,
  BidVm
} from '../../data-access/contracts/auction-api.models';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import {
  toAuctionDetailsVm,
  toBidVm
} from '../../data-access/mappers/auction-view.mapper';
import {
  AuctionRealtimeGateway,
  AuctionRealtimeSession
} from '../../data-access/realtime/auction-realtime.gateway';

interface FeedItem {
  title: string;
  detail: string;
  tone: 'info' | 'error' | 'success';
}

@Injectable()
export class AuctionDetailsFacade {
  private readonly api = inject(AuctionsApiService);
  private readonly realtime = inject(AuctionRealtimeGateway);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly auction = signal<AuctionDetailsVm | null>(null);
  readonly feed = signal<FeedItem[]>([]);
  readonly bidSubmitting = signal(false);
  readonly activationSubmitting = signal(false);

  private currentAuctionId: string | null = null;
  private realtimeSession: AuctionRealtimeSession | null = null;
  private realtimeSubscription: Subscription | null = null;

  async loadAuction(auctionId: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.feed.set([]);
    this.currentAuctionId = auctionId;

    try {
      const auction = toAuctionDetailsVm(await this.api.getAuctionById(auctionId));
      this.auction.set(auction);
      this.connectRealtime(auctionId);
    } catch (error) {
      this.auction.set(null);
      this.error.set(this.api.toMessage(error, 'Nie udało się załadować szczegółów aukcji.'));
    } finally {
      this.loading.set(false);
    }
  }

  submitBid(amount: string, currency: string): void {
    if (!this.realtimeSession) {
      this.feed.update((items) => [
        {
          title: 'Brak kanału aukcyjnego',
          detail: 'Połączenie real-time nie jest jeszcze gotowe.',
          tone: 'error'
        },
        ...items
      ]);
      return;
    }

    this.bidSubmitting.set(true);
    this.realtimeSession.placeBid({ amount, currency });
  }

  async activateAuction(): Promise<void> {
    const auctionId = this.currentAuctionId;
    if (!auctionId) {
      return;
    }

    this.activationSubmitting.set(true);
    this.error.set(null);

    try {
      const activated = toAuctionDetailsVm(await this.api.activateAuction(auctionId));
      this.auction.set(activated);
      this.feed.update((items) => [
        {
          title: 'Aukcja uruchomiona',
          detail: 'Status zmienił się na aktywny. Od tej chwili inni użytkownicy mogą składać oferty.',
          tone: 'success'
        },
        ...items
      ]);
    } catch (error) {
      this.feed.update((items) => [
        {
          title: 'Nie udało się uruchomić aukcji',
          detail: this.api.toMessage(error, 'Spróbuj ponownie po sprawdzeniu statusu i uprawnień sprzedającego.'),
          tone: 'error'
        },
        ...items
      ]);
    } finally {
      this.activationSubmitting.set(false);
    }
  }

  async destroy(): Promise<void> {
    this.realtimeSubscription?.unsubscribe();
    this.realtimeSubscription = null;
    await this.realtimeSession?.disconnect();
    this.realtimeSession = null;
  }

  private connectRealtime(auctionId: string): void {
    this.realtimeSubscription?.unsubscribe();
    void this.realtimeSession?.disconnect();

    this.realtimeSession = this.realtime.openAuctionChannel(auctionId);
    this.realtimeSubscription = this.realtimeSession.messages$.subscribe((message) => {
      this.handleRealtimeMessage(message);
    });
  }

  private handleRealtimeMessage(message: AuctionRealtimeMessage): void {
    if (message.kind === 'bid-accepted') {
      this.bidSubmitting.set(false);
      const bid = toBidVm({
        bidderId: message.bidderId,
        amount: message.amount,
        currency: message.currency,
        placedAt: message.placedAt
      });

      this.applyAcceptedBid(bid);
      this.feed.update((items) => [
        {
          title: 'Oferta zaakceptowana',
          detail: `${bid.bidderLabel} prowadzi z kwotą ${bid.priceLabel}.`,
          tone: 'success'
        },
        ...items
      ]);
      return;
    }

    if (message.kind === 'bid-rejected') {
      this.bidSubmitting.set(false);
      this.feed.update((items) => [
        {
          title: `Oferta odrzucona: ${message.reason}`,
          detail: message.message,
          tone: 'error'
        },
        ...items
      ]);
      return;
    }

    if (message.kind === 'connection-error') {
      this.feed.update((items) => [
        {
          title: 'Problem z połączeniem',
          detail: message.message,
          tone: 'error'
        },
        ...items
      ]);
      return;
    }

    this.feed.update((items) => [
      {
        title: message.eventType,
        detail: JSON.stringify(message.payload),
        tone: 'info'
      },
      ...items
    ]);

    this.auction.update((auction) => {
      if (!auction) {
        return auction;
      }

      if (message.eventType === 'AUCTION_LEADER_CHANGED') {
        const newLeaderId = typeof message.payload['newLeaderId'] === 'string' ? message.payload['newLeaderId'] : auction.leadingBidderId;
        const newLeadingPrice =
          typeof message.payload['newLeadingPrice'] === 'string'
            ? message.payload['newLeadingPrice']
            : auction.currentPrice;
        const currency =
          typeof message.payload['currency'] === 'string'
            ? message.payload['currency']
            : auction.currency;

        return {
          ...auction,
          currentPrice: newLeadingPrice,
          currency,
          priceLabel: `${newLeadingPrice} ${currency}`,
          leadingBidderId: newLeaderId,
          leadingBidderLabel: newLeaderId ?? 'Brak lidera'
        };
      }

      if (message.eventType === 'AUCTION_WON' || message.eventType === 'AUCTION_CLOSED_WITHOUT_WINNER') {
        return {
          ...auction,
          status: AuctionStatus.CLOSED,
          statusLabel: 'Zamknięta',
          statusTone: 'closed',
          isOpen: false
        };
      }

      return auction;
    });
  }

  private applyAcceptedBid(bid: BidVm): void {
    this.auction.update((auction) => {
      if (!auction) {
        return auction;
      }

      return {
        ...auction,
        currentPrice: bid.amount,
        currency: bid.currency,
        priceLabel: bid.priceLabel,
        leadingBidderId: bid.bidderId,
        leadingBidderLabel: bid.bidderLabel,
        bidHistory: [bid, ...auction.bidHistory],
        totalBids: auction.totalBids + 1
      };
    });
  }
}
