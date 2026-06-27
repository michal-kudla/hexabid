import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DocumentType, DocumentStatus } from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-document-submit',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './document-submit.component.html',
  styleUrl: './document-submit.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocumentSubmitComponent {
  readonly submitting = input(false);
  readonly documentSubmitted = output<{ documentType: DocumentType; status: DocumentStatus }>();

  readonly DocumentType = DocumentType;
  readonly DocumentStatus = DocumentStatus;

  readonly form = new FormGroup({
    documentType: new FormControl<DocumentType>(DocumentType.EXCISE_CERTIFICATE, {
      nonNullable: true,
      validators: [Validators.required]
    }),
    status: new FormControl<DocumentStatus>(DocumentStatus.COPY, {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { documentType, status } = this.form.getRawValue();
    this.documentSubmitted.emit({ documentType, status });
  }
}
