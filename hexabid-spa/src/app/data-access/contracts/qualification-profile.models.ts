import { QualificationProfileSummaryAbandonmentRiskEnum } from '../generated/auction-contract';

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
      { code: 'LEGAL_CAPACITY', title: 'Zdolność do czynności prawnych', question: 'Czy masz zdolność do czynności prawnych lub działasz przez poprawnego pełnomocnika?', answerType: 'YES_NO' },
      { code: 'SANCTIONS_CLEARANCE', title: 'Brak na listach sankcyjnych', question: 'Czy jesteś wolny od wpisów na listy sankcyjne?', answerType: 'YES_NO' },
      { code: 'PAYMENT_READINESS', title: 'Gotowość płatnicza', question: 'Czy potwierdzasz możliwość zapłaty w terminie?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja regulaminu', question: 'Czy akceptujesz regulamin aukcji, opłaty, terminy i konsekwencje?', answerType: 'YES_NO' }
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
      { code: 'LEGAL_CAPACITY', title: 'Zdolność do czynności prawnych', question: 'Czy masz zdolność do czynności prawnych lub działasz przez poprawnego pełnomocnika?', answerType: 'YES_NO' },
      { code: 'BENEFICIAL_OWNER_DISCLOSURE', title: 'Ujawnienie beneficjenta rzeczywistego', question: 'Czy ujawniono beneficjenta rzeczywistego i strukturę właścicielską?', answerType: 'YES_NO' },
      { code: 'SANCTIONS_CLEARANCE', title: 'Brak na listach sankcyjnych', question: 'Czy jesteś wolny od wpisów na listy sankcyjne?', answerType: 'YES_NO' },
      { code: 'EXPORT_CONTROL_ELIGIBILITY', title: 'Uprawnienie do kontroli eksportu', question: 'Czy możesz nabyć towar objęty kontrolą eksportu?', answerType: 'YES_NO' },
      { code: 'SECTOR_LICENSE', title: 'Licencja branżowa', question: 'Czy posiadasz licencję branżową wymaganą do nabycia tego przedmiotu?', answerType: 'YES_NO' },
      { code: 'ENVIRONMENTAL_HANDLING_CAPACITY', title: 'Zdolność do postępowania z przedmiotem', question: 'Czy potrafisz legalnie odebrać, przewieźć lub zutylizować przedmiot?', answerType: 'YES_NO' },
      { code: 'PAYMENT_READINESS', title: 'Gotowość płatnicza', question: 'Czy potwierdzasz możliwość zapłaty w terminie?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja regulaminu', question: 'Czy akceptujesz regulamin aukcji?', answerType: 'YES_NO' }
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
      { code: 'LEGAL_CAPACITY', title: 'Zdolność do czynności prawnych', question: 'Czy masz zdolność do czynności prawnych?', answerType: 'YES_NO' },
      { code: 'BENEFICIAL_OWNER_DISCLOSURE', title: 'Ujawnienie beneficjenta rzeczywistego', question: 'Czy ujawniono beneficjenta rzeczywistego?', answerType: 'YES_NO' },
      { code: 'PEP_DISCLOSURE', title: 'Ujawnienie statusu PEP', question: 'Czy jesteś osobą politycznie eksponowaną?', answerType: 'YES_NO' },
      { code: 'SANCTIONS_CLEARANCE', title: 'Brak na listach sankcyjnych', question: 'Czy jesteś wolny od wpisów na listy sankcyjne?', answerType: 'YES_NO' },
      { code: 'NO_CONFLICT_OF_INTEREST', title: 'Brak konfliktu interesów', question: 'Czy nie masz relacji z organizatorem, rzeczoznawcą lub sprzedającym?', answerType: 'YES_NO' },
      { code: 'NO_COLLUSION', title: 'Brak porozumienia', question: 'Czy nie uzgadniałeś ofert z innymi kandydatami?', answerType: 'YES_NO' },
      { code: 'SOURCE_OF_FUNDS', title: 'Źródło środków', question: 'Czy wskazałeś legalne źródło środków na zakup?', answerType: 'YES_NO' },
      { code: 'BID_BOND_ACCEPTANCE', title: 'Akceptacja wadium', question: 'Czy akceptujesz wadium, blokadę środków lub gwarancję?', answerType: 'YES_NO' },
      { code: 'DATA_ROOM_CONFIDENTIALITY', title: 'Poufność data room', question: 'Czy przyjmujesz poufność danych z data room?', answerType: 'YES_NO' },
      { code: 'INSIDER_INFORMATION_ABSENCE', title: 'Brak informacji niejawnej', question: 'Czy nie posiadasz niejawnych informacji dających przewagę?', answerType: 'YES_NO' },
      { code: 'TERMS_ACCEPTANCE', title: 'Akceptacja regulaminu', question: 'Czy akceptujesz regulamin aukcji?', answerType: 'YES_NO' }
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

export function profileByTemplateName(templateName: string): QualificationProfileEntry | undefined {
  return QUALIFICATION_PROFILE_CATALOG.find(p => p.templateName === templateName);
}

export function mapApiRiskToRisk(apiRisk?: QualificationProfileSummaryAbandonmentRiskEnum): 'low' | 'medium' | 'high' {
  if (apiRisk === QualificationProfileSummaryAbandonmentRiskEnum.low) return 'low';
  if (apiRisk === QualificationProfileSummaryAbandonmentRiskEnum.medium) return 'medium';
  if (apiRisk === QualificationProfileSummaryAbandonmentRiskEnum.high) return 'high';
  return 'low';
}
