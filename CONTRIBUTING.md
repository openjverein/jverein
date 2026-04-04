# Ressourcen

OpenJVerein nutzt die Homebankingsoftware Hibiscus und das GUI-Framework Jameica.

Das Git-Repository von OpenJVerein ist:

* https://github.com/openjverein/jverein

Das OpenJVerein-Repository sollte am besten geforkt werden. Um die Änderungen zu übernehmen, erstellt bitte einen Pull-Request.

# Handbuch

Das Handbuch ist im Repository https://github.com/openjverein/jverein-Book. Der Branch `master` wird automatisch mit GitBook synchronisiert und unter https://openjverein.gitbook.io/doku veröffentlicht. Für die Verwaltung existiert eine GitBook-Organisation OpenJVerein. In der Member-Ansicht von https://github.com/openjverein ist ein Einladungslink dafür.


# Entwicklungsumgebung

Für die OpenJVerein-Entwicklung werden benötigt:

- Eclipse oder IntelliJ IDEA
- Java 17+ (JDK)
- Maven

Es wird Java 17 oder eine höhere Version benötigt, damit die Kompatibilität zu Jameica gewährleistet ist.

# Initialer Checkout und Build

## Repository klonen

Klone zunächst deinen Fork von JVerein:

```shell
git clone https://github.com/<dein-user>/jverein.git
cd jverein
```

Jameica und Hibiscus müssen nicht mehr separat vorab geklont werden. Der Maven-Bootstrap lädt beide Projekte bei Bedarf selbst herunter und legt sie weiterhin als Nachbarverzeichnisse `../jameica` und `../hibiscus` an.

## Host-Artefakte initial vorbereiten

Vor dem ersten normalen Build müssen die von Jameica und Hibiscus bereitgestellten Host-Artefakte lokal ins Maven-Repository installiert werden:

```shell
mvn -Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts generate-sources
```

Dieser Schritt:

1. lädt Jameica und Hibiscus als ZIP von GitHub
2. entpackt sie nach `../jameica` und `../hibiscus`
3. führt dort die vorhandenen Build-Skripte aus
4. installiert die für JVerein benötigten Artefakte in das lokale Maven-Repository

Der Schritt muss normalerweise nur erneut ausgeführt werden, wenn:

- das lokale Maven-Repository bereinigt wurde
- die konfigurierten Jameica-/Hibiscus-Versionen im `pom.xml` geändert wurden
- die Nachbarverzeichnisse `../jameica` oder `../hibiscus` bewusst neu aufgebaut werden sollen

## Normaler Build und Test

Danach läuft der normale Build direkt mit Maven:

```shell
mvn test
mvn package
```

Für das Nightly-Artefakt:

```shell
mvn -Pnightly package
```

## Eclipse

Die allgemeine Einrichtung von Eclipse ist weiterhin hier beschrieben:

- https://www.willuhn.de/wiki/doku.php?id=develop:eclipse
- https://www.willuhn.de/wiki/doku.php?id=develop:jameica:faq

Wichtig für den aktuellen Projektstand:

1. zuerst `mvn -Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts generate-sources` ausführen
2. danach JVerein in Eclipse als Maven-Projekt mit m2e importieren
3. anschließend auch die Nachbarverzeichnisse `../jameica` und `../hibiscus` als Projekte importieren

Für lokale Tests und das Starten der Anwendung sind `../jameica` und `../hibiscus` nicht optional. Sie müssen vorhanden sein und im Workspace geöffnet werden, weil Jameica als Host-Anwendung startet und Hibiscus zusätzlich als Plugin geladen wird.

## IntelliJ

Für die Verwendung von IntelliJ:

### Projekt-Struktur

1. Klone deinen JVerein-Fork.
2. Führe im JVerein-Ordner `mvn -Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts generate-sources` aus.
3. Öffne das Projekt in IntelliJ direkt auf Basis des vorhandenen `pom.xml`.
4. Öffne zusätzlich die Nachbarverzeichnisse `../jameica` und `../hibiscus` oder importiere sie als weitere Module.
5. Unter `File -> Project Structure` muss eine SDK mit mindestens Java 17 ausgewählt werden. Das Language Level für den JVerein-Build bleibt Java 11 kompatibel.
6. Falls das Hibiscus-Modul importiert wird, muss dort gegebenenfalls der Ordner `test` aus den Sources entfernt werden.

Für lokale Tests und das Starten der Anwendung sind `../jameica` und `../hibiscus` ebenfalls nicht optional.

### Run-Konfiguration

1. Lege eine neue `Application`-Run-Konfiguration an.
2. Wähle als Modul `jameica`.
3. Wähle als Main Class `de.willuhn.jameica.Main`.
4. Für Program Arguments siehe https://www.willuhn.de/wiki/doku.php?id=develop:eclipse#launch-konfiguration_anlegen.
5. Als Working Directory verwende das Nachbarprojekt `../jameica`.

### Erster Start

1. Führe die Jameica-Konfiguration einmal aus und beende Jameica wieder.
2. Navigiere in den erzeugten Ordner `jameica.test` und öffne `cfg/de.willuhn.jameica.system.Config.properties`.
3. Ergänze dort:

```properties
jameica.plugin.dir.0=../hibiscus
jameica.plugin.dir.1=../jverein
```

4. Starte Jameica erneut. Danach werden Hibiscus und JVerein als Plugins geladen.
5. Wenn du Code geändert hast und über die IDE testest, führe vor dem nächsten Start einen Rebuild aus.


# Code Struktur
Der Code von JVerein ist in folgende Pakete gegliedert.

Fehlerausgabe in Action, io, control
Standard Buttons: Speichern, Speichern und neu, Hilfe, Neu ==========> Input und Action erstellen


### gui.view
extends AbstractView oder AbstractDetailView (überwacht Verlassen ohne Speichern)

Enthält die Anordnung von Inputelementen, Parts, Buttons. Innerhalb der View soll sich ausschließlich auf die Anordnung der GUI Elementen fokussiert werden können, daher sind hier keine Actions etc. enthalten
Fehler werden nur als Exception geworfen und ggf. geloggt (Logger.debug()/info()/error()), nicht direkt in der GUI angezeigt

### gui.control
extends AbstracControl, AbstractJVereinControl (für Überwachung vonn Verllassen ohne Speichern), FilterControl (Liste mit Filtermöglichkeiten), DruckMailControl (Mailversand), SaldoControl, ForumlarPartControl

Enthält alle Inputelemente mit Initialisierung
Für jedes Input, Part, Button eine get Funktion.
getOBJECT() zum holen des DBObjects (ruft getCurrentObject() auf und castet nach OBJECT)
prepareStore() zum Füllen der Daten aus den Inputs in das Objekt
handleStore() speichert die gefüllten Daten -> Impl->store()
Es sollen keine weiteren public Funktionen implementiert werden
Fehler werden nur per Exception behandelt, keine direkte Ausgabe, das ist Aufgabe der Actions
SQL-Abfragen werden in server definiert
Enthält listener (Mehrfachverwendete in gui.control.listener)

### server
extends AbstractDBObject, AbstractJVereinDBObject (zur Bereitstellung einer public isChanged() Funktion)

Dieses Package enthält alle Fachobjekte.
Hier wird alles was direkt mit der DB zu tun hat implementiert.
In den anderen Klassen sollte nur über das "DBObject" auf die DB zugegriffen werden.
Alle Getter und Setter der DB Attribute
deleteCheck(), insertCheck(), updateCheck() zum Testen der eingegebenen Daten. Das sollte nur hier erfolgen und nicht im Control oder Action. throws ApplicationException
getForeignObject() für Fremdschlüssel
ggf. weiter DBIterator etc.
ggf. refresh()
Hier keine GUI ausgabe, so dass auch ein Betrieb ohne GUI möglich wäre. Fehler werden nur als Exception geworfen und ggf. geloggt (Logger.debug()/info()/error()).

### gui.menu
extends ContextMenu

Die Menüeinträge eines Kontextmenüs
ggf. Spezielle ContextMenueItems, dabei auftretende Exceptions nur per Logger.error() ausgeben, nicht per GUI.
Keine Behandlung von Actions etc. das wir alles von den Actions erledigt

### gui.action
implements Action
Aktionen die beim Kick auf Menüeinträge und Buttons ausgeführt werden.
Aufruf von Views, handleStore(), doExport() etc.
Nicht die Behandlung der Aktion sofern sie Auswirkungen außerhalb der GUI hat. Also nicht direkte SQL Abfragen ausführen, sondern die entsprechenden Funktionen der Impl aufrufen.
Fehlermeldungen werden durch diese Klassen aufgefangen und ausgegeben.

### io
Alle Ein- und Ausgabe in Datei, Mail, Hibsicus etc.
Hier keine GUI ausgabe, so dass auch ein Betrieb ohne GUI möglich wäre. Fehler werden nur als Exception geworfen und ggf. geloggt (Logger.debug()/info()/error()).



### Calendar
Einträge, die im Jameica Kalender erscheinen sollen

### DBTools
Zur Zeit nur Transaction

### keys
Konstanten für Arten von Eigenschaften, zB. Formulararten, Kontoarten.

### Messaging
Messages und globale MessageConsumer.

### Queries
Ausgelagerte, umfangreiche SQL-Queries (die an mehreren Stellen benötigt werden).

### rmi
Interfaces der Fachobjekte.

### search
Objecte die bei der Jameica Suche gefunden werden sollen.

### server.DDLTOOL
Tools zum Erstellen un Bearbeiten der Datenbank-Spalten und Tabellen.

### server.DDLTool.Updates
Datenbank Updatescripte in der Form UpdateXXXX.

### server.Tools
===>verschieben

### util
Hilfsfunktionen die nichts mit der GUI zu tun haben.

### Variable
Maps die für Variablen in Mails, PDF, Abrechnung etc. verwendet werden.

## gui
Alles was hier ist, ist ausschlieslich für die GUI. Ein Serverbetrieb muss auch ohne diese Klassen auskommen.


### gui.boxes
Boxen die auf der Startseite angezeigt werden.

### gui.dialogs
extends AbstractDialog

Dialoge.

### gui.formatter
Mehrfach verwendete Formatter.

### gui.inpus
Mehrfach verwendete Inputs.

### gui.navigation
MyExtension: Die Navigation links mit allen Einträgen.

### gui.parts
extends Part, TablePart, TreePart, JVereinTablePart für die Unterstützung der Vor und Zurück Buttons.

Vorgefertigte Tabellen, Trees etc.

### gui.util
Werkzeuge für die GUI.
