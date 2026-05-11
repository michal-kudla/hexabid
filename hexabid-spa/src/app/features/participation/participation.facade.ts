import { Injectable, inject, signal } from '@angular/core';
import { ParticipationApiService } from '../../data-access/http/participation-api.service';
import {
  QualificationProgramVm,
  ParticipationDecisionVm,
  SubmitAnswerResultVm
} from '../../data-access/contracts/participation-api.models';
import {
  toQualificationProgramVm,
  toParticipationDecisionVm,
  toSubmitAnswerResultVm
} from '../../data-access/mappers/participation-view.mapper';
import { ParticipationDecisionViewStatusEnum } from '../../data-access/generated/auction-contract';

@Injectable()
export class ParticipationFacade {
  private readonly api = inject(ParticipationApiService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly program = signal<QualificationProgramVm | null>(null);
  readonly decision = signal<ParticipationDecisionVm | null>(null);
  readonly answerSubmitting = signal(false);
  readonly lastAnswerResult = signal<SubmitAnswerResultVm | null>(null);

  private currentAuctionId: string | null = null;

  async loadProgram(auctionId: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.currentAuctionId = auctionId;

    try {
      const response = await this.api.getProgram(auctionId);
      this.program.set(toQualificationProgramVm(response));
      if (response.decision) {
        this.decision.set(toParticipationDecisionVm(response.decision));
      }
    } catch (err: unknown) {
      this.program.set(null);
      this.decision.set(null);
      this.error.set(this.api.toMessage(err, 'Nie udało się pobrać programu dopuszczenia.'));
    } finally {
      this.loading.set(false);
    }
  }

  async startProgram(auctionId: string, templateName: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.currentAuctionId = auctionId;

    try {
      const response = await this.api.startProgram(auctionId, templateName);
      this.program.set(toQualificationProgramVm(response));
      if (response.decision) {
        this.decision.set(toParticipationDecisionVm(response.decision));
      }
    } catch (err: unknown) {
      this.program.set(null);
      this.decision.set(null);
      this.error.set(this.api.toMessage(err, 'Nie udało się rozpocząć programu dopuszczenia.'));
    } finally {
      this.loading.set(false);
    }
  }

  async submitAnswer(statementCode: string, answerValue: string): Promise<void> {
    const auctionId = this.currentAuctionId;
    if (!auctionId) {
      return;
    }

    this.answerSubmitting.set(true);
    this.error.set(null);
    this.lastAnswerResult.set(null);

    try {
      const response = await this.api.submitAnswer(auctionId, statementCode, answerValue);
      this.lastAnswerResult.set(toSubmitAnswerResultVm(response));
      this.program.set(toQualificationProgramVm(response.program));
      if (response.program.decision) {
        this.decision.set(toParticipationDecisionVm(response.program.decision));
      }
    } catch (err: unknown) {
      this.error.set(this.api.toMessage(err, 'Nie udało się złożyć odpowiedzi.'));
    } finally {
      this.answerSubmitting.set(false);
    }
  }

  async refreshDecision(): Promise<void> {
    const auctionId = this.currentAuctionId;
    if (!auctionId) {
      return;
    }

    try {
      const response = await this.api.getDecision(auctionId);
      this.decision.set(toParticipationDecisionVm(response));
    } catch (err: unknown) {
      this.error.set(this.api.toMessage(err, 'Nie udało się pobrać decyzji dopuszczenia.'));
    }
  }

  isAdmitted(): boolean {
    return this.decision()?.status === ParticipationDecisionViewStatusEnum.ADMITTED
      || this.decision()?.status === ParticipationDecisionViewStatusEnum.ADMITTED_WITH_CONDITIONS;
  }

  isRejected(): boolean {
    return this.decision()?.status === ParticipationDecisionViewStatusEnum.REJECTED;
  }

  clear(): void {
    this.program.set(null);
    this.decision.set(null);
    this.error.set(null);
    this.lastAnswerResult.set(null);
    this.currentAuctionId = null;
  }
}
