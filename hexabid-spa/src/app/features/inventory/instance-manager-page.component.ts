import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventoryApiService } from '../../data-access/http/inventory-api.service';
import { ProductsApiService } from '../../data-access/http/products-api.service';
import { 
  InventoryInstanceResponse, 
  BatchResponse,
  CreateInventoryInstanceRequest,
  ProductTypeResponse 
} from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-instance-manager-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <h1>Zarządzanie instancjami</h1>
        <p class="subtitle">Przeglądaj i twórz instancje inventory</p>
      </header>

      <div class="filters">
        <select [(ngModel)]="selectedBatchId" (change)="loadInstances()" class="filter-select">
          <option value="">Wszystkie partie</option>
          @for (batch of batches(); track batch.batchId) {
            <option [value]="batch.batchId">{{ batch.name }}</option>
          }
        </select>
        <button class="btn btn-primary" (click)="showCreateForm = true">
          Dodaj instancję
        </button>
      </div>

      @if (loading()) {
        <div class="loading">Ładowanie...</div>
      } @else if (instances().length === 0) {
        <div class="empty-state">
          <p>Brak instancji do wyświetlenia.</p>
          <p class="hint">Utwórz partię, a następnie dodaj instancje.</p>
        </div>
      } @else {
        <div class="instance-list">
          @for (instance of instances(); track instance.instanceId) {
            <div class="instance-card">
              <div class="instance-info">
                <h3>{{ instance.instanceId }}</h3>
                <p>Produkt: {{ instance.productId }}</p>
                @if (instance.batchId) {
                  <p>Partia: {{ instance.batchId }}</p>
                }
                @if (instance.serialNumber) {
                  <p>Numer seryjny: {{ instance.serialNumber }}</p>
                }
              </div>
              <div class="instance-quantity">
                @if (instance.quantity) {
                  <span class="qty">{{ instance.quantity.amount }} {{ instance.quantity.unit }}</span>
                }
              </div>
            </div>
          }
        </div>
      }

      @if (showCreateForm) {
        <div class="modal-overlay" (click)="showCreateForm = false">
          <div class="modal" (click)="$event.stopPropagation()">
            <h2>Nowa instancja</h2>
            <form (ngSubmit)="createInstance()">
              <div class="form-group">
                <label>Produkt</label>
                <select [(ngModel)]="newInstance.productId" name="productId" required>
                  <option value="">Wybierz...</option>
                  @for (p of products(); track p.productId) {
                    <option [value]="p.productId">{{ p.name }}</option>
                  }
                </select>
              </div>
              <div class="form-group">
                <label>Partia (opcjonalnie)</label>
                <select [(ngModel)]="newInstance.batchId" name="batchId">
                  <option value="">Brak</option>
                  @for (b of batches(); track b.batchId) {
                    <option [value]="b.batchId">{{ b.name }}</option>
                  }
                </select>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Ilość</label>
                  <input type="number" [(ngModel)]="newInstance.amount" name="amount" required />
                </div>
                <div class="form-group">
                  <label>Jednostka</label>
                  <select [(ngModel)]="newInstance.unit" name="unit">
                    <option value="kg">kg</option>
                    <option value="pcs">szt.</option>
                    <option value="l">l</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label>Numer seryjny (opcjonalnie)</label>
                <input type="text" [(ngModel)]="newInstance.serialNumber" name="serialNumber" />
              </div>
              @if (error()) {
                <div class="error">{{ error() }}</div>
              }
              <div class="modal-actions">
                <button type="button" class="btn btn-secondary" (click)="showCreateForm = false">Anuluj</button>
                <button type="submit" class="btn btn-primary" [disabled]="saving()">
                  {{ saving() ? 'Tworzenie...' : 'Utwórz' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { max-width: 900px; margin: 0 auto; padding: 2rem; }
    .page-header { margin-bottom: 2rem; }
    .page-header h1 { font-size: 1.75rem; font-weight: 700; margin: 0 0 0.5rem 0; }
    .subtitle { color: var(--color-text-secondary); margin: 0; }
    .filters { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; gap: 1rem; }
    .filter-select { padding: 0.5rem 1rem; border: 1px solid var(--color-border); border-radius: 8px; flex: 1; }
    .loading, .empty-state { text-align: center; padding: 3rem; color: var(--color-text-secondary); }
    .hint { font-size: 0.875rem; margin-top: 0.5rem; }
    .instance-list { display: flex; flex-direction: column; gap: 1rem; }
    .instance-card { display: flex; justify-content: space-between; padding: 1rem; border: 1px solid var(--color-border); border-radius: 8px; background: white; }
    .instance-info h3 { margin: 0 0 0.5rem 0; font-size: 0.875rem; color: var(--color-text-secondary); }
    .instance-info p { margin: 0.25rem 0; font-size: 0.875rem; }
    .instance-quantity .qty { font-weight: 600; color: var(--color-primary); }
    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
    .modal { background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 500px; }
    .modal h2 { margin: 0 0 1.5rem 0; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; font-weight: 500; margin-bottom: 0.5rem; }
    .form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid var(--color-border); border-radius: 6px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .error { padding: 0.75rem; background: #fef2f2; color: #dc2626; border-radius: 6px; margin-bottom: 1rem; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
    .btn { padding: 0.5rem 1rem; border-radius: 6px; font-weight: 500; cursor: pointer; border: none; }
    .btn-secondary { background: #f3f4f6; color: #374151; }
    .btn-primary { background: var(--color-primary); color: white; }
    .btn:disabled { opacity: 0.6; }
  `]
})
export class InstanceManagerPageComponent implements OnInit {
  private readonly inventoryService = inject(InventoryApiService);
  private readonly productsService = inject(ProductsApiService);

  instances = signal<InventoryInstanceResponse[]>([]);
  batches = signal<BatchResponse[]>([]);
  products = signal<ProductTypeResponse[]>([]);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);
  
  selectedBatchId = '';
  showCreateForm = false;
  
  newInstance = { productId: '', batchId: '', amount: '', unit: 'kg', serialNumber: '' };

  ngOnInit() {
    this.loadBatches();
    this.loadProducts();
    this.loadInstances();
  }

  private loadBatches() {
    this.inventoryService.browseBatches({ limit: 100 })
      .then(r => this.batches.set(r.items || []))
      .catch(() => {});
  }

  private loadProducts() {
    this.productsService.browseProductTypes({ limit: 100 })
      .then(r => this.products.set(r.items || []))
      .catch(() => {});
  }

  loadInstances() {
    this.loading.set(true);
    this.inventoryService.browseInventoryInstances({ 
      batchId: this.selectedBatchId || undefined,
      limit: 100 
    }).then(r => this.instances.set(r.items || []))
      .catch(() => {})
      .finally(() => this.loading.set(false));
  }

  async createInstance() {
    if (!this.newInstance.productId || !this.newInstance.amount) {
      this.error.set('Wypełnij wymagane pola');
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    
    const request: CreateInventoryInstanceRequest = {
      productId: this.newInstance.productId,
      batchId: this.newInstance.batchId || undefined,
      quantity: { amount: this.newInstance.amount, unit: this.newInstance.unit },
      serialNumber: this.newInstance.serialNumber || undefined
    };
    
    try {
      await this.inventoryService.createInventoryInstance(request);
      this.showCreateForm = false;
      this.newInstance = { productId: '', batchId: '', amount: '', unit: 'kg', serialNumber: '' };
      this.loadInstances();
    } catch (err) {
      this.error.set(this.inventoryService.toMessage(err, 'Błąd tworzenia instancji'));
    } finally {
      this.saving.set(false);
    }
  }
}