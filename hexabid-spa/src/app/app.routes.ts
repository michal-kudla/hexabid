import { Routes } from '@angular/router';
import { AppShellComponent } from './core/layout/app-shell.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/home/home-page.component').then((m) => m.HomePageComponent)
      },
      {
        path: 'auction/:auctionId',
        loadComponent: () =>
          import('./features/details/auction-details-page.component').then(
            (m) => m.AuctionDetailsPageComponent
          )
      },
      {
        path: 'sell',
        loadComponent: () =>
          import('./features/create/auction-create-page.component').then(
            (m) => m.AuctionCreatePageComponent
          )
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/user-dashboard-page.component').then(
            (m) => m.UserDashboardPageComponent
          )
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/products/product-catalog-page.component').then(
            (m) => m.ProductCatalogPageComponent
          )
      },
      {
        path: 'products/:productId',
        loadComponent: () =>
          import('./features/products/product-details-page.component').then(
            (m) => m.ProductDetailsPageComponent
          )
      },
      {
        path: 'inventory/batches/new',
        loadComponent: () =>
          import('./features/inventory/batch-create-page.component').then(
            (m) => m.BatchCreatePageComponent
          )
      },
      {
        path: 'inventory/instances',
        loadComponent: () =>
          import('./features/inventory/instance-manager-page.component').then(
            (m) => m.InstanceManagerPageComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
