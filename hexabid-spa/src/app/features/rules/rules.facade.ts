import { Injectable, inject, signal } from '@angular/core';
import { RulePhase, DocumentType, DocumentStatus } from '../../data-access/generated/auction-contract';
import {
  RuleEvaluationVm,
  SubmitDocumentVm
} from '../../data-access/contracts/rules-api.models';
import { RulesApiService } from '../../data-access/http/rules-api.service';
import {
  toRuleEvaluationVm,
  toSubmitDocumentVm
} from '../../data-access/mappers/rules-view.mapper';

@Injectable()
export class RulesFacade {
  private readonly api = inject(RulesApiService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly evaluation = signal<RuleEvaluationVm | null>(null);
  readonly documentSubmitting = signal(false);
  readonly submittedDocument = signal<SubmitDocumentVm | null>(null);

  async evaluateRules(auctionId: string, phase?: RulePhase): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const response = await this.api.evaluateRules(auctionId, phase);
      this.evaluation.set(toRuleEvaluationVm(response));
    } catch (err: unknown) {
      this.evaluation.set(null);
      this.error.set(this.api.toMessage(err, 'Nie udało się pobrać oceny reguł.'));
    } finally {
      this.loading.set(false);
    }
  }

  async submitDocument(
    auctionId: string,
    documentType: DocumentType,
    status: DocumentStatus
  ): Promise<void> {
    this.documentSubmitting.set(true);
    this.error.set(null);

    try {
      const response = await this.api.submitDocument(auctionId, documentType, status);
      this.submittedDocument.set(toSubmitDocumentVm(response));
      await this.evaluateRules(auctionId);
    } catch (err: unknown) {
      this.error.set(this.api.toMessage(err, 'Nie udało się złożyć dokumentu.'));
    } finally {
      this.documentSubmitting.set(false);
    }
  }

  clearEvaluation(): void {
    this.evaluation.set(null);
    this.error.set(null);
    this.submittedDocument.set(null);
  }
}
