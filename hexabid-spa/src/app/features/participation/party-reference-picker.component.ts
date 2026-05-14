import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-party-reference-picker',
  imports: [CommonModule],
  templateUrl: './party-reference-picker.component.html',
  styleUrl: './party-reference-picker.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PartyReferencePickerComponent {
  readonly question = input.required<string>();
  readonly submitting = input(false);
  readonly answerSubmit = output<string>();

  readonly actingMode = input<'self' | 'representative'>('self');

  onSelectSelf(): void {
    this.answerSubmit.emit('SELF');
  }

  onSelectRepresentative(): void {
    this.answerSubmit.emit('REPRESENTATIVE');
  }
}
