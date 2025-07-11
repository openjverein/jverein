/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.EinstellungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class EinstellungenAnzeigeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Anzeige");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer cont = new ScrolledContainer(getParent());

    // Allgemeine Einstellung zu Anzeige
    ColumnLayout cols1 = new ColumnLayout(cont.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cols1.getComposite());

    left.addLabelPair("Geburtsdatum Pflichtfeld",
        control.getGeburtsdatumPflicht());
    left.addLabelPair("Eintrittsdatum Pflichtfeld",
        control.getEintrittsdatumPflicht());
    left.addLabelPair("Sterbedatum", control.getSterbedatum());
    left.addLabelPair("Kommunikationsdaten anzeigen",
        control.getKommunikationsdaten());
    left.addLabelPair("Sekundäre Beitragsgruppen anzeigen *",
        control.getSekundaereBeitragsgruppen());
    left.addLabelPair("Zusatzbeträge anzeigen *",
        control.getZusatzbetrag());
    left.addLabelPair("Zusatzbeträge auch für Ausgetretene *",
        control.getZusatzbetragAusgetretene());
    left.addLabelPair("Vermerke anzeigen", control.getVermerke());
    left.addLabelPair("Wiedervorlage anzeigen *",
        control.getWiedervorlage());
    left.addLabelPair("Kursteilnehmer anzeigen *",
        control.getKursteilnehmer());
    left.addLabelPair("Kursteilnehmer Geburtsdatum und Geschlecht Pflichtfeld",
        control.getKursteilnehmerGebGesPflicht());
    left.addLabelPair("Lehrgänge anzeigen *", control.getLehrgaenge());
    left.addLabelPair("Juristische Personen erlaubt",
        control.getJuristischePersonen());
    left.addLabelPair("Mitgliedsfoto *", control.getMitgliedfoto());
    left.addLabelPair("Mittelverwendung anzeigen *",
        control.getMittelverwendung());
    left.addLabelPair("Projekte anzeigen *", control.getProjekte());
    left.addLabelPair("Spendenbescheinigungen anzeigen *",
        control.getSpendenbescheinigungen());
    left.addLabelPair("Rechnungen/Mahnungen anzeigen *",
        control.getRechnungen());
    left.addLabelPair("Wirtschaftsplanung anzeigen" + "*",
        control.getWirtschaftsplanung());

    SimpleContainer right = new SimpleContainer(cols1.getComposite());
    right.addLabelPair("Lesefelder anzeigen *", control.getUseLesefelder());
    right.addLabelPair("Nicht-Mitglieder anzeigen *", control.getZusatzadressen());
    right.addLabelPair("Auslandsadressen *", control.getAuslandsadressen());
    right.addLabelPair("Arbeitseinsatz *", control.getArbeitseinsatz());
    right.addLabelPair("Dokumentenspeicherung *",
        control.getDokumentenspeicherung());
    right.addLabelPair("Individuelle Beiträge *",
        control.getIndividuelleBeitraege());
    right.addLabelPair("Externe Mitgliedsnummer",
        control.getExterneMitgliedsnummer());
    right.addLabelPair("(Externe) Mitgliedsnummer bei Namen anzeigen",
        control.getMitgliedsnummerAnzeigen());
    right.addLabelPair("Summen Anlagenkonto in Kontensaldo",
        control.getSummenAnlagenkonto());
    right.addLabelPair("Basis für Berechnung des Alters",
        control.getAltersModel());
    // Einstellung zum Buchungsdialog
    right.addLabelPair("Buchungsart Auswahl", control.getBuchungBuchungsartAuswahl());
    right.addLabelPair("Buchungsart Sortierung", control.getBuchungsartSort());
    right.addLabelPair("Mitglied Auswahl", control.getMitgliedAuswahl());
    right.addLabelPair("Ort der Abschreibung", control.getAfaOrt());

    cont.addSeparator();
    cont.addHeadline("* " + "Änderung erfordert Neustart");
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_ANZEIGE, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreAnzeige();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
