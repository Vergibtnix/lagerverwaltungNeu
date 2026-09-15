# LAP - Lagerverwaltung

## Projektbeschreibung
LAP ist eine Webanwendung zur Verwaltung von Produkten, Verkaeufen und Nachbestellungen.
Die Anwendung basiert auf Spring Boot (MVC) mit Thymeleaf und PostgreSQL.

Funktionen:
- Registrierung und Anmeldung fuer mehrere Benutzer
- Produktanlage, Produktbearbeitung und Produktliste mit Filtern/Sortierung
- Eindeutige Artikelnummer je Produkt (automatisch erzeugt)
- Kategorieverwaltung
- Verkaufserfassung mit automatischer Lagerbestandsanpassung
- Nachbestellungen mit separater Wareneingangsbestaetigung
- Transaktionshistorie (Verkauf + Nachbestellung)
- Storno statt physischem Loeschen bei Buchungen (nachpruefbar)
- Markierung von Produkten als `nachzuliefern` bei negativem Bestand
- Gewinn/Verlust-Auswertung pro Produkt (auf Basis von Verkaufspreis und hinterlegtem Einkaufspreis)
- Benutzerspezifische Einnahmen/Ausgaben (Verkauf/Nachbestellung) inkl. Saldo
- Lagerbericht mit Artikelnummer, Artikelname, Menge und Gesamtwert
- Top-3 Statistik nach hoechstem Lagerwert
- Gewichteter Durchschnittspreis beim Wareneingang

## Abdeckung Aufgabenblatt
1. Artikel hinzufuegen: inklusive automatisch erzeugter eindeutiger `Artikelnummer`, Name, Beschreibung
2. Artikel bearbeiten: ueber Produktdetails
3. Artikel loeschen: moeglich, solange keine Buchungen referenzieren (Schutz der Historie)
4. Bestand anzeigen: Dashboard mit Menge und Gesamtwert pro Artikel
5. Bestand aktualisieren: ueber Verkauf/Nachbestellung mit Datum, Menge und Einkaufspreis je Buchung
6. Bericht generieren: Seite `Lagerbericht` mit Artikelnummer, Name, Menge, Gesamtwert
7. Statistik: Top-3 Artikel nach Lagerwert
8. Buchungen pruefbar: Transaktionen bleiben erhalten und werden als `storniert` markiert
9. Fortgeschritten: gewichteter Durchschnittspreis wird bei Wareneingang neu berechnet
10. Mehrbenutzerbetrieb: Registrierung, Login und Zuordnung von Einnahmen/Ausgaben je Benutzer

## Technologien und Versionen
- Java: 21
- Spring Boot: 3.5.4
- Spring MVC + Thymeleaf
- Spring Data JPA + Hibernate
- Spring Security (CSRF aktiv)
- PostgreSQL: 16 (Docker)
- Maven Wrapper
- Bootstrap 5.3.3

## Datenbankschema (Tabellen und Beziehungen)
### `product_categories`
- `id` (PK)
- `name` (unique, max 100)

### `products`
- `id` (PK)
- `article_number` (unique, max 50)
- `name` (unique, max 100)
- `description` (max 5000)
- `category_id` (FK -> `product_categories.id`)
- `sale_price`
- `purchase_price`
- `stock`

### `sales`
- `id` (PK)
- `product_id` (FK -> `products.id`)
- `quantity`
- `customer_name` (optional)
- `unit_price`
- `cost_price` (Einkaufspreis zum Buchungszeitpunkt)
- `sale_date`
- `canceled_date` (optional)

### `restock_orders`
- `id` (PK)
- `product_id` (FK -> `products.id`)
- `quantity`
- `expected_delivery_date`
- `supplier`
- `ordered_date`
- `unit_purchase_price`
- `received_date` (optional)
- `canceled_date` (optional)

Beziehungen:
- Eine Kategorie hat viele Produkte (1:n)
- Ein Produkt hat viele Verkaeufe (1:n)
- Ein Produkt hat viele Nachbestellungen (1:n)

## Beschreibung der Models
- `ProductCategory`: Produktkategorie mit eindeutigem Namen
- `Product`: Stammdaten, Preise und aktueller Lagerbestand
- `SaleTransaction`: Einzelverkauf inkl. Menge, Kunde, Preis, Datum
- `RestockOrder`: Nachbestellung inkl. Lieferdatum, Lieferant und Empfangsstatus

## Installationsanleitung (Endkunde)
### Voraussetzungen
- Docker Desktop
- Java 21

### 1) PostgreSQL starten
```bash
docker compose up -d
```

Hinweis: PostgreSQL wird auf dem Host unter Port `55432` bereitgestellt.

### 2) Anwendung starten
```bash
./mvnw spring-boot:run
```

Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

### 3) Anwendung aufrufen
- `http://localhost:8088`

### 4) Benutzerkonto
- Neu registrieren unter `http://localhost:8088/register`
- Danach anmelden unter `http://localhost:8088/login`

## Testdaten
Beim ersten Start werden automatisch Beispieldaten erzeugt:
- 5 Produkte aus mehreren Kategorien
- Kategorien sind objektbezogen (z. B. Elektronik, Schreibwaren, Lebensmittel)
- Verkaeufe mit unterschiedlichen Kunden
- Nachbestellungen (offen und bestaetigt)
- Szenarien mit negativem Bestand

## Sicherheit
- Serverseitige Validierung (Bean Validation)
- Clientseitige Formularvalidierung (HTML required, min/max, maxlength)
- CSRF-Schutz in allen POST-Formularen
- SQL-Injection-Schutz durch JPA/Prepared Statements

