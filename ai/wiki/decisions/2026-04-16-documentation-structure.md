# ADR: Organizacja Dokumentacji w Projekcie

## TLDR
Wprowadzono podział dokumentacji: `doc/` dla ludzi (AsciiDoc) i `ai/` dla agentów AI (Markdown + LLM WIKI).

## Tagi
#documentation #organization #ai #llm-wiki #agents

## Kontekst
Projekt potrzebował systemu dokumentacji dla różnych grup odbiorców:
- **Dla ludzi**: Specyfikacje biznesowe, architektura, przewodniki
- **Dla agentów AI**: Informacje techniczne, konfiguracje, procedury
- **Dla przyszłych sesji AI**: Pamięć między sesjami, baza decyzji architektonicznych

Dotychczasowa dokumentacja była pomieszana i nie miała jasnej struktury.

## Decyzja
1. **Dokumentacja dla ludzi**: `doc/` w formacie AsciiDoc
   - Specyfikacje, architektura C4, przewodniki użytkownika
   - Generowanie HTML/PDF przez `render.sh`

2. **Dokumentacja dla agentów AI**: `ai/` w formacie Markdown
   - Informacje techniczne i konfiguracje
   - Łatwe parsowanie przez LLM

3. **LLM WIKI**: `ai/wiki/` jako baza wiedzy
   - `index.md` - katalog wszystkich stron
   - `log.md` - chronologiczny zapis zmian
   - `concepts/` - definicje pojęć domenowych
   - `decisions/` - Architecture Decision Records (ADR)

## Alternatywy Rozważane
- **Opcja A**: Wszystko w jednym miejscu, jeden format
- **Opcja B**: Dokumentacja tylko w kodzie (żadnych osobnych plików)
- **Opcja C**: Zewnętrzne narzędzie do dokumentacji (Confluence, Notion)

**Wybrano obecną opcję** ponieważ:
- Rozdziela grupy odbiorców
- Ułatwia pracę agentom AI
- Zachowuje istniejącą strukturę `doc/`
- Tworzy bazę wiedzy dla przyszłych sesji

## Konsekwencje

### Plusy
- ✅ Jasny podział odpowiedzialności
- ✅ Łatwiejsze zarządzanie dokumentacją
- ✅ Pamięć między sesjami agentów AI
- ✅ Możliwość rozwoju LLM WIKI

### Minusy
- ❌ Dodatkowa złożoność struktury katalogów
- ❌ Konieczność utrzymania dwóch formatów

## Implementacja
1. Utworzony katalog `ai/` dla agentów AI
2. Przeniesiony `PROFIL_LOCAL_GUIDE.md` do `ai/`
3. Utworzona struktura LLM WIKI w `ai/wiki/`
4. Zaktualizowany `AGENTS.md` z instrukcjami zarządzania wiki
5. Utworzony `DOCUMENTATION_STRUCTURE.md`

## Status
✅ **Zakończone** - Struktura dokumentacji jest zorganizowana.

## Linki
- [[DOCUMENTATION_STRUCTURE]] - Szczegóły organizacji
- [[index]] - Katalog LLM WIKI
- [[log]] - Historia zmian
