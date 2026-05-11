import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipationFacade } from './participation.facade';
import { QualificationTaskCardComponent } from './qualification-task-card.component';
import type { QualificationTaskVm } from '../../data-access/contracts/participation-api.models';

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

  onAnswerSubmit(event: { code: string; value: string }): void {
    void this.facade.submitAnswer(event.code, event.value);
  }

  onStartProgram(): void {
    void this.facade.startProgram(this.auctionId(), 'PUBLIC_CONSUMER_LIGHT_V1');
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
}
