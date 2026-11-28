OpenJVerein
-----------

OpenJVerein ist eine Open-Source-Vereinsverwaltung mit einer Anbindung an die ebenfalls unter Open-Source-Lizenz stehende Homebankingsoftware Hibiscus.
Die Implementierung erfolgt mit Java. Der Ablauf auf vielen Plattformen ist damit gewährleistet. Als GUI-Framework kommt Jameica zum Einsatz.
OpenJVerein ist ein github fork des [Jameica](https://www.willuhn.de/products/jameica/) Plugins [JVerein](https://www.jverein.de/). Die Entwicklung der Projekte findet parallel statt. Durch die Weiterentwicklung von OpenJVerein ist keine Kompatibilität zu JVerein gegeben. Wenn man sich für OpenJVerein entscheidet kann eine problemlose Rückkehr zu JVerein, aufgrund der Datenbankstruktur, nicht garantiert werden.

Geburtstagsliste
---------------

Unter **Mitglieder → Mitglieder → Export → Geburtstagsliste PDF** lässt sich eine Geburtstagsliste der aktuell gefilterten Mitglieder erstellen.
Die PDF trägt den Titel „Geburtstagsliste“ mit dem laufenden Kalenderjahr und enthält alle zwölf Monate von Januar bis Dezember. Monatsüberschriften verwenden die im Exportdialog eingestellte Überschriftenschrift.
Die Geburtstage stehen je Monat in einer kompakten dreispaltigen Tabelle, nach Tag sortiert: zuerst von oben nach unten, dann spaltenweise von links nach rechts.
Die Einträge haben das Format `25.3.1984 Gerd Müller (42)`; das Alter bezieht sich auf den Geburtstag im laufenden Kalenderjahr.
Mitglieder ohne Geburtsdatum werden nicht einsortiert; ihre Anzahl wird am Ende der Liste angegeben.

Mitmachen?
----------

Perfekt! Das [Forum](https://jverein-forum.de) und die [GitHub-Organisation](https://github.com/openjverein) sind dazu die ersten Anlaufstellen.
Um in die OpenJVerein-Organisation aufgenommen zu werden, erstellt bitte ein [Issue](https://github.com/openjverein/jverein/issues).

In der [CONTRIBUTING.md](CONTRIBUTING.md) wird die Einrichtung der Entwicklungsumgebung beschrieben.

Lizenz
------

OpenJVerein steht unter der [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

Kontakt
-------

- Web: https://openjverein.github.io
- eMail:
    - openjverein(at)posteo.de
