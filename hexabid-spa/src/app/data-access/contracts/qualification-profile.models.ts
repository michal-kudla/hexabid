export type AuctionCategory =
  | 'GENERAL'
  | 'LAND'
  | 'ALCOHOL'
  | 'PHARMA'
  | 'CONTROLLED_SUBSTANCE'
  | 'VEHICLE'
  | 'ART'
  | 'ELECTRONICS';

export type Jurisdiction = 'PL' | 'EU' | 'EEA' | 'WORLD';

export interface QualificationProfileEntry {
  templateName: string;
  label: string;
  description: string;
  category: AuctionCategory[];
  jurisdiction: Jurisdiction[];
  recommended: boolean;
  taskCount: number;
  hasHardVerification: boolean;
  possibleDocuments: string[];
  estimatedMinutes: string;
  abandonmentRisk: 'low' | 'medium' | 'high';
  tasks: QualificationProfileTaskPreview[];
}

export interface QualificationProfileTaskPreview {
  code: string;
  title: string;
  question: string;
  answerType: 'YES_NO' | 'TEXT';
}

export const QUALIFICATION_PROFILE_CATALOG: QualificationProfileEntry[] = [
  {
    templateName: 'PUBLIC_CONSUMER_LIGHT_V1',
    label: 'Standardowy konsument',
    description: 'Podstawowy pakiet dla zwykłych aukcji konsumenckich. Wymaga oświadczeń o tożsamości, pełnoletności i akceptacji regulaminu.',
    category: ['GENERAL', 'ELECTRONICS', 'ART'],
    jurisdiction: ['PL', 'EU', 'EEA', 'WORLD'],
    recommended: true,
    taskCount: 4,
    hasHardVerification: false,
    possibleDocuments: [],
    estimatedMinutes: '2-3',
    abandonmentRisk: 'low',
    tasks: [
      { code: 'ACTING_AS_SELF', title: 'Działam we własnym imieniu', question: 'Czy działasz we własnym imieniu, a nie w imieniu innego podmiotu?', answerType: 'YES_NO' },
      { code: 'ADULT_CONFIRMATION', title: 'Pełnoletność', question: 'Czy masz ukończone 18 lat?', answerType: 'YES_NO' },
      { code: 'RESIDENCY_DECLARATION', title: 'Deklaracja rezydencji', question: 'Czy jesteś rezydentem Polski lub EEA?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja warunków', question: 'Czy akceptujesz warunki aukcji?', answerType: 'YES_NO' }
    ]
  },
  {
    templateName: 'REGULATED_ASSET_BUYER_V1',
    label: 'Nabywca regulowany',
    description: 'Pakiet dla aukcji z wymogami regulacyjnymi: grunt, nieruchomości, akcyza. Wymaga dodatkowych oświadczeń o rezydencji, źródle środków i uprawnieniach.',
    category: ['LAND', 'ALCOHOL', 'VEHICLE'],
    jurisdiction: ['PL', 'EU', 'EEA'],
    recommended: false,
    taskCount: 8,
    hasHardVerification: true,
    possibleDocuments: ['Zezwolenie ministra', 'Zaświadczenie akcyzowe', 'Dokument rejestracji pojazdu'],
    estimatedMinutes: '5-8',
    abandonmentRisk: 'medium',
    tasks: [
      { code: 'ACTING_AS_SELF', title: 'Działam we własnym imieniu', question: 'Czy działasz we własnym imieniu?', answerType: 'YES_NO' },
      { code: 'ADULT_CONFIRMATION', title: 'Pełnoletność', question: 'Czy masz ukończone 18 lat?', answerType: 'YES_NO' },
      { code: 'RESIDENCY_DECLARATION', title: 'Deklaracja rezydencji', question: 'Czy jesteś rezydentem PL/EEA?', answerType: 'YES_NO' },
      { code: 'LEGAL_CAPACITY', title: 'Zdolność do czynności prawnych', question: 'Czy masz pełną zdolność do czynności prawnych?', answerType: 'YES_NO' },
      { code: 'NO_CONFLICT_OF_INTEREST', title: 'Brak konfliktu interesów', question: 'Czy nie masz konfliktu interesów w tej aukcji?', answerType: 'YES_NO' },
      { code: 'FUNDS_SOURCE', title: 'Źródło środków', question: 'Czy środki pochodzą z legalnego źródła?', answerType: 'YES_NO' },
      { code: 'PERMIT_OR_LICENSE', title: 'Uprawnienie lub licencja', question: 'Czy posiadasz wymagane uprawnienie?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja warunków', question: 'Czy akceptujesz warunki aukcji?', answerType: 'YES_NO' }
    ]
  },
  {
    templateName: 'HIGH_VALUE_TENDER_V1',
    label: 'Przetarg wysokiej wartości',
    description: 'Pakiet dla aukcji o wysokiej wartości: AML, sankcje, beneficjent rzeczywisty, źródło środków. Zalecany dla transakcji powyżej 10 000 EUR.',
    category: ['LAND', 'ART', 'PHARMA', 'CONTROLLED_SUBSTANCE'],
    jurisdiction: ['PL', 'EU', 'EEA'],
    recommended: false,
    taskCount: 11,
    hasHardVerification: true,
    possibleDocuments: ['Dokument KYC', 'Certyfikat AML', 'Deklaracja beneficjenta rzeczywistego'],
    estimatedMinutes: '8-12',
    abandonmentRisk: 'high',
    tasks: [
      { code: 'ACTING_AS_SELF', title: 'Działam we własnym imieniu', question: 'Czy działasz we własnym imieniu?', answerType: 'YES_NO' },
      { code: 'ADULT_CONFIRMATION', title: 'Pełnoletność', question: 'Czy masz ukończone 18 lat?', answerType: 'YES_NO' },
      { code: 'RESIDENCY_DECLARATION', title: 'Deklaracja rezydencji', question: 'Czy jesteś rezydentem PL/EEA?', answerType: 'YES_NO' },
      { code: 'LEGAL_CAPACITY', title: 'Zdolność do czynności prawnych', question: 'Czy masz pełną zdolność do czynności prawnych?', answerType: 'YES_NO' },
      { code: 'NO_CONFLICT_OF_INTEREST', title: 'Brak konfliktu interesów', question: 'Czy nie masz konfliktu interesów?', answerType: 'YES_NO' },
      { code: 'FUNDS_SOURCE', title: 'Źródło środków', question: 'Czy środki pochodzą z legalnego źródła?', answerType: 'YES_NO' },
      { code: 'NO_SANCTIONS', title: 'Brak na listach sankcyjnych', question: 'Czy nie znajdujesz się na żadnej liście sankcyjnej?', answerType: 'YES_NO' },
      { code: 'BENEFICIAL_OWNER', title: 'Beneficjent rzeczywisty', question: 'Czy jesteś beneficjentem rzeczywistym transakcji?', answerType: 'YES_NO' },
      { code: 'ANTI_MONEY_LAUNDERING', title: 'AML', question: 'Czy transakcja nie podlega zgłoszeniu AML?', answerType: 'YES_NO' },
      { code: 'PERMIT_OR_LICENSE', title: 'Uprawnienie lub licencja', question: 'Czy posiadasz wymagane uprawnienie?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja warunków', question: 'Czy akceptujesz warunki aukcji?', answerType: 'YES_NO' }
    ]
  }
];

export function categoryLabel(category: AuctionCategory): string {
  switch (category) {
    case 'GENERAL': return 'Ogólny';
    case 'LAND': return 'Grunt / Nieruchomość';
    case 'ALCOHOL': return 'Alkohol';
    case 'PHARMA': return 'Lek / Produkt farmaceutyczny';
    case 'CONTROLLED_SUBSTANCE': return 'Substancja kontrolowana';
    case 'VEHICLE': return 'Pojazd';
    case 'ART': return 'Sztuka / Antyki';
    case 'ELECTRONICS': return 'Elektronika';
  }
}

export function jurisdictionLabel(jurisdiction: Jurisdiction): string {
  switch (jurisdiction) {
    case 'PL': return 'Polska';
    case 'EU': return 'Unia Europejska';
    case 'EEA': return 'EOG';
    case 'WORLD': return 'Świat';
  }
}

export function riskLabel(risk: 'low' | 'medium' | 'high'): string {
  switch (risk) {
    case 'low': return 'Niskie';
    case 'medium': return 'Średnie';
    case 'high': return 'Wysokie';
  }
}

export function profilesForCategory(category: AuctionCategory): QualificationProfileEntry[] {
  return QUALIFICATION_PROFILE_CATALOG.filter(p => p.category.includes(category));
}

export function recommendedProfile(category: AuctionCategory): QualificationProfileEntry {
  const profiles = profilesForCategory(category);
  return profiles.find(p => p.recommended) ?? QUALIFICATION_PROFILE_CATALOG[0];
}
