import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { RulePhaseEvaluationVm } from '../../data-access/contracts/rules-api.models';

@Component({
  selector: 'app-rules-panel',
  imports: [CommonModule],
  templateUrl: './rules-panel.component.html',
  styleUrl: './rules-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RulesPanelComponent {
  readonly evaluations = input.required<RulePhaseEvaluationVm[]>();
}
