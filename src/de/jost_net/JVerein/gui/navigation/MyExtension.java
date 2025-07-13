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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.AboutAction;
import de.jost_net.JVerein.gui.action.BackupCreateAction;
import de.jost_net.JVerein.gui.action.BackupRestoreAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.KontenrahmenExportAction;
import de.jost_net.JVerein.gui.action.KontenrahmenImportAction;
import de.jost_net.JVerein.gui.action.LesefelddefinitionenAction;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.view.AbrechnungslaufListeView;
import de.jost_net.JVerein.gui.view.AnfangsbestandListeView;
import de.jost_net.JVerein.gui.view.AnlagenbuchungListeView;
import de.jost_net.JVerein.gui.view.AnlagenverzeichnisView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzListeView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzUeberpruefungView;
import de.jost_net.JVerein.gui.view.AuswertungKursteilnehmerView;
import de.jost_net.JVerein.gui.view.AuswertungMitgliedView;
import de.jost_net.JVerein.gui.view.AuswertungNichtMitgliedView;
import de.jost_net.JVerein.gui.view.BeitragsgruppeListeView;
import de.jost_net.JVerein.gui.view.BuchungsartListeView;
import de.jost_net.JVerein.gui.view.BuchungsklasseListeView;
import de.jost_net.JVerein.gui.view.BuchungsklasseSaldoView;
import de.jost_net.JVerein.gui.view.DateinameListeView;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.gui.view.BuchungsTextKorrekturView;
import de.jost_net.JVerein.gui.view.DbBereinigenView;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeListeView;
import de.jost_net.JVerein.gui.view.EigenschaftListeView;
import de.jost_net.JVerein.gui.view.EinstellungenAbrechnungView;
import de.jost_net.JVerein.gui.view.EinstellungenAllgemeinView;
import de.jost_net.JVerein.gui.view.EinstellungenAnzeigeView;
import de.jost_net.JVerein.gui.view.EinstellungenBuchfuehrungView;
import de.jost_net.JVerein.gui.view.EinstellungenDateinamenView;
import de.jost_net.JVerein.gui.view.EinstellungenMailView;
import de.jost_net.JVerein.gui.view.EinstellungenMitgliedAnsichtView;
import de.jost_net.JVerein.gui.view.EinstellungenMitgliederSpaltenView;
import de.jost_net.JVerein.gui.view.EinstellungenRechnungenView;
import de.jost_net.JVerein.gui.view.EinstellungenSpendenbescheinigungenView;
import de.jost_net.JVerein.gui.view.EinstellungenStatistikView;
import de.jost_net.JVerein.gui.view.FamilienbeitragView;
import de.jost_net.JVerein.gui.view.ZusatzfeldListeView;
import de.jost_net.JVerein.gui.view.FormularListeView;
import de.jost_net.JVerein.gui.view.FreiesFormularMailView;
import de.jost_net.JVerein.gui.view.JahresabschlussListeView;
import de.jost_net.JVerein.gui.view.JubilaeenView;
import de.jost_net.JVerein.gui.view.KontoSaldoView;
import de.jost_net.JVerein.gui.view.KontoListeView;
import de.jost_net.JVerein.gui.view.KontoauszugMailView;
import de.jost_net.JVerein.gui.view.KursteilnehmerListeView;
import de.jost_net.JVerein.gui.view.LastschriftListeView;
import de.jost_net.JVerein.gui.view.LehrgangListeView;
import de.jost_net.JVerein.gui.view.LehrgangsartListeView;
import de.jost_net.JVerein.gui.view.MahnungMailView;
import de.jost_net.JVerein.gui.view.MailListeView;
import de.jost_net.JVerein.gui.view.MailVorlageListeView;
import de.jost_net.JVerein.gui.view.MigrationView;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.MitgliedstypListeView;
import de.jost_net.JVerein.gui.view.MittelverwendungReportView;
import de.jost_net.JVerein.gui.view.MittelverwendungSaldoView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.gui.view.PreNotificationMailView;
import de.jost_net.JVerein.gui.view.ProjektListeView;
import de.jost_net.JVerein.gui.view.ProjektSaldoView;
import de.jost_net.JVerein.gui.view.QIFBuchungsImportView;
import de.jost_net.JVerein.gui.view.RechnungListeView;
import de.jost_net.JVerein.gui.view.RechnungMailView;
import de.jost_net.JVerein.gui.view.SollbuchungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.gui.view.StatistikJahrgaengeView;
import de.jost_net.JVerein.gui.view.StatistikMitgliedView;
import de.jost_net.JVerein.gui.view.SteuerListeView;
import de.jost_net.JVerein.gui.view.UmsatzsteuerSaldoView;
import de.jost_net.JVerein.gui.view.WiedervorlageListeView;
import de.jost_net.JVerein.gui.view.ZusatzbetragListeView;
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
          new StartViewAction(MitgliedListeView.class), "user-friends.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Nicht-Mitglieder",
            new StartViewAction(NichtMitgliedListeView.class),
            "user-friends.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Kursteilnehmer",
            new StartViewAction(KursteilnehmerListeView.class),
            "user-friends.png"));
      }
      DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      it.addFilter("beitragsart = ?",
          new Object[] { ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey() });
      if (it.size() > 0)
      {
        mitglieder.addChild(new MyItem(mitglieder, "Familienbeitrag",
            new StartViewAction(FamilienbeitragView.class), "users.png"));
      }
      
      mitglieder.addChild(new MyItem(mitglieder, "Sollbuchungen",
          new StartViewAction(SollbuchungListeView.class), "calculator.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Rechnungen",
            new StartViewAction(RechnungListeView.class), "file-invoice.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Spendenbescheinigungen",
            new StartViewAction(SpendenbescheinigungListeView.class),
            "file-invoice.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Zusatzbeträge",
            new StartViewAction(ZusatzbetragListeView.class),
            "euro-sign.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.WIEDERVORLAGE))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Wiedervorlagen",
            new StartViewAction(WiedervorlageListeView.class),
            "office-calendar.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Lehrgänge",
            new StartViewAction(LehrgangListeView.class),
            "chalkboard-teacher.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
      {
        mitglieder.addChild(new MyItem(mitglieder, "Arbeitseinsätze",
            new StartViewAction(ArbeitseinsatzListeView.class),
            "screwdriver.png"));
      }
      jverein.addChild(mitglieder);

      NavigationItem buchfuehrung = null;
      buchfuehrung = new MyItem(buchfuehrung, "Buchführung", null);
      // Konten
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Konten",
          new StartViewAction(KontoListeView.class),
          "system-file-manager.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Anfangsbestände",
          new StartViewAction(AnfangsbestandListeView.class),
          "system-file-manager.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Kontensaldo",
          new StartViewAction(KontoSaldoView.class),
          "system-file-manager.png"));
      // Buchungen
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungen",
          new StartViewAction(BuchungListeView.class),
          "emblem-documents.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungskorrektur",
          new StartViewAction(BuchungsTextKorrekturView.class),
          "emblem-documents.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungsklassensaldo",
          new StartViewAction(BuchungsklasseSaldoView.class),
          "emblem-documents.png"));
      // UstVA
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        buchfuehrung
            .addChild(new MyItem(buchfuehrung, "Umsatzsteuer Voranmeldung",
                new StartViewAction(UmsatzsteuerSaldoView.class),
                "coins.png"));
      }
      // Projekte
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Projektsaldo",
            new StartViewAction(ProjektSaldoView.class), "screwdriver.png"));
      }
      // Anlagen
      if (anlagenkonto)
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenbuchungen",
            new StartViewAction(AnlagenbuchungListeView.class),
            "office-chart-area.png"));
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenverzeichnis",
            new StartViewAction(AnlagenverzeichnisView.class),
            "office-chart-area.png"));
      }
      // Mittelverwendung
      if ((Boolean) Einstellungen.getEinstellung(Property.MITTELVERWENDUNG))
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Mittelverwendung",
            new StartViewAction(MittelverwendungReportView.class),
            "gnome-session-switch.png"));
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Mittelverwendungssaldo",
            new StartViewAction(MittelverwendungSaldoView.class),
            "gnome-session-switch.png"));
      }
      // Jahresabschluss
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Jahresabschlüsse",
          new StartViewAction(JahresabschlussListeView.class),
          "office-calendar.png"));
      jverein.addChild(buchfuehrung);
      
      NavigationItem abrechnung = null;
      abrechnung = new MyItem(abrechnung, "Abrechnung", null);
      abrechnung.addChild(new MyItem(abrechnung, "Abrechnungsläufe",
          new StartViewAction(AbrechnungslaufListeView.class),
          "calculator.png"));
      abrechnung.addChild(new MyItem(abrechnung, "Lastschriften",
          new StartViewAction(LastschriftListeView.class), "file-invoice.png"));
      jverein.addChild(abrechnung);

      NavigationItem auswertung = null;
      auswertung = new MyItem(auswertung, "Auswertungen", null);
      auswertung.addChild(new MyItem(auswertung, "Mitglieder",
          new StartViewAction(AuswertungMitgliedView.class), "receipt.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN))
      {
        auswertung.addChild(new MyItem(auswertung, "Nicht-Mitglieder",
            new StartViewAction(AuswertungNichtMitgliedView.class),
            "receipt.png"));
      }
      auswertung.addChild(new MyItem(auswertung, "Jubiläen",
          new StartViewAction(JubilaeenView.class), "receipt.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        auswertung.addChild(new MyItem(auswertung, "Kursteilnehmer",
            new StartViewAction(AuswertungKursteilnehmerView.class),
            "receipt.png"));
      }
      auswertung.addChild(new MyItem(auswertung, "Mitgliederstatistik",
          new StartViewAction(StatistikMitgliedView.class), "chart-line.png"));
      auswertung.addChild(new MyItem(auswertung, "Jahrgangsstatistik",
          new StartViewAction(StatistikJahrgaengeView.class),
          "chart-line.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
      {
        auswertung.addChild(new MyItem(mitglieder, "Arbeitseinsätze",
            new StartViewAction(ArbeitseinsatzUeberpruefungView.class),
            "screwdriver.png"));
      }
      jverein.addChild(auswertung);

      NavigationItem mail = null;
      mail = new MyItem(mail, "Druck & Mail", null);
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        mail.addChild(new MyItem(mail, "Rechnungen",
            new StartViewAction(RechnungMailView.class), "document-print.png"));
        mail.addChild(new MyItem(mail, "Mahnungen",
            new StartViewAction(MahnungMailView.class), "document-print.png"));
      }
      mail.addChild(new MyItem(mail, "Kontoauszüge",
          new StartViewAction(KontoauszugMailView.class),
          "document-print.png"));
      mail.addChild(new MyItem(mail, "Freie Formulare",
          new StartViewAction(FreiesFormularMailView.class),
          "document-print.png"));
      mail.addChild(new MyItem(mail, "Pre-Notification",
          new StartViewAction(PreNotificationMailView.class),
          "document-print.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        mail.addChild(new MyItem(mail, "Spendenbescheinigungen",
            new StartViewAction(SpendenbescheinigungMailView.class),
            "document-print.png"));
      }
      mail.addChild(
          new MyItem(mail, "Mails",
              new StartViewAction(MailListeView.class),
              "envelope-open.png"));
      mail.addChild(new MyItem(mail, "Mail-Vorlagen",
          new StartViewAction(MailVorlageListeView.class),
          "envelope-open.png"));
      jverein.addChild(mail);
      
      NavigationItem administration = null;
      administration = new MyItem(administration, "Administration", null);

      NavigationItem administrationEinstellungen = null;
      administrationEinstellungen = new MyItem(administrationEinstellungen,
          "Einstellungen", null);
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Allgemein",
              new StartViewAction(EinstellungenAllgemeinView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Anzeige",
              new StartViewAction(EinstellungenAnzeigeView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Spalten",
              new StartViewAction(EinstellungenMitgliederSpaltenView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Ansicht",
              new StartViewAction(EinstellungenMitgliedAnsichtView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Abrechnung",
              new StartViewAction(EinstellungenAbrechnungView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Dateinamen",
              new StartViewAction(EinstellungenDateinamenView.class),
              "wrench.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        administrationEinstellungen.addChild(
            new MyItem(administrationEinstellungen, "Spendenbescheinigungen",
                new StartViewAction(
                    EinstellungenSpendenbescheinigungenView.class),
                "wrench.png"));
      }
      administrationEinstellungen.addChild(new MyItem(
          administrationEinstellungen, "Buchführung",
          new StartViewAction(EinstellungenBuchfuehrungView.class),
          "wrench.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        administrationEinstellungen
            .addChild(new MyItem(administrationEinstellungen, "Rechnungen",
                new StartViewAction(EinstellungenRechnungenView.class),
                "wrench.png"));
      }
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Mail",
              new StartViewAction(EinstellungenMailView.class), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Statistik",
              new StartViewAction(EinstellungenStatistikView.class),
              "wrench.png"));
      administration.addChild(administrationEinstellungen);

      NavigationItem einstellungenmitglieder = null;
      einstellungenmitglieder = new MyItem(einstellungenmitglieder,
          "Mitglieder", null);
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Beitragsgruppen",
          new StartViewAction(BeitragsgruppeListeView.class), "clone.png"));
      einstellungenmitglieder
          .addChild(new MyItem(einstellungenmitglieder, "Eigenschaftengruppen",
              new StartViewAction(EigenschaftGruppeListeView.class),
              "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Eigenschaften",
          new StartViewAction(EigenschaftListeView.class),
          "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Zusatzfelder",
          new StartViewAction(ZusatzfeldListeView.class),
          "list.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.USELESEFELDER))
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lesefelder",
            new LesefelddefinitionenAction(null), "list.png"));
      }

      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Formulare",
          new StartViewAction(FormularListeView.class), "columns.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE))
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lehrgangsarten",
            new StartViewAction(LehrgangsartListeView.class),
            "chalkboard-teacher.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN))
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Mitgliedstypen",
            new StartViewAction(MitgliedstypListeView.class),
            "user-friends.png"));
      }
      administration.addChild(einstellungenmitglieder);
      
      NavigationItem einstellungenbuchfuehrung = null;
      einstellungenbuchfuehrung = new MyItem(einstellungenbuchfuehrung,
          "Buchführung", null);
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsklassen", new StartViewAction(BuchungsklasseListeView.class),
          "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsarten", new StartViewAction(BuchungsartListeView.class),
          "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Export",
              new KontenrahmenExportAction(), "document-save.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Import",
              new KontenrahmenImportAction(), "file-import.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
            "Projekte", new StartViewAction(ProjektListeView.class),
            "screwdriver.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        einstellungenbuchfuehrung
            .addChild(new MyItem(einstellungenbuchfuehrung, "Steuer",
                new StartViewAction(SteuerListeView.class), "coins.png"));
      }
      administration.addChild(einstellungenbuchfuehrung);
      
      NavigationItem einstellungenerweitert = null;
      einstellungenerweitert = new MyItem(einstellungenerweitert, "Erweitert",
          null);
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Dateinamen", new StartViewAction(DateinameListeView.class),
          "text-x-generic.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert, "Migration",
          new StartViewAction(MigrationView.class), "file-import.png"));
      einstellungenerweitert
      .addChild(new MyItem(einstellungenerweitert, "QIF-Datei-Import",
              new StartViewAction(QIFBuchungsImportView.class),
              "file-import.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Datenbank-Bereinigung", new StartViewAction(DbBereinigenView.class),
          "placeholder-loading.png"));
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
