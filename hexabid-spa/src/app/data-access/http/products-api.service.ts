import { Injectable } from '@angular/core';
import {
  ProductTypeListResponse,
  ProductTypeResponse,
  ProductsApi,
  Configuration,
  CreateProductTypeRequest,
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class ProductsApiService {
  private readonly client = new ProductsApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async browseProductTypes(options: {
    query?: string;
    trackingStrategy?: string;
    limit?: number;
    after?: string | null;
  }): Promise<ProductTypeListResponse> {
    return this.execute(
      () =>
        this.client.browseProductTypes({
          query: options.query,
          trackingStrategy: options.trackingStrategy as any,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać katalogu produktów.'
    );
  }

  async getProductType(productId: string): Promise<ProductTypeResponse> {
    return this.execute(
      () => this.client.getProductType({ productId }),
      'Nie udało się pobrać szczegółów produktu.'
    );
  }

  async createProductType(request: CreateProductTypeRequest): Promise<ProductTypeResponse> {
    return this.execute(
      () => this.client.createProductType({ createProductTypeRequest: request }),
      'Nie udało się utworzyć typu produktu.'
    );
  }

  private async execute<T>(operation: () => Promise<T>, fallback: string): Promise<T> {
    try {
      return await operation();
    } catch (error) {
      throw this.normalizeError(error, fallback);
    }
  }

  private normalizeError(error: unknown, fallback: string): Error {
    return error instanceof Error ? error : new Error(fallback);
  }

  toMessage(error: unknown, fallback: string): string {
    return error instanceof Error ? error.message : fallback;
  }
}