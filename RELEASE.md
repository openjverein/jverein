# Release-Prozess

Dieses Dokument beschreibt den aktuellen Release-Ablauf für OpenJVerein auf Basis von [pom.xml](pom.xml) und [release.yml](.github/workflows/release.yml).

## Version vorbereiten

Die freizugebende Version wird in zwei Dateien gepflegt und muss dort übereinstimmen:

- [pom.xml](pom.xml)
- [plugin.xml](plugin.xml)

Vor einem Release muss die neue Zielversion in beiden Dateien eingetragen und committed sein.

Wichtig: Der Maven-Build verwendet `project.version` aus `pom.xml` als technische Build-Version. `plugin.xml` muss weiterhin inhaltlich zur Plugin-Version passen.

## Lokale Prüfung

Vor dem eigentlichen Release sollte der Build lokal einmal vollständig geprüft werden:

```shell
mvn -Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts generate-sources
mvn test
mvn package
```

Das Release-Artefakt liegt danach unter:

```text
target/releases/jverein.<version>.zip
```

Die für das Jameica-Repository bestimmte `plugin.xml` wird erzeugt unter:

```text
target/releases/jverein/plugin.xml
```

## Offizielles Release

Das offizielle Release wird über den GitHub-Workflow [release.yml](.github/workflows/release.yml) gestartet.

Dabei gilt:

- Für ein Bugfix-Release muss der Workflow auf dem jeweiligen Versions-Branch gestartet werden, zum Beispiel `4.1`.
- Für ein neues Minor-Release muss der Workflow auf `master` gestartet werden.
- Für ein neues Major-Release müssen die Zielversionen in [pom.xml](pom.xml) und [plugin.xml](plugin.xml) vor dem Start des Workflows manuell angepasst werden.

Ablauf des Workflows:

1. Build-Umgebung aufsetzen und fehlende Host-Artefakte aus Jameica und Hibiscus ins lokale Maven-Repository bootstrappen.
2. JVerein mit `mvn -B -f jverein/pom.xml package` bauen.
3. Die Version aus `project.version` lesen.
4. Das GitHub-Release mit `jverein.<version>.zip` anlegen.
5. Die erzeugte ZIP-Datei und die generierte `plugin.xml` in das Website-Repository `openjverein/openjverein.github.io` unter `jameica-repository/<major>.<minor>/` kopieren.
6. Die ZIP-Datei für das Jameica-Repository signieren.
7. Anschließend wird im Workflow die nächste Entwicklungsversion in `pom.xml` und `plugin.xml` hochgezählt und committed. Auf `master` wird zusätzlich ein neuer Bugfix-Branch `<major>.<minor>` angelegt.

Die Dateien im Website-Repository müssen daher nicht mehr manuell gepflegt werden.

## Nightly-Build

Der Nightly-Build läuft separat über [nightly-build.yml](.github/workflows/nightly-build.yml).

Lokal kann das Nightly-Artefakt erzeugt werden mit:

```shell
mvn -Pnightly package
```

Das Nightly-ZIP liegt danach unter:

```text
target/releases/nightly/jverein.<version>-nightly.zip
```

## Wichtige Hinweise

- Die Release-ZIP und das Nightly-ZIP werden über das `maven-assembly-plugin` erzeugt.
- Der Veröffentlichungsordner im Jameica-Repository wird aus `project.version` als `<major>.<minor>` abgeleitet.
- Für lokale Builds reicht nach einmaligem Bootstrap in der Regel `mvn test` oder `mvn package`.
- `target/jverein-sources.jar` und `target/releases/javadoc/` werden lokal erzeugt, aktuell aber nicht über die GitHub-Release-Workflows veröffentlicht.
