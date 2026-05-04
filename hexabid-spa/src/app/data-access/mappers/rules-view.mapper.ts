import type {
  RuleEvaluationResponse,
  RulePhaseEvaluation,
  RuleViolationItem as GeneratedRuleViolationItem,
  SubmitDocumentResponse as GeneratedSubmitDocumentResponse
} from '../generated/auction-contract';
import { RulePhase, RuleStatus, RuleSeverity, DocumentType, DocumentStatus } from '../generated/auction-contract';
import type {
  RuleViolationVm,
  RulePhaseEvaluationVm,
  RuleEvaluationVm,
  SubmitDocumentVm
} from '../contracts/rules-api.models';

function ruleStatusLabel(status: RuleStatus): string {
  switch (status) {
    case RuleStatus.SATISFIED: return 'Spełniona';
    case RuleStatus.PENDING: return 'Oczekująca';
    case RuleStatus.VIOLATED: return 'Naruszona';
    default: return status;
  }
}

function ruleSeverityLabel(severity: RuleSeverity): string {
  switch (severity) {
    case RuleSeverity.BLOCKING: return 'Blokująca';
    case RuleSeverity.WARNING: return 'Ostrzeżenie';
    case RuleSeverity.INFORMATIVE: return 'Informacyjna';
    default: return severity;
  }
}

function ruleTone(status: RuleStatus): 'success' | 'warning' | 'error' {
  switch (status) {
    case RuleStatus.SATISFIED: return 'success';
    case RuleStatus.PENDING: return 'warning';
    case RuleStatus.VIOLATED: return 'error';
  }
}

function phaseLabel(phase: RulePhase): string {
  switch (phase) {
    case RulePhase.PARTICIPATION: return 'Uczestnictwo';
    case RulePhase.BIDDING: return 'Licytacja';
    case RulePhase.SETTLEMENT: return 'Rozliczenie';
    default: return phase;
  }
}

function documentTypeLabel(type: DocumentType): string {
  switch (type) {
    case DocumentType.EXCISE_CERTIFICATE: return 'Zaświadczenie akcyzowe';
    case DocumentType.CUSTOMS_EXEMPTION: return 'Zwolnienie celne';
    case DocumentType.REAL_ESTATE_SETTLEMENT: return 'Dokument rozliczenia nieruchomości';
    case DocumentType.IDENTITY_VERIFICATION: return 'Weryfikacja tożsamości';
    case DocumentType.VEHICLE_REGISTRATION: return 'Rejestracja pojazdu';
    default: return type;
  }
}

function documentStatusLabel(status: DocumentStatus): string {
  switch (status) {
    case DocumentStatus.MISSING: return 'Brak';
    case DocumentStatus.COPY: return 'Kopia';
    case DocumentStatus.ORIGINAL: return 'Oryginał';
    default: return status;
  }
}

export function toRuleViolationVm(item: GeneratedRuleViolationItem): RuleViolationVm {
  return {
    ruleName: item.ruleName,
    message: item.message,
    blocking: item.blocking,
    requiredAction: item.requiredAction,
    status: item.status,
    severity: item.severity,
    statusLabel: ruleStatusLabel(item.status),
    severityLabel: ruleSeverityLabel(item.severity),
    tone: ruleTone(item.status)
  };
}

export function toRulePhaseEvaluationVm(eval_: RulePhaseEvaluation): RulePhaseEvaluationVm {
  return {
    phase: eval_.phase,
    rules: eval_.rules.map(toRuleViolationVm),
    hasBlockingViolations: eval_.hasBlockingViolations,
    phaseLabel: phaseLabel(eval_.phase)
  };
}

export function toRuleEvaluationVm(response: RuleEvaluationResponse): RuleEvaluationVm {
  const evaluations = response.evaluations.map(toRulePhaseEvaluationVm);
  return {
    auctionId: response.auctionId,
    evaluations,
    hasAnyBlockingViolations: evaluations.some(e => e.hasBlockingViolations)
  };
}

export function toSubmitDocumentVm(response: GeneratedSubmitDocumentResponse): SubmitDocumentVm {
  return {
    documentType: response.documentType,
    status: response.status,
    documentTypeLabel: documentTypeLabel(response.documentType),
    statusLabel: documentStatusLabel(response.status)
  };
}
