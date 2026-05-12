import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipationFacade } from './participation.facade';
import { QualificationTaskCardComponent } from './qualification-task-card.component';
import type { QualificationTaskVm } from '../../data-access/contracts/participation-api.models';
import type { QualificationSummaryVm } from '../../data-access/contracts/auction-api.models';

@Component({
  selector: 'app-participation-center',
  imports: [CommonModule, QualificationTaskCardComponent],
  templateUrl: './participation-center.component.html',
  styleUrl: './participation-center.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ParticipationFacade]
})
export class ParticipationCenterComponent {
  readonly facade = inject(ParticipationFacade);
  readonly auctionId = input.required<string>();
  readonly qualificationSummary = input<QualificationSummaryVm | null | undefined>(null);

  onAnswerSubmit(event: { code: string; value: string }): void {
    void this.facade.submitAnswer(event.code, event.value);
  }

  onStartProgram(): void {
    void this.facade.startProgram(this.auctionId());
  }

  onRefreshProgram(): void {
    void this.facade.loadProgram(this.auctionId());
  }

  onRefreshDecision(): void {
    void this.facade.refreshDecision();
  }

  tasksByStep(tasks: QualificationTaskVm[]): Map<string, QualificationTaskVm[]> {
    const map = new Map<string, QualificationTaskVm[]>();
    for (const task of tasks) {
      const step = task.stepLabel ?? 'Dopuszczenie';
      if (!map.has(step)) {
        map.set(step, []);
      }
      map.get(step)!.push(task);
    }
    return map;
  }

  profileLabel(): string {
    const summary = this.qualificationSummary();
    return summary?.templateLabel ?? 'kwalifikacja';
  }

  taskCountLabel(): string {
    const summary = this.qualificationSummary();
    return summary?.taskCount ? `${summary.taskCount}` : '';
  }
}
