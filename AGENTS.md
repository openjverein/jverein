# AGENTS.md - AI Coding Agent Guide for OpenJVerein

## Project Overview

OpenJVerein is an open-source association/club management software built as a Jameica plugin. It provides features for member management, accounting, billing, and integration with the Hibiscus home banking software. The codebase is written in Java using Java 17+.

**Key Architecture**: Jameica Plugin Model → GUI-MVC Layers → Database Objects (RMI)

## Critical Architectural Pattern: View-Control-Server (VCS) Separation

This project enforces **strict separation of concerns** across three distinct layers:

### 1. **server/** (Data & Business Logic Layer)
- **Extends**: `AbstractJVereinDBObject` or `AbstractDBObject`
- **Responsibility**: Database operations, validation, business rules
- **NO GUI dependencies** - must work standalone for potential server deployment
- **Error handling**: Only exceptions and logging (`Logger.debug/info/error()`)
- **Examples**: `MitgliedImpl`, `BuchungImpl`, `SollbuchungImpl`
- **Validation**: Implement `insertCheck()`, `updateCheck()`, `deleteCheck()` methods
- **SQL**: Do NOT write direct SQL here - use Queries package instead

### 2. **gui/control/** (Controller/Mediator Layer)
- **Extends**: `AbstractControl`, `AbstractJVereinControl`, `FilterControl`, `DruckMailControl`, `SaldoControl`
- **Responsibility**: Bind UI inputs to data objects, orchestrate prepareStore/handleStore flow
- **Methods**: Only `getXXX()` (for UI elements) and `getOBJECT()` (cast current DBObject)
- **NO public methods beyond standard pattern** - keep interface minimal
- **Listeners**: Store reusable ones in `gui/control/listener/` package
- **Error handling**: Throw exceptions only, no GUI output (Actions handle that)
- **Examples**: `MitgliedControl`, `BuchungsControl` - typically 1000-3000+ lines per complex feature

### 3. **gui/view/** (Presentation Layer)
- **Extends**: `AbstractView` or `AbstractDetailView`
- **Responsibility**: Arrange UI elements, handle layout only
- **NO Actions, Controls, or Business Logic** in views
- **Error handling**: Log with Logger, throw as Exception (never display)
- **Change monitoring**: `AbstractDetailView` auto-monitors unsaved changes
- **Examples**: `MitgliedDetailView`, `BuchungsDetailView`

### Cross-Layer Flow Pattern
```
User Action → gui/action/* → gui/control/.prepareStore() 
  → server/*.insertCheck/updateCheck() → server/*.store() 
  → gui/action catches Exception → GUI displays error
```

## 4. **gui/action/** (Event Handlers)
- **Implements**: `Action` interface
- **Responsibility**: Handle user clicks, open Views, catch exceptions, show error dialogs
- **Flow**: Launch view → call control.prepareStore() → call action-specific methods on server objects
- **Error Handling**: TRY-CATCH everything; `OperationCanceledException` for user cancellations
- **Never**: Direct SQL, business logic, or data manipulation

## Building & Dependencies

### Ant Build System (Maven + Ant)
```bash
# Initial setup - resolves Jameica, Hibiscus, and dependencies
ant -file build/build.xml resolve-dependencies

# Dependencies downloaded to: lib/ and lib.test/
# External plugins required: jameica, hibiscus (as separate git repos)
```

### Build Dependencies
- **Jameica**: GUI framework (version 2.12.0)
- **Hibiscus**: Home banking plugin (version 2.12.0)
- **SWT**: Eclipse widget toolkit (version 4.33)
- Minimum: **Java 17 JDK** (Maven target 11, but runtime needs 17)

## IDE Setup Specifics

### IntelliJ IDEA Workflow
1. Clone jverein, hibiscus, jameica repos at same parent level
2. Run: `ant -buildfile ./build/build.xml build-dependencies` to download dependencies
3. Import jverein as **Eclipse project** (not regular Java)
4. Import hibiscus and jameica as modules (remove test folder from hibiscus)
5. Run configuration: `de.willuhn.jameica.Main` (main class) in jameica module
6. Edit `jameica.test/cfg/de.willuhn.jameica.system.Config.properties`:
   ```
   jameica.plugin.dir.0=../hibiscus
   jameica.plugin.dir.1=../jverein
   ```
7. After code changes: **Rebuild project** before running

### Debugging: JVerein loads as plugin at Jameica startup

## Key Packages & Code Organization

| Package | Purpose |
|---------|---------|
| `gui/view/` | View layouts (extends AbstractView/AbstractDetailView) |
| `gui/control/` | UI state & data binding (1000-3000 LOC classes) |
| `gui/action/` | Click handlers, error dialogs, view launches |
| `gui/menu/` | Context menus (extends ContextMenu) |
| `gui/parts/` | Reusable UI components (JVereinTablePart has nav buttons) |
| `gui/input/` | Custom input fields (MitgliedInput, IBANInput, etc.) |
| `gui/formatter/` | Reusable formatters for display |
| `gui/dialogs/` | Modal dialogs (extends AbstractDialog) |
| `gui/boxes/` | Start page widgets |
| `server/` | **50+ DBObject implementations** - business logic & DB schema |
| `server/DDLTool/` | Database schema management, UpdateXXXX scripts |
| `server/Tools/` | DB utilities (currently slated for refactoring) |
| `io/` | Exporters/Importers (CSV, PDF, XML, QIF, vCard, SEPA) |
| `Queries/` | Complex SQL queries shared across code |
| `Variable/` | Template variables for mail/PDF/billing |
| `keys/` | Enum-like constants (payment types, states, etc.) |
| `rmi/` | RMI interfaces for server objects |
| `Messaging/` | Message bus (e.g., UmsatzMessageConsumer) |

## Project-Specific Conventions

### Naming
- **Impl suffix**: All server DBObjects end in `Impl` (MitgliedImpl, BuchungImpl)
- **rmi/ interfaces**: Mirror server objects without Impl suffix (Mitglied, Buchung)
- **Control suffix**: All controls end in `Control` (MitgliedControl)
- **Action suffix**: All action handlers end in `Action`
- **View suffix**: All view classes end in `View`

### Commit Messages
- **Language**: All commit messages must be written in German
- **Style**: Use clear, descriptive German sentences explaining the changes

### Error Handling Philosophy
- **server/** layer: Throw `ApplicationException` or `RemoteException`
- **gui/control/** layer: Throw exceptions, never show GUI
- **gui/action/** layer: Catch and show error dialogs to user
- Logging: `de.willuhn.logging.Logger` (not Java standard logging)

### Database Access Patterns
- **NO raw SQL in server objects** - use `server/Queries/` for complex queries
- **Foreign keys**: Implement `getForeignObject()` in server classes
- **Refresh data**: Call `refresh()` after external updates
- **Validation**: Use `deleteCheck()`, `insertCheck()`, `updateCheck()` (throws `ApplicationException`)

### GUI Patterns
- **AbstractDetailView**: Auto-watches for unsaved changes
- **JVereinTablePart**: Tables with navigation (forward/back buttons)
- **DruckMailControl**: Predefined for print/mail features
- **FilterControl**: Pre-built list filtering

### Import/Export System (io/ package)
- All Importers/Exporters implement `IO` interface
- `IORegistry` manages available formats
- Examples: `CSVBuchungsImport`, `Rechnungsausgabe`, `VCardTool`, `SEPASupport`
- New formats: Implement `IO` + register in `IORegistry`

## Critical Developer Workflows

### Adding a New Feature
1. **Create server object** (`server/XxxImpl.java` extends `AbstractJVereinDBObject`)
   - Define DB schema & getters/setters
   - Implement validation in check methods
   - No GUI code allowed
2. **Create control** (`gui/control/XxxControl.java`)
   - Initialize inputs from server object
   - Implement prepareStore() to write back to object
   - Keep as dumb mediator - no business logic
3. **Create view** (`gui/view/XxxDetailView.java`)
   - Only arrange UI elements
   - Reference control methods, never server directly
4. **Create action** (`gui/action/XxxAction.java`)
   - Launch view, handle errors, show dialogs
5. **Create RMI interface** (`rmi/Xxx.java`) for server object

### Database Changes
- Create `server/DDLTool/UpdateXXXX.java` for schema migrations
- Reference in `server/DDLTool/DDLTool.java`
- Versioning auto-increments; existing instances auto-upgrade

### Running Tests
- Currently: `maven.test.skip=true` in pom.xml (tests disabled)
- Test source at: `junit/src/de/jost_net/...`
- Manual testing via IDE run configuration

## Common Integration Points

### Hibiscus Integration
- Message consumer: `UmsatzMessageConsumer` listens to bank transactions
- Automatic booking: `Buchungsuebernahme` imports Hibiscus transactions
- Property access via `Einstellungen.getProperty(Property.HIBISCUS_KONTO_ID)`

### Jameica Plugin System
- `JVereinPlugin.java` extends `AbstractPlugin` - entry point
- Register navigation: `gui/navigation/MyExtension`
- Messaging via `de.willuhn.jameica.messaging.Message` system
- Settings persist in `de.willuhn.jameica.system.Settings`

### Mail & Export Pipeline
- `MailSender` handles email delivery
- Templates use Velocity with `Variable/*` for interpolation
- PDF generation via custom reporters: `ReportVordergrund`, `ReportHintergrund`

## When Modifying Existing Code

- **Always check if a server object exists** before creating logic in actions/controls
- **Reuse existing Controls** - most features already have one
- **Check Queries package** before writing SQL
- **Review similar features** - MitgliedImpl + MitgliedControl is the reference pattern (~1500+ LOC for complex features)
- **Understand the "big picture"**: Feature = server object (logic) + control (binding) + view (layout) + action (events)

## Critical Files to Know

- `CONTRIBUTING.md`: Contains detailed code structure overview
- `pom.xml`: Dependencies and build configuration
- `build/build.xml`: Ant orchestration, resolve-dependencies target
- `plugin.xml`: Plugin metadata and service declarations
- `src/de/jost_net/JVerein/Einstellungen.java`: Global settings registry
- `src/de/jost_net/JVerein/JVereinPlugin.java`: Plugin lifecycle and message consumers

## License

GPLv3 - all code must comply with GPL terms.
