Kurzanleitung für die Erstellung eines neuen Release
----------------------------------------------------

**Im Projekt openjverein/jverein**

- Öffne build/RELEASE im Editor und passe die Versionsnummer an
- Wechsle in den Ordner "build", öffne dort ein Terminalfenster und gib den Befehl "ant" ein
- Hierbei wird auch die Build-Nummer in "build/BUILD" aktualisiert
- Im Ordner "releases/${version}-${build}/" findest du die ZIP-Datei mit dem neuen Release
- Erstelle ein GIT-Tag mit der Versionsnummer ("git tag ${version}")
- Führe ein GIT Commit+Push durch, um Tag und "build/BUILD" in Upstream zu übernehmen
- Erstelle ein neues Release unter https://github.com/openjverein/jverein und lade dort die Artefakte aus "releases/${version}-${build}/" hoch

**Im Projekt openjverein/openjverein.github.io**

Die folgenden Schritte sind nötig, damit das neue Release vom Update-Manager in Jameica automatisch erkannt und heruntergeladen wird. Andernfalls müssen User die neue Version manuell installieren.

- Ersetze die Datei "jameica-repository/plugin.xml" gegen die Version mit der neuen Versionsnummer (die Datei ist in o.g. ZIP-Datei enthalten)
- Füge die ZIP-Datei "jverein.${version}.zip" zum Ordner "jameica-repository" hinzu
- Führe ein GIT Commit+Push durch, das neue Release zu veröffentlichen
