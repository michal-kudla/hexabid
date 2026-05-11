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
  ParticipationDecisionVm,
  SubmitAnswerResultVm,
  TaskSeverity
} from '../contracts/participation-api.models';

function programStatusLabel(status: StatementProgramViewStatusEnum): string {
  switch (status) {
    case StatementProgramViewStatusEnum.IN_PROGRESS: return 'Wymaga działania';
    case StatementProgramViewStatusEnum.COMPLETED: return 'Ukończony';
    case StatementProgramViewStatusEnum.REJECTED: return 'Odrzucony';
    case StatementProgramViewStatusEnum.CANCELLED: return 'Anulowany';
    default: return status;
  }
}

function programActionLabel(status: StatementProgramViewStatusEnum): string {
  switch (status) {
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

function taskSeverity(answerType: StatementStepViewAnswerTypeEnum): TaskSeverity {
  return 'BLOCKING';
}

export function toQualificationTaskVm(
  step: StatementStepView,
  status: QualificationTaskVm['status']
): QualificationTaskVm {
  const destructiveAnswers = isDestructiveAnswer(step.answerType, 'NO')
    ? [{ answerValue: 'NO', consequence: 'Ta odpowiedź spowoduje odmowę dopuszczenia do aukcji.' }]
    : [];

  return {
    code: step.statementCode,
    kind: 'STATEMENT',
    status,
    title: step.title,
    question: step.question,
    answerType: step.answerType,
    answerValue: step.answerValue,
    order: step.order,
    stepLabel: step.stepLabel,
    severity: taskSeverity(step.answerType),
    blockedBy: [],
    destructiveAnswers
  };
}

export function toQualificationProgramVm(response: StatementProgramView): QualificationProgramVm {
  const available = response.availableStatements.map(s => toQualificationTaskVm(s, 'AVAILABLE'));
  const completed = response.completedStatements.map(s => toQualificationTaskVm(s, 'COMPLETED'));
  const blocked = response.blockedStatements.map(s => toQualificationTaskVm(s, 'BLOCKED'));

  const tasks = [...available, ...completed, ...blocked].sort((a, b) => a.order - b.order);
  const totalCount = available.length + completed.length + blocked.length;
  const completedCount = completed.length;
  const progressPercent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

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
    tasks
  };
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
