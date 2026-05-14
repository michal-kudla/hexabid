import { Injectable, inject, signal } from '@angular/core';
import { ParticipationApiService } from '../../data-access/http/participation-api.service';
import {
  QualificationProgramVm,
  ParticipationDecisionVm
} from '../../data-access/contracts/participation-api.models';
import {
  toQualificationProgramVm,
  toParticipationDecisionVm
} from '../../data-access/mappers/participation-view.mapper';

export interface ParticipationEntryVm {
  auctionId: string;
  program: QualificationProgramVm;
  decision: ParticipationDecisionVm | null;
}

@Injectable()
export class MyParticipationsFacade {
  private readonly api = inject(ParticipationApiService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly participations = signal<ParticipationEntryVm[]>([]);

  async loadParticipations(auctionIds: string[]): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.participations.set([]);

    const entries: ParticipationEntryVm[] = [];

    for (const auctionId of auctionIds) {
      try {
        const response = await this.api.getProgram(auctionId);
        const program = toQualificationProgramVm(response);
        const decision = response.decision ? toParticipationDecisionVm(response.decision) : null;
        entries.push({ auctionId, program, decision });
      } catch {
        // no program for this auction - skip
      }
    }

    this.participations.set(entries);
    this.loading.set(false);
  }
}
