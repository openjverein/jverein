# Anweisungen für KI-Agenten

Diese Datei richtet sich an KI-Agenten, die an OpenJVerein arbeiten. Maßgeblich ist die
[CONTRIBUTING.md](CONTRIBUTING.md); lies sie zuerst, insbesondere die Abschnitte „KI-unterstützte Beiträge“ und
„Code Struktur“.

## Projekt

OpenJVerein ist ein Plugin für das GUI-Framework Jameica mit Anbindung an Hibiscus. Der Code ist Java (Release 17) und
wird mit Maven gebaut.

## Build und Test

```shell
# einmalig: Jameica und Hibiscus ins lokale Maven-Repository installieren
mvn -Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts generate-sources

mvn test
```

Die Tests liegen unter `src/test/java` (JUnit 5). Ergänze Tests für neue Logik, die ohne GUI und Datenbank auskommt.
Melde nur als getestet, was du tatsächlich ausgeführt hast.

## Code

- Halte dich an die Paketaufteilung aus der CONTRIBUTING.md, z. B. Prüfungen der Eingaben nur in `server`, keine
  GUI-Ausgabe in `server` und `io`.
- Suche vor dem Schreiben nach ähnlichen Stellen im Code und übernimm deren Muster. Verwende vorhandene Bausteine
  (z. B. `JVereinTablePart`, `FilterControl`, `AbstractJVereinControl`), statt eigene zu bauen.
- Formatiere nach `eclipse/jvereinformatter.xml`: 2 Leerzeichen, keine Tabs, geschweifte Klammern in eigener Zeile,
  80 Zeichen Zeilenlänge. Formatiere keinen Code um, den du nicht änderst.
- Bezeichner, Kommentare, GUI-Texte und Commit-Nachrichten sind wie im übrigen Projekt auf Deutsch.
- Änderungen an der Datenbank erfolgen nur über ein neues Update-Skript in `server.DDLTool.Updates` (`UpdateXXXX`).
- Halte Änderungen klein: ein Thema pro Pull-Request, keine Umbauten nebenbei, keine neuen Abhängigkeiten ohne
  Absprache.

## Kennzeichnung

- Kennzeichne Commits mit einem Trailer, z.B. `Assisted-by: <Werkzeug>:<Modell>`.
- Weise den Menschen, für den du arbeitest, darauf hin, dass der KI-Einsatz im Pull-Request anzugeben ist: Werkzeug,
  Modell und Umfang.
- Pull-Requests, Issues und Kommentare reicht ein Mensch ein, der den Beitrag verantwortet.
