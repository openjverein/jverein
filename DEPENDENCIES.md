# Abhängigkeiten für JVerein

Dieses Dokument beschreibt den aktuellen Stand der Abhängigkeiten und des Bootstrap-Prozesses aus [pom.xml](/Users/tobias/code/jverein/pom.xml).

## Überblick

Der Build nutzt Maven. Die Abhängigkeiten fallen aktuell in vier Gruppen:

1. reguläre Maven-Abhängigkeiten
2. `provided`-Abhängigkeiten aus öffentlichen Maven-Repositories
3. `provided`-Host-Artefakte, die vor dem normalen Build lokal ins Maven-Repository installiert werden
4. zwei verbleibende lokale `systemPath`-Abhängigkeiten im Repository

## Reguläre Maven-Abhängigkeiten

Diese Bibliotheken werden direkt aus Maven-Repositories bezogen und gehören zur normalen Laufzeit von JVerein:

- `javax.activation:activation:1.1.1`
- `com.google.zxing:core:3.5.4`
- `net.sourceforge.csvjdbc:csvjdbc:1.0.46`
- `org.dom4j:dom4j:2.2.0`
- `com.googlecode.ez-vcard:ez-vcard:0.12.2`
- `org.freemarker:freemarker:2.3.34`
- `com.itextpdf:itext-hyph-xml:5.1.1`
- `com.fasterxml.jackson.core:jackson-core:2.21.2`
- `jakarta.activation:jakarta.activation-api:2.1.4`
- `com.google.zxing:javase:3.5.4`
- `com.sun.mail:javax.mail:1.6.2`
- `joda-time:joda-time:2.14.1`
- `de.jollyday:jollyday:0.5.10`
- `org.jsoup:jsoup:1.22.1`
- `org.mustangproject:library:2.22.0`
- `org.yaml:snakeyaml:2.6`
- `com.github.mangstadt:vinnie:2.0.2`
- `org.apache.pdfbox:xmpbox:3.0.7`

## Öffentliche `provided`-Abhängigkeiten

Diese Bibliotheken werden zum Kompilieren benötigt, sollen aber nicht als reguläre Laufzeitabhängigkeiten von JVerein verpackt werden:

- `commons-lang:commons-lang:2.6`
- `net.sf.supercsv:super-csv:2.4.0`
- `com.itextpdf:itextpdf:5.5.13.5`
- `com.itextpdf:itext-pdfa:5.5.13.5`
- `org.apache.pdfbox:pdfbox:3.0.7`
- `be.cyberelf.nanoxml:nanoxml:2.2.3`
- `org.apache.velocity:velocity:1.7`
- `com.github.hbci4j:hbci4j-core:4.1.6`
- `org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:${swt.version}`
- `com.ibm.icu:icu4j:${icu4j.version}`
- `javax.annotation:javax.annotation-api:1.3.2`
- `javax.xml.bind:jaxb-api:${jaxb.api.version}`

Hinweise:

- SWT ist aktuell als Windows-x86_64-Artefakt modelliert.
- Das veröffentlichte SWT-Artefakt hat fehlerhafte Upstream-Metadaten, funktioniert im Build aber trotzdem.

## Host-Artefakte aus Jameica und Hibiscus

Diese Artefakte werden im normalen Build als `provided` verwendet:

- `de.willuhn:jameica-lib:${jameica.version}`
- `de.willuhn:hibiscus-lib:${hibiscus.version}`
- `de.willuhn:de-willuhn-ds:${jameica.version}`
- `de.willuhn:de-willuhn-util:${jameica.version}`
- `de.jost_net:obantoo-bin:${obantoo.version}`

Diese Artefakte kommen nicht aus öffentlichen Maven-Repositories. Sie werden über das Profil `bootstrap-host-artifacts` vorbereitet:

1. Jameica und Hibiscus werden als ZIP von GitHub geladen.
2. Beide Projekte werden nach `../jameica` und `../hibiscus` entpackt.
3. Dort werden die vorhandenen Ant-Builds ausgeführt.
4. Die benötigten JARs werden per `maven-install-plugin` ins lokale Maven-Repository installiert.

Der normale Maven-Build nutzt anschließend das standardmäßig aktive Profil `installed-host-artifacts` und löst diese Artefakte als normale `provided`-Dependencies aus dem lokalen Maven-Repository auf.

## Verbleibende lokale `systemPath`-Abhängigkeiten

Aktuell gibt es nur noch zwei lokale JARs, die direkt aus dem Repository referenziert werden:

- `lib/nc.jar`
- `lib/bsh-2.1.1.jar`

### `lib/nc.jar`

- Maven-Koordinate: `jonelo:numerical-chameleon-local:0`
- Scope: `system`
- Inhalt: `jonelo.NumericalChameleon.*`
- Herkunft: veraltete Numerical-Chameleon-/`n16n-desktop`-Variante
- Öffentliche Maven-Koordinate: aktuell keine

Repository: [jonelo/n16n-desktop](https://github.com/jonelo/n16n-desktop)

### `lib/bsh-2.1.1.jar`

- Maven-Koordinate: `org.beanshell:bsh-local:2.1.1`
- Scope: `system`
- Öffentliche Maven-Koordinate in exakt dieser Version: aktuell keine

## Test-Abhängigkeiten

Für Tests werden zusätzlich verwendet:

- `org.junit.jupiter:junit-jupiter:6.0.3`
- `org.junit.platform:junit-platform-launcher:6.0.3`
- `org.mockito:mockito-core:5.23.0`
- `net.bytebuddy:byte-buddy-agent:${bytebuddy.agent.version}`

Der Byte-Buddy-Agent wird im Surefire-Lauf als `-javaagent` eingebunden.

## CI-Verwendung

Die GitHub-Action für das Setup liest die Versionen aus [pom.xml](/Users/tobias/code/jverein/pom.xml), prüft die benötigten Host-Artefakte im lokalen Maven-Repository und startet den Bootstrap nur dann, wenn sie fehlen.

Der anschließende normale Build läuft ohne `systemPath` für Jameica-/Hibiscus-Artefakte.
