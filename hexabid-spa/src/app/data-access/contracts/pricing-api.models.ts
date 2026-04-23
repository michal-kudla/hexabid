import { WadiumStrategy } from '../generated/auction-contract';

export { WadiumStrategy, PricingConfigExciseTypeEnum } from '../generated/auction-contract';

export interface MoneyVm {
  amount: string;
  currency: string;
  label: string;
}

export interface AppliedRatesVm {
  vatRate: string;
  exciseRate: string | null;
  customsDutyRate: string | null;
  wadiumType: string | null;
  wadiumTypeLabel: string | null;
}

export interface PriceBreakdownVm {
  hammerPrice: MoneyVm;
  wadiumOffset: MoneyVm;
  netto: MoneyVm;
  excise: MoneyVm;
  customsDuty: MoneyVm;
  vat: MoneyVm;
  totalDue: MoneyVm;
  appliedRates: AppliedRatesVm;
}

export interface WadiumDepositVm {
  wadiumId: string;
  auctionId: string;
  status: string;
  statusLabel: string;
  amount: MoneyVm;
  refundableOnLoss: boolean;
  deductibleOnWin: boolean;
}

export interface WadiumRefundVm {
  wadiumId: string;
  auctionId: string;
  status: string;
  statusLabel: string;
  refundAmount: MoneyVm;
}

export interface PricingConfigVm {
  wadiumStrategy: WadiumStrategy | null;
  wadiumRate: string | null;
  wadiumFixedAmount: MoneyVm | null;
  vatRate: string | null;
  isExcisable: boolean;
  exciseRate: string | null;
  exciseType: string | null;
  isImported: boolean;
  customsDutyRate: string | null;
  exciseTypeLabel: string | null;
  wadiumStrategyLabel: string | null;
}
