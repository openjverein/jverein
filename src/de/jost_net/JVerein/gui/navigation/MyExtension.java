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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.schlevoigt.JVerein.gui.action.BuchungsTexteKorrigierenAction;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.AboutAction;
import de.jost_net.JVerein.gui.action.AbrechnunslaufListAction;
import de.jost_net.JVerein.gui.action.AbschreibungsListeAction;
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
import de.jost_net.JVerein.gui.action.AnfangsbestandListAction;
import de.jost_net.JVerein.gui.action.AnlagenlisteAction;
import de.jost_net.JVerein.gui.action.ArbeitseinsaetzeListeAction;
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
import de.jost_net.JVerein.gui.action.DbBereinigenAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.EigenschaftGruppeListeAction;
import de.jost_net.JVerein.gui.action.EigenschaftListeAction;
import de.jost_net.JVerein.gui.action.FamilienbeitragAction;
import de.jost_net.JVerein.gui.action.FelddefinitionenAction;
import de.jost_net.JVerein.gui.action.FormularListeAction;
import de.jost_net.JVerein.gui.action.FreieFormulareAction;
import de.jost_net.JVerein.gui.action.JahresabschlussListAction;
import de.jost_net.JVerein.gui.action.JubilaeenAction;
import de.jost_net.JVerein.gui.action.KontenrahmenExportAction;
import de.jost_net.JVerein.gui.action.KontenrahmenImportAction;
import de.jost_net.JVerein.gui.action.KontensaldoAction;
import de.jost_net.JVerein.gui.action.KontoListAction;
import de.jost_net.JVerein.gui.action.KontoauszugAction;
import de.jost_net.JVerein.gui.action.KursteilnehmerSucheAction;
import de.jost_net.JVerein.gui.action.LastschriftListAction;
import de.jost_net.JVerein.gui.action.LehrgaengeListeAction;
import de.jost_net.JVerein.gui.action.LehrgangsartListeAction;
import de.jost_net.JVerein.gui.action.LesefelddefinitionenAction;
import de.jost_net.JVerein.gui.action.MailListeAction;
import de.jost_net.JVerein.gui.action.MailVorlagenAction;
import de.jost_net.JVerein.gui.action.MitgliedMigrationAction;
import de.jost_net.JVerein.gui.action.MitgliedSucheAction;
import de.jost_net.JVerein.gui.action.MitgliedstypListAction;
import de.jost_net.JVerein.gui.action.MittelverwendungListeAction;
import de.jost_net.JVerein.gui.action.NichtMitgliedSucheAction;
import de.jost_net.JVerein.gui.action.PreNotificationAction;
import de.jost_net.JVerein.gui.action.ProjektListAction;
import de.jost_net.JVerein.gui.action.ProjektSaldoAction;
import de.jost_net.JVerein.gui.action.QIFBuchungsImportViewAction;
import de.jost_net.JVerein.gui.action.RechnungListeAction;
import de.jost_net.JVerein.gui.action.SollbuchungListeAction;
import de.jost_net.JVerein.gui.action.SollbuchungMahnungAction;
import de.jost_net.JVerein.gui.action.SollbuchungRechnungMailAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungListeAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungSendAction;
import de.jost_net.JVerein.gui.action.StatistikJahrgaengeAction;
import de.jost_net.JVerein.gui.action.StatistikMitgliedAction;
import de.jost_net.JVerein.gui.action.WiedervorlageListeAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeListeAction;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
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
      // Check ob es ein Anlagenkonto gibt
      boolean anlagenkonto = false;
      try
      {
        DBService service = Einstellungen.getDBService();
        String sql = "SELECT konto.id from konto "
            + "WHERE (kontoart = ?) ";
        anlagenkonto = (boolean) service.execute(sql,
            new Object[] { Kontoart.ANLAGE.getKey() }, new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            if (rs.next())
            {
              return true;
            }
            return false;
          }
        });
      }
      catch (Exception e)
      {
        ;
      }
      NavigationItem jverein = (NavigationItem) extendable;
      
      NavigationItem mitglieder = null;
      mitglieder = new MyItem(mitglieder, "Mitglieder", null);
      
      mitglieder.addChild(new MyItem(mitglieder, "Mitglieder",
          new MitgliedSucheAction(), "user-friends.png"));
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Nicht-Mitglieder",
            new NichtMitgliedSucheAction(), "user-friends.png"));
      }
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Kursteilnehmer",
            new KursteilnehmerSucheAction(), "user-friends.png"));
      }
      DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      it.addFilter("beitragsart = ?",
          new Object[] { ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey() });
      if (it.size() > 0)
      {
        mitglieder.addChild(new MyItem(mitglieder, "Familienbeitrag",
            new FamilienbeitragAction(), "users.png"));
      }
      
      mitglieder.addChild(new MyItem(mitglieder, "Sollbuchungen",
          new SollbuchungListeAction(), "calculator.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Rechnungen",
          new RechnungListeAction(), "file-invoice.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Spendenbescheinigungen",
          new SpendenbescheinigungListeAction(), "file-invoice.png"));
      if (Einstellungen.getEinstellung().getZusatzbetrag())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Zusatzbeträge",
            new ZusatzbetraegeListeAction(), "euro-sign.png"));
      }
      if (Einstellungen.getEinstellung().getWiedervorlage())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Wiedervorlagen",
            new WiedervorlageListeAction(), "office-calendar.png"));
      }
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Lehrgänge",
            new LehrgaengeListeAction(), "chalkboard-teacher.png"));
      }
      if (Einstellungen.getEinstellung().getArbeitseinsatz())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Arbeitseinsätze",
            new ArbeitseinsaetzeListeAction(), "screwdriver.png"));
      }
      jverein.addChild(mitglieder);

      NavigationItem buchfuehrung = null;
      buchfuehrung = new MyItem(buchfuehrung, "Buchführung", null);
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Konten",
          new KontoListAction(), "list.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Anfangsbestände",
          new AnfangsbestandListAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungen",
          new BuchungsListeAction(), "euro-sign.png"));
      if (anlagenkonto)
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenbuchungen",
          new AbschreibungsListeAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungskorrektur",
          new BuchungsTexteKorrigierenAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungsklassensaldo",
          new BuchungsklasseSaldoAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Projektsaldo",
          new ProjektSaldoAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Kontensaldo",
          new KontensaldoAction(), "euro-sign.png"));
      if (Einstellungen.getEinstellung().getMittelverwendung())
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Mittelverwendung",
            new MittelverwendungListeAction(), "euro-sign.png"));
      }
      if (anlagenkonto)
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenverzeichnis",
            new AnlagenlisteAction(), "euro-sign.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Jahresabschlüsse",
          new JahresabschlussListAction(), "euro-sign.png"));
      jverein.addChild(buchfuehrung);
      
      NavigationItem abrechnung = null;
      abrechnung = new MyItem(abrechnung, "Abrechnung", null);
      abrechnung.addChild(new MyItem(abrechnung, "Abrechnungsläufe",
          new AbrechnunslaufListAction(), "calculator.png"));
      abrechnung.addChild(new MyItem(abrechnung, "Lastschriften",
          new LastschriftListAction(), "file-invoice.png"));
      jverein.addChild(abrechnung);

      NavigationItem auswertung = null;
      auswertung = new MyItem(auswertung, "Auswertungen", null);
      auswertung.addChild(new MyItem(auswertung, "Mitglieder",
          new AuswertungMitgliedAction(), "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Nicht-Mitglieder",
          new AuswertungAdressenAction(), "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Jubiläen",
          new JubilaeenAction(), "receipt.png"));
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        auswertung.addChild(new MyItem(auswertung, "Kursteilnehmer",
            new AuswertungKursteilnehmerAction(), "receipt.png"));
      }
      auswertung.addChild(new MyItem(auswertung, "Mitgliederstatistik",
          new StatistikMitgliedAction(), "chart-line.png"));
      auswertung.addChild(new MyItem(auswertung, "Jahrgangsstatistik",
          new StatistikJahrgaengeAction(), "chart-line.png"));
      if (Einstellungen.getEinstellung().getArbeitseinsatz())
      {
        auswertung.addChild(new MyItem(mitglieder, "Arbeitseinsätze",
            new ArbeitseinsatzUeberpruefungAction(), "screwdriver.png"));
      }
      jverein.addChild(auswertung);

      NavigationItem mail = null;
      mail = new MyItem(mail, "Druck & Mail", null);
      mail.addChild(new MyItem(mail, "Rechnungen",
          new SollbuchungRechnungMailAction(), "document-print.png"));
      mail.addChild(new MyItem(mail, "Mahnungen",
          new SollbuchungMahnungAction(), "document-print.png"));
      mail.addChild(new MyItem(mail, "Kontoauszüge",
          new KontoauszugAction(), "document-print.png"));
      mail.addChild(new MyItem(mail, "Freie Formulare",
          new FreieFormulareAction(), "document-print.png"));
      mail.addChild(new MyItem(mail, "Pre-Notification",
          new PreNotificationAction(), "document-print.png"));
      mail.addChild(new MyItem(mail, "Spendenbescheinigungen",
          new SpendenbescheinigungSendAction(), "document-print.png"));
      mail.addChild(
          new MyItem(mail, "Mails", new MailListeAction(), "envelope-open.png"));
      mail.addChild(new MyItem(mail, "Mail-Vorlagen", new MailVorlagenAction(),
          "envelope-open.png"));
      jverein.addChild(mail);
      
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
          administrationEinstellungen, "Buchführung",
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

      NavigationItem einstellungenmitglieder = null;
      einstellungenmitglieder = new MyItem(einstellungenmitglieder,
          "Mitglieder", null);
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Beitragsgruppen",
          new BeitragsgruppeSucheAction(), "clone.png"));
      einstellungenmitglieder
          .addChild(new MyItem(einstellungenmitglieder, "Eigenschaftengruppen",
              new EigenschaftGruppeListeAction(), "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Eigenschaften",
          new EigenschaftListeAction(), "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Zusatzfelder",
          new FelddefinitionenAction(), "list.png"));
      if (Einstellungen.getEinstellung().getUseLesefelder())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lesefelder",
            new LesefelddefinitionenAction(null), "list.png"));
      }

      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Formulare",
          new FormularListeAction(), "columns.png"));
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lehrgangsarten",
            new LehrgangsartListeAction(), "chalkboard-teacher.png"));
      }
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Mitgliedstypen",
            new MitgliedstypListAction(), "user-friends.png"));
      }
      administration.addChild(einstellungenmitglieder);
      
      NavigationItem einstellungenbuchfuehrung = null;
      einstellungenbuchfuehrung = new MyItem(einstellungenbuchfuehrung,
          "Buchführung", null);
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsklassen", new BuchungsklasseListAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsarten", new BuchungsartListAction(), "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Export",
              new KontenrahmenExportAction(), "document-save.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Import",
              new KontenrahmenImportAction(), "file-import.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Projekte", new ProjektListAction(), "screwdriver.png"));
      administration.addChild(einstellungenbuchfuehrung);
      
      NavigationItem einstellungenerweitert = null;
      einstellungenerweitert = new MyItem(einstellungenerweitert, "Erweitert",
          null);
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert, "Migration",
          new MitgliedMigrationAction(), "file-import.png"));
      einstellungenerweitert
      .addChild(new MyItem(einstellungenerweitert, "QIF-Datei-Import",
          new QIFBuchungsImportViewAction(), "file-import.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Datenbank-Bereinigung", new DbBereinigenAction(), "placeholder-loading.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Diagnose-Backup-Export", new BackupCreateAction(), "document-save.png"));
      einstellungenerweitert.addChild(
          new MyItem(einstellungenerweitert, "Diagnose-Backup-Import",
              new BackupRestoreAction(), "file-import.png"));
      administration.addChild(einstellungenerweitert);
      jverein.addChild(administration);
      jverein.addChild(new MyItem(jverein, "Dokumentation",
          new DokumentationAction(), "question-circle.png"));
      jverein.addChild(
          new MyItem(jverein, "Über", new AboutAction(), "gtk-info.png"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend navigation");
    }

  }
}
