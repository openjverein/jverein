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
package de.jost_net.JVerein.gui.navigation;

import com.schlevoigt.JVerein.gui.action.BuchungsTexteKorrigierenAction;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.AboutAction;
import de.jost_net.JVerein.gui.action.AbrechnungSEPAAction;
import de.jost_net.JVerein.gui.action.AbrechnunslaufListAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenAbrechnungAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenAllgemeinAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenAnzeigeAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenBuchfuehrungAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenDateinamenAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenMailAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenMitgliedAnsichtAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenMitgliederSpaltenAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenRechnungenAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenSpendenbescheinigungenAction;
import de.jost_net.JVerein.gui.action.AdministrationEinstellungenStatistikAction;
import de.jost_net.JVerein.gui.action.AdressenSucheAction;
import de.jost_net.JVerein.gui.action.AdresstypListAction;
import de.jost_net.JVerein.gui.action.AnfangsbestandListAction;
import de.jost_net.JVerein.gui.action.ArbeitseinsatzUeberpruefungAction;
import de.jost_net.JVerein.gui.action.AuswertungAdressenAction;
import de.jost_net.JVerein.gui.action.AuswertungKursteilnehmerAction;
import de.jost_net.JVerein.gui.action.AuswertungMitgliedAction;
import de.jost_net.JVerein.gui.action.BackupCreateAction;
import de.jost_net.JVerein.gui.action.BackupRestoreAction;
import de.jost_net.JVerein.gui.action.BeitragsgruppeSucheAction;
import de.jost_net.JVerein.gui.action.BuchungsListeAction;
import de.jost_net.JVerein.gui.action.BuchungsartListAction;
import de.jost_net.JVerein.gui.action.BuchungsklasseListAction;
import de.jost_net.JVerein.gui.action.BuchungsklasseSaldoAction;
import de.jost_net.JVerein.gui.action.BuchungsuebernahmeAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.EigenschaftGruppeListeAction;
import de.jost_net.JVerein.gui.action.EigenschaftListeAction;
import de.jost_net.JVerein.gui.action.FamilienbeitragAction;
import de.jost_net.JVerein.gui.action.FelddefinitionenAction;
import de.jost_net.JVerein.gui.action.FormularListeAction;
import de.jost_net.JVerein.gui.action.JahresabschlussListAction;
import de.jost_net.JVerein.gui.action.JahressaldoAction;
import de.jost_net.JVerein.gui.action.JubilaeenAction;
import de.jost_net.JVerein.gui.action.KontenrahmenExportAction;
import de.jost_net.JVerein.gui.action.KontenrahmenImportAction;
import de.jost_net.JVerein.gui.action.KontoListAction;
import de.jost_net.JVerein.gui.action.KursteilnehmerSucheAction;
import de.jost_net.JVerein.gui.action.LehrgaengeListeAction;
import de.jost_net.JVerein.gui.action.LehrgangsartListeAction;
import de.jost_net.JVerein.gui.action.LesefelddefinitionenAction;
import de.jost_net.JVerein.gui.action.MailListeAction;
import de.jost_net.JVerein.gui.action.MailVorlagenAction;
import de.jost_net.JVerein.gui.action.MitgliedImportAction;
import de.jost_net.JVerein.gui.action.MitgliedSucheAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoListeAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoMahnungAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoRechnungAction;
import de.jost_net.JVerein.gui.action.ProjektListAction;
import de.jost_net.JVerein.gui.action.ProjektSaldoAction;
import de.jost_net.JVerein.gui.action.QIFBuchungsImportViewAction;
import de.jost_net.JVerein.gui.action.SpendenAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungListeAction;
import de.jost_net.JVerein.gui.action.StatistikJahrgaengeAction;
import de.jost_net.JVerein.gui.action.StatistikMitgliedAction;
import de.jost_net.JVerein.gui.action.WiedervorlageListeAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeImportAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeListeAction;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.logging.Logger;

public class MyExtension implements Extension
{

  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  @Override
  public void extend(Extendable extendable)
  {
    try
    {
      NavigationItem jverein = (NavigationItem) extendable;
      
      NavigationItem mitglieder = null;
      mitglieder = new MyItem(mitglieder, "Mitglieder", null);
      
      mitglieder.addChild(new MyItem(mitglieder, "Mitglieder",
          new MitgliedSucheAction(), "user-friends.png"));
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Adressen",
            new AdressenSucheAction(), "user-friends.png"));
      }
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Kursteilnehmer",
            new KursteilnehmerSucheAction(), "user-friends.png"));
      }
      DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      it.addFilter("beitragsart = ?",
          new Object[] { ArtBeitragsart.FAMILIE_ZAHLER.getKey() });
      if (it.size() > 0)
      {
        mitglieder.addChild(new MyItem(mitglieder, "Familienbeitrag",
            new FamilienbeitragAction(), "users.png"));
      }
      
      mitglieder.addChild(new MyItem(mitglieder, "Mitgliedskonten",
          new MitgliedskontoListeAction(), "calculator.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Rechnungen",
          new MitgliedskontoRechnungAction(), "file-invoice.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Mahnungen",
          new MitgliedskontoMahnungAction(), "file-invoice.png"));
      if (Einstellungen.getEinstellung().getArbeitseinsatz())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Arbeitseins�tze pr�fen",
            new ArbeitseinsatzUeberpruefungAction(), "screwdriver.png"));
      }

      if (Einstellungen.getEinstellung().getZusatzbetrag())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Zusatzbetr�ge",
            new ZusatzbetraegeListeAction(), "euro-sign.png"));
        mitglieder.addChild(new MyItem(mitglieder, "Zusatzbetr�ge importieren",
            new ZusatzbetraegeImportAction(), "file-import.png"));
      }
      if (Einstellungen.getEinstellung().getWiedervorlage())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Wiedervorlage",
            new WiedervorlageListeAction(), "office-calendar.png"));
      }
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Lehrg�nge",
            new LehrgaengeListeAction(), "chalkboard-teacher.png"));
      }
      mitglieder.addChild(new MyItem(mitglieder, "Spendenbescheinigungen",
          new SpendenbescheinigungListeAction(), "file-invoice.png"));
      jverein.addChild(mitglieder);

      NavigationItem abrechnung = null;
      abrechnung = new MyItem(abrechnung, "Abrechnung", null);
      abrechnung.addChild(new MyItem(abrechnung, "Abrechnung",
          new AbrechnungSEPAAction(), "calculator.png"));
      abrechnung.addChild(new MyItem(abrechnung, "Abrechnungslauf",
          new AbrechnunslaufListAction(), "calculator.png"));
      jverein.addChild(abrechnung);

      NavigationItem auswertung = null;
      auswertung = new MyItem(auswertung, "Auswertungen", null);
      auswertung.addChild(new MyItem(auswertung, "Mitglieder",
          new AuswertungMitgliedAction(), "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Adressen",
          new AuswertungAdressenAction(), "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Jubil�en",
          new JubilaeenAction(), "receipt.png"));
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        auswertung.addChild(new MyItem(auswertung, "Kursteilnehmer",
            new AuswertungKursteilnehmerAction(), "receipt.png"));
      }
      auswertung.addChild(new MyItem(auswertung, "Statistik",
          new StatistikMitgliedAction(), "chart-line.png"));
      auswertung.addChild(new MyItem(auswertung, "Statistik Jahrg�nge",
          new StatistikJahrgaengeAction(), "chart-line.png"));
      jverein.addChild(auswertung);

      NavigationItem mail = null;
      mail = new MyItem(mail, "Mail", null);
      mail.addChild(
          new MyItem(mail, "Mails", new MailListeAction(), "envelope-open.png"));
      mail.addChild(new MyItem(mail, "Mail-Vorlagen", new MailVorlagenAction(),
          "envelope-open.png"));
      jverein.addChild(mail);

      NavigationItem buchfuehrung = null;
      buchfuehrung = new MyItem(buchfuehrung, "Buchf�hrung", null);
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Konten",
          new KontoListAction(), "list.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Anfangsbest�nde",
          new AnfangsbestandListAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Hibiscus-Buchungen",
          new BuchungsuebernahmeAction(), "hibiscus-icon-64x64.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungen",
          new BuchungsListeAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungskorrektur",
          new BuchungsTexteKorrigierenAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungsklassensaldo",
          new BuchungsklasseSaldoAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Projektsaldo",
          new ProjektSaldoAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Jahressaldo",
          new JahressaldoAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Jahresabschl�sse",
          new JahresabschlussListAction(), "euro-sign.png"));
      jverein.addChild(buchfuehrung);

      NavigationItem administration = null;
      administration = new MyItem(administration, "Administration", null);

      NavigationItem administrationEinstellungen = null;
      administrationEinstellungen = new MyItem(administrationEinstellungen,
          "Einstellungen", null);
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Allgemein",
              new AdministrationEinstellungenAllgemeinAction(), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Anzeige",
              new AdministrationEinstellungenAnzeigeAction(), "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Spalten",
              new AdministrationEinstellungenMitgliederSpaltenAction(),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Ansicht",
              new AdministrationEinstellungenMitgliedAnsichtAction(),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Abrechnung",
              new AdministrationEinstellungenAbrechnungAction(), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Dateinamen",
              new AdministrationEinstellungenDateinamenAction(), "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Spendenbescheinigungen",
              new AdministrationEinstellungenSpendenbescheinigungenAction(),
              "wrench.png"));
      administrationEinstellungen.addChild(new MyItem(
          administrationEinstellungen, "Buchf�hrung",
          new AdministrationEinstellungenBuchfuehrungAction(), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Rechnungen",
              new AdministrationEinstellungenRechnungenAction(), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Mail",
              new AdministrationEinstellungenMailAction(), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Statistik",
              new AdministrationEinstellungenStatistikAction(), "wrench.png"));
      administration.addChild(administrationEinstellungen);

      administration.addChild(new MyItem(administration, "Beitragsgruppen",
          new BeitragsgruppeSucheAction(), "clone.png"));

      NavigationItem einstellungenbuchfuehrung = null;
      einstellungenbuchfuehrung = new MyItem(einstellungenbuchfuehrung,
          "Buchf�hrung", null);
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsklassen", new BuchungsklasseListAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsarten", new BuchungsartListAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Export",
              new KontenrahmenExportAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Import",
              new KontenrahmenImportAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "QIF Datei-Import",
              new QIFBuchungsImportViewAction(), "file-import.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Projekte", new ProjektListAction(), "screwdriver.png"));
      administration.addChild(einstellungenbuchfuehrung);

      administration
          .addChild(new MyItem(administration, "Eigenschaften-Gruppen",
              new EigenschaftGruppeListeAction(), "ellipsis-v.png"));
      administration.addChild(new MyItem(administration, "Eigenschaften",
          new EigenschaftListeAction(), "ellipsis-v.png"));
      administration.addChild(new MyItem(administration, "Felddefinitionen",
          new FelddefinitionenAction(), "list.png"));
      if (Einstellungen.getEinstellung().getUseLesefelder())
      {
        administration.addChild(new MyItem(administration, "Lesefelder",
            new LesefelddefinitionenAction(null), "list.png"));
      }
      // TODO deaktiviert f�r Versionsbau
      // if (Einstellungen.getEinstellung().getInventar())
      // {
      // administration.addChild(new MyItem(administration,
      // "Inventar-Lager", new InventarLagerortListeAction(),
      // "category_obj.gif"));
      // }
      administration.addChild(new MyItem(administration, "Formulare",
          new FormularListeAction(), "columns.png"));
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        administration.addChild(new MyItem(administration, "Lehrgangsarten",
            new LehrgangsartListeAction(), "chalkboard-teacher.png"));
      }
      administration.addChild(new MyItem(administration, "Import",
          new MitgliedImportAction(), "file-import.png"));
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        administration.addChild(new MyItem(administration, "Adresstypen",
            new AdresstypListAction(), "columns.png"));
      }
      NavigationItem einstellungenerweitert = null;
      einstellungenerweitert = new MyItem(einstellungenerweitert, "Erweitert",
          null);
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Diagnose-Backup erstellen", new BackupCreateAction(), "document-save.png"));
      einstellungenerweitert.addChild(
          new MyItem(einstellungenerweitert, "Diagnose-Backup importieren",
              new BackupRestoreAction(), "file-import.png"));
      administration.addChild(einstellungenerweitert);
      jverein.addChild(administration);
      jverein.addChild(new MyItem(jverein, "Dokumentation",
          new DokumentationAction(), "question-circle.png"));
      jverein.addChild(new MyItem(jverein, "Spende f�r JVerein",
          new SpendenAction(), "coins.png"));
      jverein.addChild(
          new MyItem(jverein, "�ber", new AboutAction(), "gtk-info.png"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend navigation");
    }

  }
}
