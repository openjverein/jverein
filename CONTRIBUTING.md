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
- Java 17+ (JDK)

Es wird Java 17 oder eine höhere Version benötigt, damit die Kompatibilität zu Jameica gewährleistet ist.

# Build und Test
Build und Test sind hier beschrieben: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse

# Einrichtung der IDE
Um alle externen Abhängigkeiten bereitzustellen, muss initial das Ant-Build-Script mit dem Parameter `resolve-dependencies` ausgeführt 
werden. Dieses lädt die benötigten Bibliotheken herunter und kopiert sie in das Verzeichnis `lib`.

```shell
ant --file build/build.xml resolve-dependencies
```

Sowohl IntelliJ als auch Eclipse enthalten eine Unterstützung für Ant-Build-Scripts, die es ermöglicht, das 
Build-Script über die IDE auszuführen.

- Eclipse:
  - siehe [Running Ant Buildfiles](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-ant-running.htm)
  und im Schritt 3 `resolve-dependencies` auswählen.
  - Alternativ über die [Ant View](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Freference%2Fref-antview.htm) die
  build.xml hinzufügen, danach resolve-dependencies anklicken und auf den grünen Play-Button klicken.
- IntelliJ:
  - Plugin wie unter [Getting started with Ant](https://www.jetbrains.com/help/idea/ant.html) beschrieben, installieren und wie in der dort verlinkten früheren 
    Dokumentation beschrieben, benutzen.
## Eclipse 
Die Einrichtung von Eclipse ist hier: https://www.willuhn.de/wiki/doku.php?id=develop:eclipse und hier: https://www.willuhn.de/wiki/doku.php?id=develop:jameica:faq beschrieben.

## IntelliJ
Für die Verwendung von IntelliJ folge diesen Schritten:
### Downloads
1. Klone deinen JVerein-Fork
2. Öffne eine Kommandozeile im JVerein Ordner und führe den Command `ant -buildfile ./build/build.xml build-dependencies` aus um die Jameica und Hibiscus Abhängigkeiten zu laden.

### Projekt-Struktur
1. Um das JVerein-Projekt anzulegen, folge dieser Anleitung: https://www.jetbrains.com/help/idea/import-project-from-eclipse-page-1.html#import-project (Unter dem Punkt "Import a project with settings") und wähle den JVerein Ordner aus. Der JVerein Ordner muss als Eclipse Projekt importiert werden.
2. Die eben heruntergeladenen Abhängigkeiten, befinden sich in dem Ordner, in dem auch der JVerein Ordner liegt. Importiere diese Ordner als Module nach dieser Anleitung: https://www.jetbrains.com/help/idea/import-project-from-eclipse-page-1.html#import-as-module (Unter dem Punkt "Import an Eclipse project as a module"). Auch hier müssen die Ordner als Eclipse-Modul importiert werden.
3. Unter File -> Settings -> File Encodings: setze das Project Encoding auf ISO-8859-1
4. Unter File -> Project Structure muss eine SDK mit mindestens Java 17 ausgewählt werden und das Language Level auf Java 11 gesetzt sein.
5. Wechsle nun in diesem Fenster auf Modules und passe in allen Modulen die SDK so an, dass sie auf ein installiertes SDK verweist.
6. Ebenfalls in diesem Fenster muss aus dem Hibiscus Modul im Sources tab der Ordner test entfernt werden.
7. Lege eine neue Run/Debug Configuration an. Wähle dort "Application".
8. In der Configuration wähle als Modul "jameica" und als Main Class "de.willuhn.jameica.Main". Für die Program Arguments siehe https://www.willuhn.de/wiki/doku.php?id=develop:eclipse#launch-konfiguration_anlegen. Als Working Directory wähle jameica-<version>-nightly.src/jameica.

### Erster Start
1. Führe die eben erstellte Configuration aus. Noch sind keine Plugins installiert, schließe daher Jameica wieder.
2. Navigiere in den erstellten jameica.test Ordner und öffne die Datei `cfg/de.willuhn.jameica.system.Config.properties` in einem Text-Editor.
3. Füge die Zeilen `jameica.plugin.dir.0=../hibiscus` und `jameica.plugin.dir.1=../jverein` in die Datei ein.
4. Führe nun die Jameica-Configuration erneut aus und die Plugins werden jetzt geladen. Die Einrichtung ist abgeschlossen und du kannst anfangen an diesem Projekt mitzuwirken.
5. Wenn du etwas am Code geändert hast und du deine Änderungen testen willst, musst du vor dem erneuten Ausführen der Run-Configuration einen Rebuild des Projekts durchführen.
