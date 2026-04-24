import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductsApiService } from '../../data-access/http/products-api.service';
import { InventoryApiService } from '../../data-access/http/inventory-api.service';
import { ProductTypeResponse, BatchResponse } from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-product-details-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container">
      <nav class="breadcrumb">
        <a routerLink="/products">← Katalog produktów</a>
      </nav>

      @if (loading()) {
        <div class="loading">Ładowanie produktu...</div>
      } @else if (error()) {
        <div class="error">{{ error() }}</div>
      } @else if (product()) {
        <div class="product-details">
          <header class="product-header">
            <div class="product-icon">
              @switch (product()!.trackingStrategy) {
                @case ('UNIQUE') { 🔑 }
                @case ('BATCH_TRACKED') { 📦 }
                @case ('INDIVIDUALLY_TRACKED') { 🏷️ }
                @case ('IDENTICAL') { ⚖️ }
                @default { 📋 }
              }
            </div>
            <div class="product-info">
              <h1>{{ product()!.name }}</h1>
              <span class="strategy-badge" [class]="getStrategyClass(product()!.trackingStrategy)">
                {{ getStrategyLabel(product()!.trackingStrategy) }}
              </span>
            </div>
          </header>

          @if (product()!.description) {
            <section class="section">
              <h2>Opis</h2>
              <p>{{ product()!.description }}</p>
            </section>
          }

          <section class="section">
            <h2>Informacje</h2>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">Preferred Unit</span>
                <span class="value">{{ product()!.preferredUnit }}</span>
              </div>
              <div class="info-item">
                <span class="label">Tracking Strategy</span>
                <span class="value">{{ product()!.trackingStrategy }}</span>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <h2>Partie tego produktu</h2>
              <a routerLink="/inventory/batches/new" [queryParams]="{productId: product()!.productId}" class="btn btn-primary">
                Utwórz partię
              </a>
            </div>

            @if (batchesLoading()) {
              <p class="loading-text">Ładowanie partii...</p>
            } @else if (batches().length === 0) {
              <p class="empty-text">Brak partii dla tego produktu.</p>
            } @else {
              <div class="batch-list">
                @for (batch of batches(); track batch.batchId) {
                  <div class="batch-card">
                    <h3>{{ batch.name }}</h3>
                    <p class="batch-quantity">{{ batch.quantity?.amount }} {{ batch.quantity?.unit }}</p>
                    @if (batch.dateProduced) {
                      <p class="batch-date">Wyprodukowano: {{ formatDate(batch.dateProduced) }}</p>
                    }
                  </div>
                }
              </div>
            }
          </section>
        </div>
      } @else {
        <div class="not-found">Produkt nie znaleziony.</div>
      }
    </div>
  `,
  styles: [`
    .page-container {
      max-width: 900px;
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

    .loading, .error, .not-found {
      text-align: center;
      padding: 3rem;
      color: var(--ink-secondary);
    }

    .error {
      color: var(--danger);
    }

    .product-header {
      display: flex;
      align-items: center;
      gap: 1.5rem;
      margin-bottom: 2rem;
      padding-bottom: 2rem;
      border-bottom: 2px solid var(--border);
    }

    .product-icon {
      font-size: 3rem;
    }

    .product-info h1 {
      margin: 0 0 0.5rem 0;
      font-size: 2rem;
      color: var(--ink);
    }

    .strategy-badge {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-weight: 500;
    }

    .strategy-badge.unique { background: var(--color-amber-50); color: var(--accent-text); }
    .strategy-badge.batch { background: var(--accent-light); color: var(--accent-text); }
    .strategy-badge.individual { background: var(--success-bg); color: var(--success-text); }
    .strategy-badge.identical { background: var(--color-stone-100); color: var(--ink-secondary); border: 1px solid var(--border); }

    .section {
      margin-bottom: 2rem;
    }

    .section h2 {
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0 0 1rem 0;
      color: var(--ink);
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .section-header h2 {
      margin: 0;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .info-item .label {
      font-size: 0.875rem;
      color: var(--ink-secondary);
    }

    .info-item .value {
      font-weight: 500;
      color: var(--ink);
    }

    .btn {
      padding: 10px 22px;
      min-height: 44px;
      border-radius: var(--radius-md);
      text-decoration: none;
      font-weight: 500;
      cursor: pointer;
    }

    .btn-primary {
      background: var(--accent);
      color: white;
      border: none;
    }

    .batch-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .batch-card {
      padding: 1rem;
      border: 2px solid var(--border);
      border-radius: var(--radius-md);
      background: var(--bg-surface);
    }

    .batch-card h3 {
      margin: 0 0 0.5rem 0;
      font-size: 1rem;
      color: var(--ink);
    }

    .batch-quantity {
      margin: 0;
      color: var(--ink-secondary);
      font-size: 0.875rem;
    }

    .batch-date {
      margin: 0.5rem 0 0 0;
      font-size: 0.75rem;
      color: var(--ink-secondary);
    }

    .loading-text, .empty-text {
      color: var(--ink-secondary);
      font-size: 0.875rem;
    }
  `]
})
export class ProductDetailsPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly productsService = inject(ProductsApiService);
  private readonly inventoryService = inject(InventoryApiService);

  product = signal<ProductTypeResponse | null>(null);
  batches = signal<BatchResponse[]>([]);
  loading = signal(false);
  batchesLoading = signal(false);
  error = signal<string | null>(null);

  ngOnInit() {
    const productId = this.route.snapshot.paramMap.get('productId');
    if (productId) {
      this.loadProduct(productId);
      this.loadBatches(productId);
    }
  }

  private loadProduct(productId: string) {
    this.loading.set(true);
    this.productsService.getProductType(productId)
      .then(response => this.product.set(response))
      .catch(err => this.error.set(this.productsService.toMessage(err, 'Błąd pobierania produktu')))
      .finally(() => this.loading.set(false));
  }

  private loadBatches(productId: string) {
    this.batchesLoading.set(true);
    this.inventoryService.browseBatches({ productId, limit: 50 })
      .then(response => this.batches.set(response.items || []))
      .finally(() => this.batchesLoading.set(false));
  }

  getStrategyLabel(strategy: string | undefined): string {
    switch (strategy) {
      case 'UNIQUE': return 'Unikalny';
      case 'BATCH_TRACKED': return 'Partiami';
      case 'INDIVIDUALLY_TRACKED': return 'Indywidualnie';
      case 'IDENTICAL': return 'Identyczny';
      default: return strategy || 'N/A';
    }
  }

  getStrategyClass(strategy: string | undefined): string {
    switch (strategy) {
      case 'UNIQUE': return 'unique';
      case 'BATCH_TRACKED': return 'batch';
      case 'INDIVIDUALLY_TRACKED': return 'individual';
      case 'IDENTICAL': return 'identical';
      default: return '';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('pl-PL');
  }
}