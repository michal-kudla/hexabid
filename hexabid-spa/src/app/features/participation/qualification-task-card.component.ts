import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { QualificationTaskVm, QualificationTaskKind } from '../../data-access/contracts/participation-api.models';
import { kindLabel, kindDescription } from '../../data-access/mappers/participation-view.mapper';
import { PartyReferencePickerComponent } from './party-reference-picker.component';

@Component({
  selector: 'app-qualification-task-card',
  imports: [CommonModule, PartyReferencePickerComponent],
  templateUrl: './qualification-task-card.component.html',
  styleUrl: './qualification-task-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QualificationTaskCardComponent {
  readonly task = input.required<QualificationTaskVm>();
  readonly submitting = input(false);
  readonly answerSubmit = output<{ code: string; value: string }>();

  selectedAnswer: string | null = null;
  confirmingDestructive = false;

  kindLabel = kindLabel;
  kindDescription = kindDescription;

  onYesNoSelect(value: string): void {
    const task = this.task();
    const isDestructive = task.destructiveAnswers.some(d => d.answerValue === value);
    if (isDestructive) {
      this.selectedAnswer = value;
      this.confirmingDestructive = true;
    } else {
      this.answerSubmit.emit({ code: task.code, value });
    }
  }

  confirmDestructive(): void {
    if (this.selectedAnswer) {
      this.answerSubmit.emit({ code: this.task().code, value: this.selectedAnswer });
    }
    this.confirmingDestructive = false;
    this.selectedAnswer = null;
  }

  cancelDestructive(): void {
    this.confirmingDestructive = false;
    this.selectedAnswer = null;
  }

  onTextSubmit(inputEl: HTMLInputElement): void {
    const value = inputEl.value.trim();
    if (value) {
      this.answerSubmit.emit({ code: this.task().code, value });
      inputEl.value = '';
    }
  }

  statusIcon(): string {
    switch (this.task().status) {
      case 'COMPLETED': return '\u2713';
      case 'BLOCKED': return '\u23F3';
      case 'AVAILABLE': return '\u25CB';
      default: return '\u25CB';
    }
  }

  statusClass(): string {
    switch (this.task().status) {
      case 'COMPLETED': return 'task-completed';
      case 'BLOCKED': return 'task-blocked';
      case 'AVAILABLE': return 'task-available';
      default: return 'task-available';
    }
  }

  kindIcon(): string {
    switch (this.task().kind) {
      case 'STATEMENT': return '\u270D';
      case 'VERIFIED_FACT': return '\u2705';
      case 'EVIDENCE': return '\uD83D\uDCC4';
      case 'EXTERNAL_CHECK': return '\uD83D\uDD0D';
      case 'PARTY_REFERENCE': return '\uD83D\uDC64';
      default: return '\u270D';
    }
  }

  kindClass(): string {
    switch (this.task().kind) {
      case 'VERIFIED_FACT': return 'kind-verified-fact';
      case 'EVIDENCE': return 'kind-evidence';
      case 'EXTERNAL_CHECK': return 'kind-external-check';
      case 'PARTY_REFERENCE': return 'kind-party-reference';
      default: return 'kind-statement';
    }
  }

  onPartyAnswer(partyAnswer: string): void {
    this.answerSubmit.emit({ code: this.task().code, value: partyAnswer });
  }

  actionLabel(): string {
    switch (this.task().kind) {
      case 'VERIFIED_FACT': return 'Potwierdź';
      case 'EVIDENCE': return 'Dołącz dokument';
      case 'EXTERNAL_CHECK': return 'Zgódź się na weryfikację';
      case 'PARTY_REFERENCE': return 'Wskaż podmiot';
      default: return 'Odpowiedz';
    }
  }
}
