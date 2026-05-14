import type {
  StatementProgramView,
  StatementStepView,
  ParticipationDecisionView as GeneratedDecision,
  SubmitStatementAnswerResponse as GeneratedSubmitResponse
} from '../generated/auction-contract';
import {
  StatementProgramViewStatusEnum,
  ParticipationDecisionViewStatusEnum,
  StatementStepViewAnswerTypeEnum
} from '../generated/auction-contract';
import type {
  QualificationProgramVm,
  QualificationTaskVm,
  QualificationStageVm,
  ParticipationDecisionVm,
  SubmitAnswerResultVm,
  TaskSeverity,
  QualificationTaskKind
} from '../contracts/participation-api.models';

function programStatusLabel(status: StatementProgramViewStatusEnum): string {
  switch (status) {
    case StatementProgramViewStatusEnum.NOT_STARTED: return 'Nie rozpoczęto';
    case StatementProgramViewStatusEnum.IN_PROGRESS: return 'Wymaga działania';
    case StatementProgramViewStatusEnum.COMPLETED: return 'Ukończony';
    case StatementProgramViewStatusEnum.REJECTED: return 'Odrzucony';
    case StatementProgramViewStatusEnum.CANCELLED: return 'Anulowany';
    default: return status;
  }
}

function programActionLabel(status: StatementProgramViewStatusEnum): string {
  switch (status) {
    case StatementProgramViewStatusEnum.NOT_STARTED: return 'Rozpocznij dopuszczenie';
    case StatementProgramViewStatusEnum.IN_PROGRESS: return 'Kontynuuj';
    case StatementProgramViewStatusEnum.COMPLETED: return 'Przejdź do formularza oferty';
    case StatementProgramViewStatusEnum.REJECTED: return 'Zobacz szczegóły';
    case StatementProgramViewStatusEnum.CANCELLED: return 'Rozpocznij ponownie';
    default: return 'Kontynuuj';
  }
}

function decisionStatusLabel(status: ParticipationDecisionViewStatusEnum): string {
  switch (status) {
    case ParticipationDecisionViewStatusEnum.PENDING: return 'Oczekuje';
    case ParticipationDecisionViewStatusEnum.ADMITTED: return 'Dopuszczony';
    case ParticipationDecisionViewStatusEnum.ADMITTED_WITH_CONDITIONS: return 'Dopuszczony z warunkami';
    case ParticipationDecisionViewStatusEnum.REJECTED: return 'Odrzucony';
    default: return status;
  }
}

function decisionTone(status: ParticipationDecisionViewStatusEnum): 'success' | 'warning' | 'error' | 'info' {
  switch (status) {
    case ParticipationDecisionViewStatusEnum.ADMITTED: return 'success';
    case ParticipationDecisionViewStatusEnum.ADMITTED_WITH_CONDITIONS: return 'warning';
    case ParticipationDecisionViewStatusEnum.REJECTED: return 'error';
    default: return 'info';
  }
}

function isDestructiveAnswer(answerType: StatementStepViewAnswerTypeEnum, answerValue: string): boolean {
  if (answerType === StatementStepViewAnswerTypeEnum.YES_NO && answerValue.toUpperCase() === 'NO') {
    return true;
  }
  return false;
}

function taskSeverity(step: StatementStepView, status: QualificationTaskVm['status']): TaskSeverity {
  if (status === 'COMPLETED') return 'INFO';
  const code = step.statementCode;
  if (code.includes('TERMS_ACCEPTANCE') || code.includes('DATA_ROOM') || code.includes('INSIDER')) {
    return 'INFO';
  }
  if (code.includes('PAYMENT_READINESS') || code.includes('BID_BOND') || code.includes('ENVIRONMENTAL')) {
    return 'IMPORTANT';
  }
  return 'BLOCKING';
}

function inferTaskKind(code: string): QualificationTaskKind {
  if (code.includes('LEGAL_CAPACITY') || code.includes('ACTING_AS_SELF') || code.includes('BENEFICIAL_OWNER')) {
    return 'PARTY_REFERENCE';
  }
  if (code.includes('SANCTIONS') || code.includes('PEP_DISCLOSURE') || code.includes('AML') || code.includes('NO_SANCTIONS')) {
    return 'EXTERNAL_CHECK';
  }
  if (code.includes('SECTOR_LICENSE') || code.includes('PERMIT') || code.includes('EXPORT_CONTROL') || code.includes('ENVIRONMENTAL')) {
    return 'EVIDENCE';
  }
  if (code.includes('ADULT') || code.includes('AGE') || code.includes('KYC') || code.includes('IDENTITY')) {
    return 'VERIFIED_FACT';
  }
  return 'STATEMENT';
}

function kindLabel(kind: QualificationTaskKind): string {
  switch (kind) {
    case 'STATEMENT': return 'Oświadczenie';
    case 'VERIFIED_FACT': return 'Wymóg weryfikacji';
    case 'EVIDENCE': return 'Wymagany dokument';
    case 'EXTERNAL_CHECK': return 'Sprawdzenie zewnętrzne';
    case 'PARTY_REFERENCE': return 'Identyfikacja podmiotu';
  }
}

function kindDescription(kind: QualificationTaskKind): string {
  switch (kind) {
    case 'STATEMENT': return 'Musisz złożyć oświadczenie.';
    case 'VERIFIED_FACT': return 'Ten wymóg wymaga weryfikacji — samo oświadczenie może nie wystarczyć.';
    case 'EVIDENCE': return 'Może być wymagany dokument lub licencja.';
    case 'EXTERNAL_CHECK': return 'System lub operator musi zweryfikować ten wymóg.';
    case 'PARTY_REFERENCE': return 'Musisz określić, w jakim charakterze działasz.';
  }
}

export function toQualificationTaskVm(
  step: StatementStepView,
  status: QualificationTaskVm['status'],
  templateName?: string
): QualificationTaskVm {
  const kind = inferTaskKind(step.statementCode);
  const destructiveAnswers = isDestructiveAnswer(step.answerType, 'NO')
    ? [{ answerValue: 'NO', consequence: 'Ta odpowiedź spowoduje odmowę dopuszczenia do aukcji.' }]
    : [];

  return {
    code: step.statementCode,
    kind,
    status,
    title: step.title,
    question: step.question,
    answerType: step.answerType,
    answerValue: step.answerValue,
    order: step.order,
    stepLabel: step.stepLabel,
    severity: taskSeverity(step, status),
    blockedBy: [],
    destructiveAnswers,
    sourceProfileLabels: templateName ? [templateName] : [],
    subjectLabel: step.stepLabel ?? 'Zalogowany użytkownik',
    subjectRole: 'bidder',
    explanation: kindDescription(kind)
  };
}

export function toQualificationProgramVm(response: StatementProgramView): QualificationProgramVm {
  const templateName = response.templateName;
  const available = response.availableStatements.map(s => toQualificationTaskVm(s, 'AVAILABLE', templateName));
  const completed = response.completedStatements.map(s => toQualificationTaskVm(s, 'COMPLETED', templateName));
  const blocked = response.blockedStatements.map(s => toQualificationTaskVm(s, 'BLOCKED', templateName));

  const tasks = [...available, ...completed, ...blocked].sort((a, b) => a.order - b.order);
  const totalCount = available.length + completed.length + blocked.length;
  const completedCount = completed.length;
  const progressPercent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;
  const stages = buildStages(tasks);

  return {
    auctionId: response.auctionId,
    programInstanceId: response.programInstanceId,
    templateName: response.templateName,
    status: response.status,
    headline: programStatusLabel(response.status),
    userActionLabel: programActionLabel(response.status),
    completedCount,
    totalCount,
    progressPercent,
    tasks,
    stages
  };
}

function buildStages(tasks: QualificationTaskVm[]): QualificationStageVm[] {
  const stageMap = new Map<string, QualificationTaskVm[]>();
  for (const task of tasks) {
    const label = task.stepLabel ?? 'Dopuszczenie';
    if (!stageMap.has(label)) {
      stageMap.set(label, []);
    }
    stageMap.get(label)!.push(task);
  }

  const stages: QualificationStageVm[] = [];
  let hasCurrent = false;
  for (const [label, stageTasks] of stageMap) {
    const allCompleted = stageTasks.every(t => t.status === 'COMPLETED');
    const anyAvailable = stageTasks.some(t => t.status === 'AVAILABLE');
    const anyFailed = stageTasks.some(t => t.status === 'FAILED');
    let status: QualificationStageVm['status'] = 'LOCKED';
    if (allCompleted) {
      status = 'DONE';
    } else if (anyFailed) {
      status = 'FAILED';
    } else if (anyAvailable) {
      status = hasCurrent ? 'LOCKED' : 'CURRENT';
      hasCurrent = true;
    }
    stages.push({
      code: label.replace(/\s+/g, '_').toUpperCase(),
      label,
      purpose: '',
      status,
      tasks: stageTasks
    });
  }
  return stages;
}

export function toParticipationDecisionVm(response: GeneratedDecision): ParticipationDecisionVm {
  return {
    status: response.status,
    rootCause: response.rootCause,
    humanReason: response.humanReason,
    missingStatements: response.missingStatements ?? [],
    cascadedStatements: response.cascadedStatements ?? [],
    conditions: response.conditions ?? [],
    statusLabel: decisionStatusLabel(response.status),
    tone: decisionTone(response.status)
  };
}

export function toSubmitAnswerResultVm(response: GeneratedSubmitResponse): SubmitAnswerResultVm {
  return {
    resultType: response.resultType,
    reason: response.reason,
    missingPrerequisites: response.missingPrerequisites ?? []
  };
}

export { kindLabel, kindDescription };
