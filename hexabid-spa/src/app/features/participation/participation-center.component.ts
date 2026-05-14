import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipationFacade } from './participation.facade';
import { QualificationTaskCardComponent } from './qualification-task-card.component';
import type { QualificationTaskVm, QualificationStageVm } from '../../data-access/contracts/participation-api.models';
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

  stages(): QualificationStageVm[] {
    return this.facade.program()?.stages ?? [];
  }

  stageStatusLabel(status: QualificationStageVm['status']): string {
    switch (status) {
      case 'LOCKED': return 'Zablokowany';
      case 'CURRENT': return 'Bieżący';
      case 'DONE': return 'Ukończony';
      case 'FAILED': return 'Nieudany';
    }
  }

  completedInStage(stage: QualificationStageVm): number {
    return stage.tasks.filter(t => t.status === 'COMPLETED').length;
  }
}
