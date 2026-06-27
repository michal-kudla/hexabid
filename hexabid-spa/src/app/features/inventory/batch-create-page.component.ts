import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventoryApiService } from '../../data-access/http/inventory-api.service';
import { ProductsApiService } from '../../data-access/http/products-api.service';
import { CreateBatchRequest, ProductTypeResponse } from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-batch-create-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-container">
      <nav class="breadcrumb">
        <a routerLink="/products">← Katalog produktów</a>
      </nav>

      <header class="page-header">
        <h1>Utwórz partię</h1>
        <p class="subtitle">Dodaj nową partię produkcyjną</p>
      </header>

      <form (ngSubmit)="createBatch()" class="form">
        <div class="form-group">
          <label for="productId">Produkt</label>
          <select id="productId" [(ngModel)]="batchData.productId" name="productId" required>
            <option value="">Wybierz produkt...</option>
            @for (product of products(); track product.productId) {
              <option [value]="product.productId">{{ product.name }}</option>
            }
          </select>
        </div>

        <div class="form-group">
          <label for="name">Nazwa partii</label>
          <input
            type="text"
            id="name"
            [(ngModel)]="batchData.name"
            name="name"
            placeholder="np. KUKURYDZA-2026-POLAND-001"
            required
          />
          <span class="hint">Unikalna nazwa identyfikująca partię</span>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="quantity">Ilość</label>
            <input
              type="number"
              id="quantity"
              [(ngModel)]="batchData.quantityAmount"
              name="quantityAmount"
              min="0"
              step="0.01"
              required
            />
          </div>

          <div class="form-group">
            <label for="unit">Jednostka</label>
            <select id="unit" [(ngModel)]="batchData.quantityUnit" name="quantityUnit" required>
              <option value="kg">kg</option>
              <option value="t">t</option>
              <option value="pcs">szt.</option>
              <option value="l">l</option>
              <option value="m">m</option>
              <option value="m2">m²</option>
              <option value="m3">m³</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="dateProduced">Data produkcji (opcjonalnie)</label>
            <input
              type="date"
              id="dateProduced"
              [(ngModel)]="batchData.dateProduced"
              name="dateProduced"
            />
          </div>

          <div class="form-group">
            <label for="bestBefore">Najlepiej spożyć przed (opcjonalnie)</label>
            <input
              type="date"
              id="bestBefore"
              [(ngModel)]="batchData.bestBefore"
              name="bestBefore"
            />
          </div>
        </div>

        @if (error()) {
          <div class="error-message">{{ error() }}</div>
        }

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" routerLink="/products">
            Anuluj
          </button>
          <button type="submit" class="btn btn-primary" [disabled]="saving()">
            {{ saving() ? 'Tworzenie...' : 'Utwórz partię' }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .page-container {
      max-width: 600px;
      margin: 0 auto;
      padding: 2rem;
    }

    .breadcrumb {
      margin-bottom: 2rem;
    }

    .breadcrumb a {
      color: var(--accent);
      text-decoration: none;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h1 {
      font-size: 1.75rem;
      font-weight: 700;
      margin: 0 0 0.5rem 0;
    }

    .subtitle {
      color: var(--ink-secondary);
      margin: 0;
    }

    .form {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .form-group label {
      font-weight: 500;
      color: var(--ink);
    }

    .form-group input,
    .form-group select {
      padding: 10px 14px;
      min-height: 44px;
      border: 2px solid var(--border);
      border-radius: var(--radius-md);
      font-size: 1rem;
    }

    .form-group input:focus,
    .form-group select:focus {
      outline: none;
      border-color: var(--accent);
    }

    .hint {
      font-size: 0.75rem;
      color: var(--ink-secondary);
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    .error-message {
      padding: 1rem;
      background: var(--danger-bg);
      border: 2px solid var(--danger);
      border-radius: var(--radius-md);
      color: var(--danger-text);
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1rem;
    }

    .btn {
      padding: 10px 22px;
      min-height: 44px;
      border-radius: var(--radius-md);
      font-weight: 500;
      cursor: pointer;
      border: none;
    }

    .btn-secondary {
      background: var(--bg-surface);
      color: var(--ink);
      border: 2px solid var(--border-strong);
    }

    .btn-primary {
      background: var(--accent);
      color: white;
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  `]
})
export class BatchCreatePageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly inventoryService = inject(InventoryApiService);
  private readonly productsService = inject(ProductsApiService);

  products = signal<ProductTypeResponse[]>([]);
  saving = signal(false);
  error = signal<string | null>(null);

  batchData = {
    productId: '',
    name: '',
    quantityAmount: '',
    quantityUnit: 'kg',
    dateProduced: '',
    bestBefore: ''
  };

  ngOnInit() {
    this.loadProducts();
    
    const productId = this.route.snapshot.queryParamMap.get('productId');
    if (productId) {
      this.batchData.productId = productId;
    }
  }

  private loadProducts() {
    this.productsService.browseProductTypes({ limit: 100 })
      .then(response => this.products.set(response.items || []))
      .catch(() => {});
  }

  async createBatch() {
    if (!this.batchData.productId || !this.batchData.name || !this.batchData.quantityAmount) {
      this.error.set('Wypełnij wszystkie wymagane pola');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request: CreateBatchRequest = {
      productId: this.batchData.productId,
      name: this.batchData.name,
      quantity: {
        amount: this.batchData.quantityAmount,
        unit: this.batchData.quantityUnit
      },
      dateProduced: this.batchData.dateProduced ? new Date(this.batchData.dateProduced).toISOString() : undefined,
      bestBefore: this.batchData.bestBefore ? new Date(this.batchData.bestBefore).toISOString() : undefined
    };

    try {
      await this.inventoryService.createBatch(request);
      this.router.navigate(['/products', this.batchData.productId]);
    } catch (err) {
      this.error.set(this.inventoryService.toMessage(err, 'Nie udało się utworzyć partii'));
    } finally {
      this.saving.set(false);
    }
  }
}