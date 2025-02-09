# OpenJVerein

OpenJVerein nutzt die Homebankingsoftware [Hibiscus](https://www.willuhn.de/products/hibiscus/) und das
GUI-Framework [Jameica](https://www.willuhn.de/products/jameica/).

Das OpenJVerein-Repository muss zur Weiterentwicklung [geforkt](https://github.com/openjverein/jverein/fork) werden.
Dort einen Branch vom master branch aus erstellen, um dort zu entwickeln. Um die Änderungen zu 
übernehmen, erstelle bitte einen Pull-Request. 

# Handbuch

Das Handbuch ist im Repository https://github.com/openjverein/jverein-Book. Der Branch `master` wird automatisch mit
GitBook synchronisiert und unter https://openjverein.gitbook.io/doku veröffentlicht. Für die Verwaltung existiert eine
GitBook-Organisation OpenJVerein. In der Member-Ansicht von https://github.com/openjverein ist ein Einladungslink dafür.

# Entwicklungsumgebung

Für die OpenJVerein-Entwicklung werden benötigt

- Eclipse/IntelliJ IDEA
- Java 11 (JDK)

Es wird Java 11 (keine höhere Version) benötigt, damit die Kompatibilität zu Jameica gewährleistet ist und keine APIs
verwendet werden, die in späteren Java Versionen eingeführt wurden.

# Einrichtung der IDE

1. Klone das JVerein Projekt
2. Führe
   ```maven
   mvnw -f setup-build.xml clean install
   ```
   im check out aus. Damit werden die Abhängigkeiten [Jameica](https://www.willuhn.de/products/jameica/) und
   [Hibiscus](https://www.willuhn.de/products/hibiscus/) heruntergeladen und im lokalen Maven repository
   bereitgestellt. Zusätzlich sind sie als Verzeichnisse unter den Ordnern jameica und hibiscus verfügbar.
3. Öffne das Projekt in IntelliJ oder Eclipse und stelle sicher, dass es als Maven Projekt importiert wird.
4. Füge `jameica` als Modul hinzu.
    - In IntelliJ auf File > New > Module from existing sources... klicken und ./jameica/jameica.iml auswählen
    - In Eclipse auf File > Import... > klicken
        - General > Existing Projects into Workspace auswählen
        - In Select root directory das aktuelle Verzeichnis auswählen
        - Search for nested Projects auswählen
        - jameica und hibiscus auswählen und auf Finish klicken

Ab jetzt kann entwickelt werden.

# Plugin bauen

Mit

```maven
mvnw clean package
```

wird das Plugin gebaut und liegt im `target` unter `jverein-<version>.zip`.

# Testen des Anwendung

> [!IMPORTANT]
> Die Konfiguration ist nur für Windows vorbereitet. Eine Anpasung für Linux und MacOS steht noch aus, kann jedoch
> einfach selbst durchgeführt werden, indem die Umgebungsvariable auf $HOME statt USERPROFILE gestellt wird

Zum lokalen Test muss Jameica gestartet und die Konfiguration angepasst werden, sodass die Plugins Hibiscus und 
JVerein eingebunden werden. Für IntelliJ ist eine Run configuration mit dem Namen JVerein bereits angelegt. Für 
Eclipse muss diese importiert werden
1. File > Import... > Run/Debug > Launch Configurations auswählen
2. Im Dialog `./eclipse` unter From Directory auswählen.
3. eclipse anklicken und in der rechten Seite JVerein.launch auswählen.
4. Auf Finish klicken.

Nun kann es losgehen
1. Das plugin im target Verzeichnis entpacken mit
   ```maven
   mvnw clean verify
   ```
2. JVerein mit dem Launcher bzw. der Run configuration starten. Damit startet JVerein initial und legt das 
   Verzeichnis jameica.test im home Verzeichnis (Windows %USERPROFILE%) an.
3. JVerein beenden und die plugins einbinden. Dazu in der Datei
   `%USERPROFILE%/jameica.test/cfg/de.willuhn.jameica.system.Config.properties` die Zeilen
   ```properties
   jameica.plugin.dir.0=<jverein-checkout>/hibiscus
   jameica.plugin.dir.1=<jverein-checkout>/target/jverein
   ```
   einfügen und `<jverein-checkout>` durch den Pfad zum check out des JVerein Verzeichnisses ersetzen.
4. JVerein erneut start. Hibscus und JVerein sollten nun verfügbar sein

# Links

Diese Links sind die Grundlage für die Dokumentation und verweisen auf die Referenzen im Jameica Projekt 

- [Eclipse Launch Konfiguration anlegen](https://www.willuhn.de/wiki/doku.php?id=develop:eclipse#launch-konfiguration_anlegen)
- [Jameica in Eclipse einrichten](https://www.willuhn.de/wiki/doku.php?id=develop:eclipse)
- [FAQ für Plugin-Entwickler](https://www.willuhn.de/wiki/doku.php?id=develop:jameica:faq)