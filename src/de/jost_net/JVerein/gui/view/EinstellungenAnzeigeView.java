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
    ColumnLayout cols1 = new ColumnLayout(cont.getComposite(), 4);

    SimpleContainer cont1 = new SimpleContainer(cols1.getComposite());
    cont1.addHeadline("Mitglieder Feature Auswahl");
    cont1.addLabelPair("Arbeitseinsatz *", control.getArbeitseinsatz());
    cont1.addLabelPair("Familienbeitrag *", control.getFamilienbeitrag());
    cont1.addLabelPair("Kursteilnehmer *", control.getKursteilnehmer());
    cont1.addLabelPair("Lehrgänge *", control.getLehrgaenge());
    cont1.addLabelPair("Lesefelder *", control.getUseLesefelder());
    cont1.addLabelPair("Nicht-Mitglieder *", control.getZusatzadressen());
    cont1.addLabelPair("Rechnungen/Mahnungen *", control.getRechnungen());
    cont1.addLabelPair("Spendenbescheinigungen *",
        control.getSpendenbescheinigungen());
    cont1.addLabelPair("Wiedervorlage *", control.getWiedervorlage());
    cont1.addLabelPair("Zusatzbeträge *", control.getZusatzbetrag());
    cont1.addLabelPair("Zusatzfelder *", control.getUseZusatzfelder());

    SimpleContainer cont2 = new SimpleContainer(cols1.getComposite());
    cont2.addHeadline("Buchführung Feature Auswahl");
    cont2.addLabelPair("Projekte *", control.getProjekte());
    cont2.addLabelPair("Mittelverwendung *", control.getMittelverwendung());
    cont2.addLabelPair("Wirtschaftsplanung *", control.getWirtschaftsplanung());
    cont2.addLabelPair("Anlagenkonten *", control.getAnlagenkonten());
    cont2.addLabelPair("Rücklagenkonten", control.getRuecklagenkonten());
    cont2.addLabelPair("Forderungen/Verbindlichkeiten Konten",
        control.getVerbindlichkeitenForderungen());
    cont2.addHeadline("Sonstige Feature Auswahl");
    cont2.addLabelPair("Dokumentenspeicherung *",
        control.getDokumentenspeicherung());

    SimpleContainer cont3 = new SimpleContainer(cols1.getComposite());
    cont3.addHeadline("Mitglieder Anzeige");
    cont3.addLabelPair("Auslandsadressen (Staat)",
        control.getAuslandsadressen());
    cont3.addLabelPair("Externe Mitgliedsnummer",
        control.getExterneMitgliedsnummer());
    cont3.addLabelPair("(Ext.) Mitgliedsnummer bei Namen",
        control.getMitgliedsnummerAnzeigen());
    cont3.addLabelPair("Individuelle Beiträge",
        control.getIndividuelleBeitraege());
    cont3.addLabelPair("Juristische Personen erlaubt",
        control.getJuristischePersonen());
    cont3.addLabelPair("Kommunikationsdaten", control.getKommunikationsdaten());
    cont3.addLabelPair("Mitgliedsfoto", control.getMitgliedfoto());
    cont3.addLabelPair("Sekundäre Beitragsgruppen",
        control.getSekundaereBeitragsgruppen());
    cont3.addLabelPair("Sterbedatum", control.getSterbedatum());
    cont3.addLabelPair("Vermerke", control.getVermerke());

    SimpleContainer cont4 = new SimpleContainer(cols1.getComposite());
    cont4.addHeadline("Allgemeines");
    cont4.addLabelPair("Buchungsarten ohne Buchung unterdrücken",
        control.getUnterdrueckungOhneBuchung());
    cont4.addLabelPair("Summen Anlagenkonto in Kontensaldo",
        control.getSummenAnlagenkonto());

    cont.addSeparator();
    ColumnLayout cols2 = new ColumnLayout(cont.getComposite(), 2);
    SimpleContainer links = new SimpleContainer(cols2.getComposite());
    links.addLabelPair("Intervall für aktive Konten (Jahre)",
        control.getUnterdrueckungKonten());
    links.addLabelPair("Basis für Berechnung des Alters",
        control.getAltersModel());
    links.addLabelPair("Ort der Abschreibung", control.getAfaOrt());
    links.addLabelPair("Mitglied Auswahl", control.getMitgliedAuswahl());

    SimpleContainer rechts = new SimpleContainer(cols2.getComposite());
    rechts.addLabelPair("Ungenutzte Auto Buchungsarten unterdrücken (Monate)",
        control.getUnterdrueckungLaenge());
    rechts.addLabelPair("Buchungsart Auswahl",
        control.getBuchungBuchungsartAuswahl());
    rechts.addLabelPair("Buchungsart/-klasse Sortierung",
        control.getBuchungsartSort());
    rechts.addLabelPair("Buchungsart/-klasse Anzeige",
        control.getBuchungsartAnzeige());

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
