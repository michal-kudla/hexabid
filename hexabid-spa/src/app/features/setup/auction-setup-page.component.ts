import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import { WadiumStrategy, PricingConfigExciseTypeEnum } from '../../data-access/generated/auction-contract';
import type { CreateAuctionRequest, PricingConfig, Money } from '../../data-access/generated/auction-contract';
import {
  AuctionCategory,
  Jurisdiction,
  QualificationProfileEntry,
  categoryLabel,
  jurisdictionLabel,
  riskLabel,
  profilesForCategory,
  recommendedProfile,
  QUALIFICATION_PROFILE_CATALOG
} from '../../data-access/contracts/qualification-profile.models';

type SetupStep = 'subject' | 'qualification' | 'pricing' | 'review';

@Component({
  selector: 'app-auction-setup-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auction-setup-page.component.html',
  styleUrl: './auction-setup-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuctionSetupPageComponent {
  private readonly router = inject(Router);
  private readonly api = inject(AuctionsApiService);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly steps: SetupStep[] = ['subject', 'qualification', 'pricing', 'review'];
  readonly currentStep = signal<SetupStep>('subject');

  readonly categoryLabel = categoryLabel;
  readonly jurisdictionLabel = jurisdictionLabel;
  readonly riskLabel = riskLabel;
  readonly WadiumStrategy = WadiumStrategy;
  readonly ExciseType = PricingConfigExciseTypeEnum;

  readonly subjectForm = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(4)] }),
    category: new FormControl<AuctionCategory>('GENERAL', { nonNullable: true }),
    jurisdiction: new FormControl<Jurisdiction>('PL', { nonNullable: true }),
    amount: new FormControl('100.00', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] }),
    endsAt: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  readonly selectedProfile = signal<QualificationProfileEntry>(QUALIFICATION_PROFILE_CATALOG[0]);

  readonly showPricing = signal(false);

  readonly pricingForm = new FormGroup({
    wadiumStrategy: new FormControl<WadiumStrategy | ''>('', { nonNullable: true }),
    wadiumRate: new FormControl('', { nonNullable: true }),
    wadiumFixedAmount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    vatRate: new FormControl('0.23', { nonNullable: true, validators: [Validators.required] }),
    isExcisable: new FormControl(false, { nonNullable: true }),
    exciseRate: new FormControl('', { nonNullable: true }),
    exciseType: new FormControl<PricingConfigExciseTypeEnum>(PricingConfigExciseTypeEnum.PERCENTAGE, { nonNullable: true }),
    isImported: new FormControl(false, { nonNullable: true }),
    customsDutyRate: new FormControl('', { nonNullable: true })
  });

  stepIndex(): number {
    return this.steps.indexOf(this.currentStep());
  }

  stepLabel(step: SetupStep): string {
    switch (step) {
      case 'subject': return 'Przedmiot i kategoria';
      case 'qualification': return 'Kwalifikacja licytantów';
      case 'pricing': return 'Cena i zabezpieczenia';
      case 'review': return 'Podsumowanie';
    }
  }

  isFirstStep(): boolean {
    return this.currentStep() === 'subject';
  }

  isLastStep(): boolean {
    return this.currentStep() === 'review';
  }

  goToStep(step: SetupStep): void {
    this.currentStep.set(step);
  }

  nextStep(): void {
    const idx = this.stepIndex();
    if (idx < this.steps.length - 1) {
      this.currentStep.set(this.steps[idx + 1]);
    }
  }

  prevStep(): void {
    const idx = this.stepIndex();
    if (idx > 0) {
      this.currentStep.set(this.steps[idx - 1]);
    }
  }

  onCategoryChange(): void {
    const category = this.subjectForm.value.category;
    if (category) {
      this.selectedProfile.set(recommendedProfile(category));
    }
  }

  selectProfile(profile: QualificationProfileEntry): void {
    this.selectedProfile.set(profile);
  }

  availableProfiles(): QualificationProfileEntry[] {
    const category = this.subjectForm.value.category ?? 'GENERAL';
    return profilesForCategory(category);
  }

  togglePricing(): void {
    this.showPricing.update(v => !v);
  }

  async submit(): Promise<void> {
    this.submitting.set(true);
    this.error.set(null);

    try {
      const sv = this.subjectForm.getRawValue();

      const request: CreateAuctionRequest = {
        title: sv.title,
        startingPrice: {
          amount: sv.amount,
          currency: sv.currency
        } as Money,
        endsAt: new Date(sv.endsAt).toISOString()
      };

      if (this.showPricing()) {
        const pv = this.pricingForm.getRawValue();
        const pricingConfig: PricingConfig = {
          vatRate: pv.vatRate,
          isExcisable: pv.isExcisable,
          isImported: pv.isImported
        };

        if (pv.wadiumStrategy === WadiumStrategy.PERCENTAGE && pv.wadiumRate) {
          pricingConfig.wadiumStrategy = pv.wadiumStrategy;
          pricingConfig.wadiumRate = pv.wadiumRate;
        } else if (pv.wadiumStrategy === WadiumStrategy.FIXED && pv.wadiumFixedAmount) {
          pricingConfig.wadiumStrategy = pv.wadiumStrategy;
          pricingConfig.wadiumFixedAmount = { amount: pv.wadiumFixedAmount, currency: sv.currency } as Money;
        }

        if (pv.isExcisable && pv.exciseRate) {
          pricingConfig.exciseRate = pv.exciseRate;
          pricingConfig.exciseType = pv.exciseType;
        }

        if (pv.isImported && pv.customsDutyRate) {
          pricingConfig.customsDutyRate = pv.customsDutyRate;
        }

        request.pricingConfig = pricingConfig;
      }

      const created = await this.api.createAuction(request);
      await this.router.navigate(['/auction', created.auctionId]);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się utworzyć aukcji.'));
    } finally {
      this.submitting.set(false);
    }
  }
}
