# LAP - Lagerverwaltung

## Projektbeschreibung
LAP ist eine Webanwendung zur Verwaltung von Produkten, Verkaeufen und Nachbestellungen.
Die Anwendung basiert auf Spring Boot (MVC) mit Thymeleaf und PostgreSQL.

Funktionen:
- Produktanlage, Produktbearbeitung und Produktliste mit Filtern/Sortierung
- Kategorieverwaltung
- Verkaufserfassung mit automatischer Lagerbestandsanpassung
- Nachbestellungen mit separater Wareneingangsbestaetigung
- Transaktionshistorie (Verkauf + Nachbestellung)
- Markierung von Produkten als `nachzuliefern` bei negativem Bestand
- Gewinn/Verlust-Auswertung pro Produkt (auf Basis von Verkaufspreis und hinterlegtem Einkaufspreis)

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
- `sale_date`

### `restock_orders`
- `id` (PK)
- `product_id` (FK -> `products.id`)
- `quantity`
- `expected_delivery_date`
- `supplier`
- `ordered_date`
- `received_date` (optional)

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
- `http://localhost:8080`

## Testdaten
Beim ersten Start werden automatisch Beispieldaten erzeugt:
- 5 Produkte aus mehreren Kategorien
- Verkaeufe mit unterschiedlichen Kunden
- Nachbestellungen (offen und bestaetigt)
- Szenarien mit negativem Bestand

## Sicherheit
- Serverseitige Validierung (Bean Validation)
- Clientseitige Formularvalidierung (HTML required, min/max, maxlength)
- CSRF-Schutz in allen POST-Formularen
- SQL-Injection-Schutz durch JPA/Prepared Statements

