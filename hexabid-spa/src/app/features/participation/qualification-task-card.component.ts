import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { QualificationTaskVm } from '../../data-access/contracts/participation-api.models';

@Component({
  selector: 'app-qualification-task-card',
  imports: [CommonModule],
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
}
