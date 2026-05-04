import type { RulePhase, RuleStatus, RuleSeverity, DocumentType, DocumentStatus } from '../generated/auction-contract';

export { RulePhase, RuleStatus, RuleSeverity, DocumentType, DocumentStatus } from '../generated/auction-contract';

export interface RuleViolationVm {
  ruleName: string;
  message: string;
  blocking: boolean;
  requiredAction: string;
  status: RuleStatus;
  severity: RuleSeverity;
  statusLabel: string;
  severityLabel: string;
  tone: 'success' | 'warning' | 'error';
}

export interface RulePhaseEvaluationVm {
  phase: RulePhase;
  rules: RuleViolationVm[];
  hasBlockingViolations: boolean;
  phaseLabel: string;
}

export interface RuleEvaluationVm {
  auctionId: string;
  evaluations: RulePhaseEvaluationVm[];
  hasAnyBlockingViolations: boolean;
}

export interface SubmitDocumentVm {
  documentType: DocumentType;
  status: DocumentStatus;
  documentTypeLabel: string;
  statusLabel: string;
}
