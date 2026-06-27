import type {
  AuctionPriceBreakdownResponse,
  AppliedRates,
  WadiumResponse,
  WadiumRefundResponse,
  PricingConfig
} from '../generated/auction-contract';
import {
  MoneyVm,
  AppliedRatesVm,
  PriceBreakdownVm,
  WadiumDepositVm,
  WadiumRefundVm,
  PricingConfigVm
} from '../contracts/pricing-api.models';
import { AppliedRatesWadiumTypeEnum, WadiumStrategy } from '../generated/auction-contract';

function moneyLabel(amount: string, currency: string): string {
  return `${amount} ${currency}`;
}

function toMoneyVm(amount: string, currency: string): MoneyVm {
  return { amount, currency, label: moneyLabel(amount, currency) };
}

function wadiumTypeLabel(type: AppliedRatesWadiumTypeEnum | null | undefined): string | null {
  if (!type) return null;
  return type === AppliedRatesWadiumTypeEnum.FIXED ? 'Stałe' : 'Procentowe';
}

function wadiumStatusLabel(status: string): string {
  switch (status) {
    case 'PAID': return 'Wpłacone';
    case 'REFUNDED': return 'Zwrócone';
    case 'DEDUCTED': return 'Potrącone';
    default: return status;
  }
}

function wadiumStrategyLabel(strategy: WadiumStrategy | null | undefined): string | null {
  if (!strategy) return null;
  return strategy === WadiumStrategy.FIXED ? 'Stałe kwotowo' : 'Procentowe';
}

function exciseTypeLabel(type: string | null | undefined): string | null {
  if (!type) return null;
  return type === 'PERCENTAGE' ? 'Procentowe' : 'Na jednostkę';
}

export function toAppliedRatesVm(rates: AppliedRates): AppliedRatesVm {
  return {
    vatRate: rates.vatRate,
    exciseRate: rates.exciseRate ?? null,
    customsDutyRate: rates.customsDutyRate ?? null,
    wadiumType: rates.wadiumType ?? null,
    wadiumTypeLabel: wadiumTypeLabel(rates.wadiumType)
  };
}

export function toPriceBreakdownVm(response: AuctionPriceBreakdownResponse): PriceBreakdownVm {
  return {
    hammerPrice: toMoneyVm(response.hammerPrice.amount, response.hammerPrice.currency),
    wadiumOffset: toMoneyVm(response.wadiumOffset.amount, response.wadiumOffset.currency),
    netto: toMoneyVm(response.netto.amount, response.netto.currency),
    excise: toMoneyVm(response.excise.amount, response.excise.currency),
    customsDuty: toMoneyVm(response.customsDuty.amount, response.customsDuty.currency),
    vat: toMoneyVm(response.vat.amount, response.vat.currency),
    totalDue: toMoneyVm(response.totalDue.amount, response.totalDue.currency),
    appliedRates: toAppliedRatesVm(response.appliedRates)
  };
}

export function toWadiumDepositVm(response: WadiumResponse): WadiumDepositVm {
  return {
    wadiumId: response.wadiumId,
    auctionId: response.auctionId,
    status: response.status,
    statusLabel: wadiumStatusLabel(response.status),
    amount: toMoneyVm(response.amount.amount, response.amount.currency),
    refundableOnLoss: response.refundableOnLoss,
    deductibleOnWin: response.deductibleOnWin
  };
}

export function toWadiumRefundVm(response: WadiumRefundResponse): WadiumRefundVm {
  return {
    wadiumId: response.wadiumId,
    auctionId: response.auctionId,
    status: response.status,
    statusLabel: wadiumStatusLabel(response.status),
    refundAmount: toMoneyVm(response.refundAmount.amount, response.refundAmount.currency)
  };
}

export function toPricingConfigVm(config: PricingConfig | undefined | null): PricingConfigVm | null {
  if (!config) return null;

  return {
    wadiumStrategy: config.wadiumStrategy ?? null,
    wadiumRate: config.wadiumRate ?? null,
    wadiumFixedAmount: config.wadiumFixedAmount
      ? toMoneyVm(config.wadiumFixedAmount.amount, config.wadiumFixedAmount.currency)
      : null,
    vatRate: config.vatRate ?? null,
    isExcisable: config.isExcisable ?? false,
    exciseRate: config.exciseRate ?? null,
    exciseType: config.exciseType ?? null,
    isImported: config.isImported ?? false,
    customsDutyRate: config.customsDutyRate ?? null,
    exciseTypeLabel: exciseTypeLabel(config.exciseType),
    wadiumStrategyLabel: wadiumStrategyLabel(config.wadiumStrategy)
  };
}
