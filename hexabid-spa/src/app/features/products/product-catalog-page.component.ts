import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductsApiService } from '../../data-access/http/products-api.service';
import { ProductTypeResponse } from '../../data-access/generated/auction-contract';

@Component({
  selector: 'app-product-catalog-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <h1>Katalog Produktów</h1>
        <p class="subtitle">Przeglądaj dostępne typy produktów na sprzedaż</p>
      </header>

      <div class="filters">
        <input
          type="text"
          placeholder="Szukaj produktu..."
          [(ngModel)]="searchQuery"
          (input)="onSearch()"
          class="search-input"
        />
        <select [(ngModel)]="selectedStrategy" (change)="onSearch()" class="strategy-select">
          <option value="">Wszystkie typy</option>
          <option value="UNIQUE">Unikalne</option>
          <option value="BATCH_TRACKED">Partiami</option>
          <option value="INDIVIDUALLY_TRACKED">Indywidualnie</option>
          <option value="IDENTICAL">Identyczne</option>
        </select>
      </div>

      @if (loading()) {
        <div class="loading">Ładowanie produktów...</div>
      } @else if (error()) {
        <div class="error">{{ error() }}</div>
      } @else if (products().length === 0) {
        <div class="empty-state">
          <p>Brak produktów w katalogu.</p>
          <p class="hint">Skontaktuj się z administratorem, aby dodać produkty.</p>
        </div>
      } @else {
        <div class="product-grid">
          @for (product of products(); track product.productId) {
            <a [routerLink]="['/products', product.productId]" class="product-card">
              <div class="product-icon">
                @switch (product.trackingStrategy) {
                  @case ('UNIQUE') { 🔑 }
                  @case ('BATCH_TRACKED') { 📦 }
                  @case ('INDIVIDUALLY_TRACKED') { 🏷️ }
                  @case ('IDENTICAL') { ⚖️ }
                  @default { 📋 }
                }
              </div>
              <h3 class="product-name">{{ product.name }}</h3>
              @if (product.description) {
                <p class="product-description">{{ product.description }}</p>
              }
              <div class="product-meta">
                <span class="strategy-badge" [class]="getStrategyClass(product.trackingStrategy)">
                  {{ getStrategyLabel(product.trackingStrategy) }}
                </span>
                <span class="unit">{{ product.preferredUnit }}</span>
              </div>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h1 {
      font-size: 2rem;
      font-weight: 700;
      color: var(--ink);
      margin: 0 0 0.5rem 0;
    }

    .subtitle {
      color: var(--ink-secondary);
      margin: 0;
    }

    .filters {
      display: flex;
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .search-input {
      flex: 1;
      padding: 10px 14px;
      min-height: 44px;
      border: 2px solid var(--border);
      border-radius: var(--radius-md);
      font-size: 1rem;
    }

    .strategy-select {
      padding: 10px 14px;
      min-height: 44px;
      border: 2px solid var(--border);
      border-radius: var(--radius-md);
      font-size: 1rem;
      background: var(--bg-surface);
    }

    .loading, .error, .empty-state {
      text-align: center;
      padding: 3rem;
      color: var(--ink-secondary);
    }

    .error {
      color: var(--danger);
    }

    .hint {
      font-size: 0.875rem;
      margin-top: 0.5rem;
    }

    .product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.5rem;
    }

    .product-card {
      display: block;
      padding: 1.5rem;
      border: 2px solid var(--border);
      border-radius: var(--radius-xl);
      text-decoration: none;
      transition: all 0.2s ease;
      background: var(--bg-surface);
    }

    .product-card:hover {
      border-color: var(--accent);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    .product-icon {
      font-size: 2rem;
      margin-bottom: 1rem;
    }

    .product-name {
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--ink);
      margin: 0 0 0.5rem 0;
    }

    .product-description {
      font-size: 0.875rem;
      color: var(--ink-secondary);
      margin: 0 0 1rem 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .product-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .strategy-badge {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-weight: 500;
    }

    .strategy-badge.unique {
      background: var(--color-amber-50);
      color: var(--accent-text);
    }

    .strategy-badge.batch {
      background: var(--accent-light);
      color: var(--accent-text);
    }

    .strategy-badge.individual {
      background: var(--success-bg);
      color: var(--success-text);
    }

    .strategy-badge.identical {
      background: var(--color-stone-100);
      color: var(--ink-secondary);
      border: 1px solid var(--border);
    }

    .unit {
      font-size: 0.875rem;
      color: var(--ink-secondary);
    }
  `]
})
export class ProductCatalogPageComponent {
  private readonly productsService = inject(ProductsApiService);

  products = signal<ProductTypeResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  searchQuery = '';
  selectedStrategy = '';

  constructor() {
    this.loadProducts();
  }

  private loadProducts() {
    this.loading.set(true);
    this.error.set(null);

    this.productsService.browseProductTypes({
      query: this.searchQuery || undefined,
      trackingStrategy: this.selectedStrategy || undefined,
      limit: 50
    }).then(response => {
      this.products.set(response.items || []);
    }).catch(err => {
      this.error.set(this.productsService.toMessage(err, 'Błąd pobierania produktów'));
    }).finally(() => {
      this.loading.set(false);
    });
  }

  onSearch() {
    this.loadProducts();
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
}