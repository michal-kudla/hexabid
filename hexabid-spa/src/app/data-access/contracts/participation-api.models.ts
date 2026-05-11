import type {
  ParticipationDecisionViewStatusEnum,
  StatementProgramViewStatusEnum,
  StatementStepViewAnswerTypeEnum,
  SubmitStatementAnswerResponseResultTypeEnum
} from '../generated/auction-contract';

export {
  ParticipationDecisionViewStatusEnum as ParticipationStatus,
  StatementProgramViewStatusEnum as ProgramStatus,
  StatementStepViewAnswerTypeEnum as AnswerType,
  SubmitStatementAnswerResponseResultTypeEnum as AnswerResult
} from '../generated/auction-contract';

export type QualificationTaskKind =
  | 'STATEMENT'
  | 'VERIFIED_FACT'
  | 'EVIDENCE'
  | 'EXTERNAL_CHECK'
  | 'PARTY_REFERENCE';

export type QualificationTaskStatus =
  | 'AVAILABLE'
  | 'COMPLETED'
  | 'BLOCKED'
  | 'PENDING_REVIEW'
  | 'FAILED'
  | 'SKIPPED_BY_DECISION';

export type TaskSeverity = 'BLOCKING' | 'IMPORTANT' | 'INFO';

export interface DestructiveAnswerVm {
  answerValue: string;
  consequence: string;
}

export interface QualificationTaskVm {
  code: string;
  kind: QualificationTaskKind;
  status: QualificationTaskStatus;
  title: string;
  question: string;
  answerType: StatementStepViewAnswerTypeEnum;
  answerValue?: string;
  order: number;
  stepLabel?: string;
  severity: TaskSeverity;
  blockedBy: string[];
  destructiveAnswers: DestructiveAnswerVm[];
}

export interface QualificationProgramVm {
  auctionId: string;
  programInstanceId: string;
  templateName: string;
  status: StatementProgramViewStatusEnum;
  headline: string;
  userActionLabel: string;
  completedCount: number;
  totalCount: number;
  progressPercent: number;
  tasks: QualificationTaskVm[];
}

export interface ParticipationDecisionVm {
  status: ParticipationDecisionViewStatusEnum;
  rootCause?: string;
  humanReason?: string;
  missingStatements: string[];
  cascadedStatements: string[];
  conditions: string[];
  statusLabel: string;
  tone: 'success' | 'warning' | 'error' | 'info';
}

export interface SubmitAnswerResultVm {
  resultType: SubmitStatementAnswerResponseResultTypeEnum;
  reason?: string;
  missingPrerequisites: string[];
}
