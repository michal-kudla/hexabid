import { Injectable } from '@angular/core';
import {
  BatchListResponse,
  BatchResponse,
  CreateBatchRequest,
  InventoryInstanceListResponse,
  InventoryInstanceResponse,
  CreateInventoryInstanceRequest,
  InventoryApi,
  Configuration,
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private readonly client = new InventoryApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async browseBatches(options: {
    productId?: string;
    query?: string;
    limit?: number;
    after?: string | null;
  }): Promise<BatchListResponse> {
    return this.execute(
      () =>
        this.client.browseBatches({
          productId: options.productId,
          query: options.query,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać listy partii.'
    );
  }

  async getBatch(batchId: string): Promise<BatchResponse> {
    return this.execute(
      () => this.client.getBatch({ batchId }),
      'Nie udało się pobrać szczegółów partii.'
    );
  }

  async createBatch(request: CreateBatchRequest): Promise<BatchResponse> {
    return this.execute(
      () => this.client.createBatch({ createBatchRequest: request }),
      'Nie udało się utworzyć partii.'
    );
  }

  async browseInventoryInstances(options: {
    batchId?: string;
    productId?: string;
    limit?: number;
    after?: string | null;
  }): Promise<InventoryInstanceListResponse> {
    return this.execute(
      () =>
        this.client.browseInventoryInstances({
          batchId: options.batchId,
          productId: options.productId,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać listy instancji.'
    );
  }

  async getInventoryInstance(instanceId: string): Promise<InventoryInstanceResponse> {
    return this.execute(
      () => this.client.getInventoryInstance({ instanceId }),
      'Nie udało się pobrać szczegółów instancji.'
    );
  }

  async createInventoryInstance(request: CreateInventoryInstanceRequest): Promise<InventoryInstanceResponse> {
    return this.execute(
      () => this.client.createInventoryInstance({ createInventoryInstanceRequest: request }),
      'Nie udało się utworzyć instancji.'
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