## Plan: Umstellung des JVerein-Builds auf Maven

Der JVerein-Build ist heute Maven-basiert. Für Jameica und Hibiscus gibt es weiterhin einen Bootstrap-Schritt, der über Maven gesteuert wird und die benötigten Host-Artefakte in das lokale Maven-Repository installiert.

## Aktueller Stand

### Bereits umgesetzt

- [x] `pom.xml` ist ein vollständiges Build-POM
- [x] `src` ist als `sourceDirectory` konfiguriert
- [x] `junit/src` ist als `testSourceDirectory` konfiguriert
- [x] Nicht-Java-Ressourcen aus `src` werden in das JAR übernommen
- [x] Maven-Testlauf ist aktiv und läuft erfolgreich
- [x] Maven-Surefire ist für die vorhandenen Mockito-Tests stabilisiert
- [x] Release-ZIP wird unter `target/releases/` erzeugt
- [x] Nightly-ZIP wird unter `target/releases/nightly/` erzeugt
- [x] `plugin.xml` wird im Maven-Build transformiert
- [x] Build-Check-Workflow nutzt Maven
- [x] Nightly-Workflow nutzt Maven
- [x] Release-Workflow nutzt Maven
- [x] Host-Artefakte aus Jameica und Hibiscus werden über Maven in das lokale Repository installiert
- [x] GitHub Actions nutzen den Maven-basierten Host-Bootstrap
- [x] `DEPENDENCIES.md` beschreibt den aktuellen Zustand

### Auf echte Maven-Artefakte umgestellte Abhängigkeiten

- [x] `org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64` mit `provided`
- [x] `com.ibm.icu:icu4j` mit `provided`
- [x] `javax.annotation:javax.annotation-api` mit `provided`
- [x] `javax.xml.bind:jaxb-api` mit `provided`
- [x] `com.github.hbci4j:hbci4j-core` mit `provided`

### Über lokales Maven-Repository bereitgestellte Host-Artefakte

- [x] `de.willuhn:jameica-lib:${jameica.version}`
- [x] `de.willuhn:hibiscus-lib:${hibiscus.version}`
- [x] `de.willuhn:de-willuhn-ds:${jameica.version}`
- [x] `de.willuhn:de-willuhn-util:${jameica.version}`
- [x] `de.jost_net:obantoo-bin:${obantoo.version}`

## Was der Build heute leistet

- `mvn test` läuft erfolgreich
- `mvn package` erzeugt ein lauffähiges JVerein-JAR
- `mvn package` erzeugt ein Release-ZIP unter `target/releases/jverein.<version>.zip`
- `mvn -Pnightly package` erzeugt ein Nightly-ZIP unter `target/releases/nightly/`
- Host-Artefakte aus Jameica und Hibiscus können mit `-Dbootstrap.host.artifacts=true -Pbootstrap-host-artifacts` vorbereitet werden
- der normale Maven-Build löst die Host-Artefakte anschließend aus dem lokalen Maven-Repository auf

## Noch offene Punkte

### 1. Verbleibende lokale `systemPath`-Abhängigkeiten

Aktuell sind nur noch zwei lokale Spezial-JARs direkt aus dem Repository eingebunden:

- [ ] `lib/nc.jar`
- [ ] `lib/bsh-2.1.1.jar`

Für diese beiden Artefakte ist noch offen, ob sie dauerhaft lokal bleiben oder durch einen anderen Mechanismus ersetzt werden.

### 2. Ant-Restabhängigkeit im Host-Bootstrap

Der Bootstrap für Jameica und Hibiscus wird zwar über Maven ausgelöst, ruft intern aber weiterhin deren vorhandene Ant-Builds auf.

Aktuell betrifft das:

- [ ] Build von `jameica` über externes `build/build.xml`
- [ ] Build von `hibiscus` über externes `build/build.xml`

### 3. Release-Struktur

Der Maven-Build erzeugt die funktionalen Release-Artefakte. Optional offen bleibt, ob weitere Ausgaben wieder ergänzt werden sollen:

- [ ] Source-ZIP
- [ ] Javadoc-Ausgabe
- [ ] weitere Nebenprodukte nur falls fachlich benötigt

## Nächste sinnvolle Schritte

### Kurzfristig

1. Entscheidung zu `lib/bsh-2.1.1.jar` treffen
2. `lib/nc.jar` bewusst lokal belassen oder einen Ersatzpfad definieren
3. dokumentieren, welche Laufzeitkomponenten weiterhin von Jameica und Hibiscus bereitgestellt werden

### Mittelfristig

1. prüfen, ob der Host-Bootstrap ohne Ant-Subbuilds modelliert werden kann
2. lokale Entwicklerdokumentation vollständig auf Maven als Primärpfad ausrichten
3. optionale Release-Nebenprodukte in Maven ergänzen, falls sie noch benötigt werden

### Langfristig

1. verbleibende lokale Spezial-JARs weiter reduzieren
2. Host-Bootstrap weiter vereinfachen
3. erst danach über vollständigen Ant-Rückbau entscheiden

## Konkrete To-do-Liste ab jetzt

1. Entscheidung zu `bsh-2.1.1.jar`
2. Entscheidung zu `nc.jar`
3. Dokumentation der von Jameica/Hibiscus erwarteten Laufzeitbereitstellung
4. optional Source-ZIP und Javadoc ergänzen
5. danach Ant im Host-Bootstrap gezielt abbauen

## Empfehlung

Die große Migrationsstufe ist erreicht: JVerein baut, testet und paketiert mit Maven, und die benötigten Host-Artefakte werden Maven-kompatibel über das lokale Repository bereitgestellt.

Die nächsten sinnvollen Schritte betreffen nicht mehr die Grundmigration, sondern die Bereinigung der letzten lokalen Spezial-JARs und die weitere Reduktion der internen Ant-Nutzung.
