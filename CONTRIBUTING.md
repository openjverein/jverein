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
- Java 17 (JDK)

Es wird Java 17 (keine höhere Version) benötigt, damit die Kompatibilität zu Jameica gewährleistet ist und keine 
APIs verwendet werden, die in späteren Java Versionen eingeführt wurden.

# Build und Test
Build und Test sind hier beschrieben: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse

# Einrichtung der IDE
Um alle externen Abhängigkeiten bereitzustellen, muss initial das Ant-Build-Scripts mit dem Parameter `resolve-dependencies` ausgeführt 
werden. Dieses lädt die benötigten Bibliotheken herunter und kopiert sie in das Verzeichnis `lib`.

```shell
ant --file build/build.xml resolve-dependencies
```

Sowohl IntelliJ als auch Eclipse enthalten eine Unterstützung für Ant-Build-Scripts, die es ermöglicht, das 
Build-Script über die IDE auszuführen.

- Eclipse: https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-ant-running.htm
- IntelliJ: https://www.jetbrains.com/help/idea/ant.html
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
