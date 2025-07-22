# Ressourcen


OpenJVerein nutzt die Homebankingsoftware Hibiscus und das GUI-Framework Jameica. Für die Entwicklung müssen daher deren Git-Repositories eingebunden werden:

* https://github.com/willuhn/jameica.git
* https://github.com/willuhn/hibiscus.git

Das Git-Repository von OpenJVerein kann dann über https://github.com/openjverein/jverein verwendet werden.

Das OpenJVerein-Repository sollte am besten geforkt werden. Um die Änderungen zu übernehmen, erstellt bitte einen Pull-Request.

# Handbuch

Das Handbuch ist im Repository https://github.com/openjverein/jverein-Book. Der Branch `master` wird automatisch mit GitBook synchronisiert und unter https://openjverein.gitbook.io/doku veröffentlicht. Für die Verwaltung existiert eine GitBook-Organisation OpenJVerein. In der Member-Ansicht von https://github.com/openjverein ist ein Einladungslink dafür.


# Entwicklungsumgebung

Für die OpenJVerein-Entwicklung werden benötigt

- Eclipse/IntelliJ IDEA
- Java 11 (JDK)

Es wird Java 11 (keine höhere Version) benötigt, damit die Kompatibilität zu Jameica gewährleistet ist und keine APIs verwendet werden, die in späteren Java Versionen eingeführt wurden.

# Build und Test
Build und Test sind hier beschrieben: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse

# Einrichtung der IDE
## Eclipse 
Die Einrichtung von Eclipse ist hier: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse und hier: https://www.willuhn.de/wiki/doku.php?id=develop:jameica:faq beschrieben.

## IntelliJ
Für die Verwendung von IntelliJ folge diesen Schritten:
### Downloads
1. Klone deinen JVerein-Fork
2. Downloade die Quellcodepakete des aktuellen Nightly-Builds von https://www.willuhn.de/products/hibiscus/download_ext.php und https://www.willuhn.de/products/jameica/download_ext.php
3. Entpacke die Ordner (am besten in dem Ordner, in dem auch der JVerein-Ordner liegt)

### Projekt-Struktur
1. Um das JVerein-Projekt anzulegen, folge dieser Anleitung: https://www.jetbrains.com/help/idea/import-project-from-eclipse-page-1.html#import-project (Unter dem Punkt "Import a project with settings") und wähle den JVerein Ordner aus.
2. Importiere die entpackten Nightly-Build Ordner als Module nach dieser Anleitung: https://www.jetbrains.com/help/idea/import-project-from-eclipse-page-1.html#import-as-module (Unter dem Punkt "Import an Eclipse project as a module")
3. Jetzt muss sichergestellt werden, dass die richtige SWT ausgewählt wurde. Dazu öffne File->Project Structure. Wähle in diesem Menü swt.jar aus. Dort drückst du auf das "+" und lokalisierst wie hier: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse#classpath_anpassen beschrieben, die für dein System passende SWT aus.
4. Führe einen Build des Projekts aus (Strg + F9)
5. Behebe die Build-Fehler, in dem du mit der Maus über die markierten Importe fährst und in den Quick-Fixes jeweils "Add library <xy> to classpath" auswählst. Führe danach den Build erneut aus.
6. Lege eine neue Run/Debug Configuration an. Wähle dort "Application".
7. In der Configuration wähle als Modul "jameica" und als Main Class "de.willuhn.jameica.Main". Für die Program Arguments siehe https://www.willuhn.de/wiki/doku.php?id=develop:eclipse#launch-konfiguration_anlegen. Als Working Directory wähle jameica-<version>-nightly.src/jameica.

### Erster Start
1. Führe die eben erstellte Configuration aus. Noch sind keine Plugins installiert, schließe daher Jameica wieder.
2. Navigiere in den erstellten jameica.test Ordner und öffne die Datei `cfg/de.willuhn.jameica.system.Config.properties` in einem Text-Editor.
3. Füge die Zeilen `jameica.plugin.dir.0=../../hibiscus-<version>-nightly.src/hibiscus` und `jameica.plugin.dir.1=../../jverein` in die Datei ein.
4. Führe nun die Jameica-Configuration erneut aus und die Plugins werden jetzt geladen. Die Einrichtung ist abgeschlossen und du kannst anfangen an diesem Projekt mitzuwirken.
5. Wenn du etwas am Code geändert hast und du deine Änderungen testen willst, musst du vor dem erneuten Ausführen der Run-Configuration einen Rebuild des Projekts durchführen.


# Code Struktur
Der Code von JVerein ist in folgende Packages gegliedert.

Fehlerausgabe in Action, io, control
Standard Buttons: Speichern, Speichern und neu, Hilfe, Neu ==========> Input und Action erstellen


### gui.view
extends AbstractView oder AbstractDetailView (überwacht Verlassen ohne Speichern)

Enthält die Anordnung von Inputelementen, Parts, Buttons. Innerhalb der View soll sich ausschließlich auf die Anordnung der GUI Elementen fokussiert werden können, daher sind hier keine Actions etc. enthalten
Fehler werden nur als Exception geworfen und ggf. geloggt (Logger.debug()/info()/error()), nicht direkt in der GUI angezeigt

### gui.control
extends AbstracControl, AbstractJVereinControl (für Überwachung vonn Verllassen ohne Speichern), FilterControl (Liste mit Filtermöglichkeiten), DruckMailControl (Mailversand), SaldoControl, ForumlarPartControl

Enthält alle Inputelemente mit initialisierng
Für jedes Input, Part, Button eine get Funktion.
getOBJECT() zum holen des DBObjects (ruft getCurrentObject() auf und castet nach OBJECT)
fill() zum füllen der Daten aus den Inputs in das Object
handleStore() speichert die gefüllten Daten -> Impl->store()
Es sollen keine weiteren public Funktionen implementiert werden
Fehler werden nur per Exception behandelt, keine direkte Ausgabe, das ist Aufgabe der Actions
SQL-Abfragen werden in server definiert
Enthält Listener (Mehrfachverwendete in gui.control.listener)

### server
extends AbstractDBObject oder AbstractJVereinDBObject (zur bereitstellung einer public isChanged() funktion)

Dieses Package enthält alle Fachobjekte.
Hier wird alles was direkt mit der DB zu tun hat implementiert.
In den anderen KLssen sollte möglichst nur über diese DBObjecte auf die DB zugegriffen werden, keine direkten SQL Queries.
Alle getter und Setter der DB Attribute
deleteCheck(), insertCheck(), updateCheck() zum testen der eingegebenen Daten. Das sollte nur hier erfolgen und nicht im Control oder Action. throws ApplicationException
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
Nicht die Behandlung der Aktion sofern sie auswirkungen außerhalb der GUI hat (Also nicht direkte SQL Queries ausführen sondern die entsprechenden Funktionen der Impl aufrufen)
Fehlermeldungen werden durch diese Klassen aufgefangen und ausgegeben.

### io
Alle Ein- und Ausgabe in Datei, Mail, Hibsicus etc.
Hier keine GUI ausgabe, so dass auch ein Betrieb ohne GUI möglich wäre. Fehler werden nur als Exception geworfen und ggf. geloggt (Logger.debug()/info()/error()).



### Calendar
Einträge, die im Jameica Kalender erscheinen sollen

### DBTools
zzT. nur Transaction

### keys
Konstanten für Arten von Eigenschaften, zB. Formulararten, Kontoarten.

### Messaging
Messages und globale MessageConsumer.

### Queries
Ausgelagerte, umfangreiche SQL-Queries (die an mehreren stellen benötigt werden).

### rmi
Interfaces der Fachobjekte.

### search
Objecte die bei der Jameica Suche gefunden werden sollen.

### server.DDLTOOL
Tools zum erstellen un Bearbeiten der DBSpalten und Tabellen.

### server.DDLTool.Updates
DB Updatescripte in der Form UpdateXXXX.

### server.Tools
===>verschieben

### util
Hilfsfunktionen die nichts mit der gui zu tun haben

### Variable
Maps die für Variablen in Mails, Pdfs, Abrechnung etc. verwendet werden

## gui
Alles was hier ist, ist ausschlieslich für die GUI, ein Serverbetrieb muss auch ohne diese Klassen auskommen!


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
extends Part, TablePart, TreePart

Vorgefertigte Tabellen, Trees etc.

### gui.util
Werkzeuge für die GUI

