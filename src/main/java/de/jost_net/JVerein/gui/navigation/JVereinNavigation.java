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
import de.jost_net.JVerein.gui.view.AbweichendeZahlerView;
import de.jost_net.JVerein.gui.view.AnfangsbestandListeView;
import de.jost_net.JVerein.gui.view.AnlagenbuchungListeView;
import de.jost_net.JVerein.gui.view.AnlagenverzeichnisView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzListeView;
import de.jost_net.JVerein.gui.view.BeitragsgruppeListeView;
import de.jost_net.JVerein.gui.view.BelegListeView;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.gui.view.BuchungsartListeView;
import de.jost_net.JVerein.gui.view.BuchungsklasseListeView;
import de.jost_net.JVerein.gui.view.BuchungsklasseSaldoView;
import de.jost_net.JVerein.gui.view.DbBereinigenView;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeListeView;
import de.jost_net.JVerein.gui.view.EigenschaftListeView;
import de.jost_net.JVerein.gui.view.EinstellungenAbrechnungView;
import de.jost_net.JVerein.gui.view.EinstellungenAllgemeinView;
import de.jost_net.JVerein.gui.view.EinstellungenAnzeigeView;
import de.jost_net.JVerein.gui.view.EinstellungenBuchfuehrungView;
import de.jost_net.JVerein.gui.view.EinstellungenMailView;
import de.jost_net.JVerein.gui.view.EinstellungenMitgliedAnsichtView;
import de.jost_net.JVerein.gui.view.EinstellungenRechnungenView;
import de.jost_net.JVerein.gui.view.EinstellungenReportsView;
import de.jost_net.JVerein.gui.view.EinstellungenSpendenbescheinigungenView;
import de.jost_net.JVerein.gui.view.EinstellungenStatistikView;
import de.jost_net.JVerein.gui.view.EinstellungenVerzeichnisView;
import de.jost_net.JVerein.gui.view.EinstellungenVorlageListeView;
import de.jost_net.JVerein.gui.view.FamilienbeitragView;
import de.jost_net.JVerein.gui.view.FormularListeView;
import de.jost_net.JVerein.gui.view.FreiesFormularMailView;
import de.jost_net.JVerein.gui.view.JahresabschlussListeView;
import de.jost_net.JVerein.gui.view.KontoListeView;
import de.jost_net.JVerein.gui.view.KontoSaldoView;
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
import de.jost_net.JVerein.gui.view.PersonalbogenMailView;
import de.jost_net.JVerein.gui.view.PreNotificationMailView;
import de.jost_net.JVerein.gui.view.ProjektListeView;
import de.jost_net.JVerein.gui.view.ProjektSaldoView;
import de.jost_net.JVerein.gui.view.QIFBuchungsImportView;
import de.jost_net.JVerein.gui.view.RechnungListeView;
import de.jost_net.JVerein.gui.view.RechnungMailView;
import de.jost_net.JVerein.gui.view.SollbuchungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.gui.view.SteuerListeView;
import de.jost_net.JVerein.gui.view.UmsatzsteuerSaldoView;
import de.jost_net.JVerein.gui.view.WiedervorlageListeView;
import de.jost_net.JVerein.gui.view.WirtschaftsplanListeView;
import de.jost_net.JVerein.gui.view.ZusatzbetragListeView;
import de.jost_net.JVerein.gui.view.ZusatzfeldListeView;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.logging.Logger;

public class JVereinNavigation implements Extension
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

      NavigationItem mitglieder = new JVereinNavigationItem(jverein,
          "Mitglieder", null);

      mitglieder.addChild(new JVereinNavigationItem(mitglieder, "Mitglieder",
          new StartViewAction(MitgliedListeView.class), "user-friends.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN))
      {
        mitglieder
            .addChild(new JVereinNavigationItem(mitglieder, "Nicht-Mitglieder",
                new StartViewAction(NichtMitgliedListeView.class),
                "user-friends.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        mitglieder
            .addChild(new JVereinNavigationItem(mitglieder, "Kursteilnehmer",
                new StartViewAction(KursteilnehmerListeView.class),
                "user-friends.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.FAMILIENBEITRAG))
      {
        mitglieder
            .addChild(new JVereinNavigationItem(mitglieder, "Familienverband",
                new StartViewAction(FamilienbeitragView.class), "users.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ABWEICHENDEZAHLER))
      {
        mitglieder.addChild(
            new JVereinNavigationItem(mitglieder, "Abweichende Zahler",
                new StartViewAction(AbweichendeZahlerView.class), "users.png"));
      }
      mitglieder.addChild(new JVereinNavigationItem(mitglieder, "Sollbuchungen",
          new StartViewAction(SollbuchungListeView.class), "calculator.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        mitglieder.addChild(new JVereinNavigationItem(mitglieder, "Rechnungen",
            new StartViewAction(RechnungListeView.class), "file-invoice.png"));
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        mitglieder.addChild(
            new JVereinNavigationItem(mitglieder, "Spendenbescheinigungen",
                new StartViewAction(SpendenbescheinigungListeView.class),
                "file-invoice.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
      {
        mitglieder.addChild(new JVereinNavigationItem(mitglieder,
            "Zusatzbeträge", new StartViewAction(ZusatzbetragListeView.class),
            "euro-sign.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.WIEDERVORLAGE))
      {
        mitglieder.addChild(new JVereinNavigationItem(mitglieder,
            "Wiedervorlagen", new StartViewAction(WiedervorlageListeView.class),
            "office-calendar.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE))
      {
        mitglieder.addChild(new JVereinNavigationItem(mitglieder, "Lehrgänge",
            new StartViewAction(LehrgangListeView.class),
            "chalkboard-teacher.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
      {
        mitglieder
            .addChild(new JVereinNavigationItem(mitglieder, "Arbeitseinsätze",
                new StartViewAction(ArbeitseinsatzListeView.class),
                "screwdriver.png"));
      }
      jverein.addChild(mitglieder);

      NavigationItem buchfuehrung = new JVereinNavigationItem(jverein,
          "Buchführung", null);
      // Konten
      buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung, "Konten",
          new StartViewAction(KontoListeView.class),
          "system-file-manager.png"));
      buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung,
          "Anfangsbestände", new StartViewAction(AnfangsbestandListeView.class),
          "system-file-manager.png"));
      buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung,
          "Kontensaldo", new StartViewAction(KontoSaldoView.class),
          "system-file-manager.png"));
      // Buchungen
      buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung, "Buchungen",
          new StartViewAction(BuchungListeView.class), "emblem-documents.png"));
      buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung, "Belege",
          new StartViewAction(BelegListeView.class), "emblem-documents.png"));
      buchfuehrung.addChild(
          new JVereinNavigationItem(buchfuehrung, "Buchungsklassensaldo",
              new StartViewAction(BuchungsklasseSaldoView.class),
              "emblem-documents.png"));
      // UstVA
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Umsatzsteuer Voranmeldung",
                new StartViewAction(UmsatzsteuerSaldoView.class), "coins.png"));
      }
      // Projekte
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        buchfuehrung.addChild(new JVereinNavigationItem(buchfuehrung,
            "Projektsaldo", new StartViewAction(ProjektSaldoView.class),
            "screwdriver.png"));
      }
      // Anlagen
      if ((Boolean) Einstellungen.getEinstellung(Property.ANLAGENKONTEN))
      {
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Anlagenbuchungen",
                new StartViewAction(AnlagenbuchungListeView.class),
                "office-chart-area.png"));
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Anlagenverzeichnis",
                new StartViewAction(AnlagenverzeichnisView.class),
                "office-chart-area.png"));
      }
      // Mittelverwendung
      if ((Boolean) Einstellungen.getEinstellung(Property.MITTELVERWENDUNG))
      {
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Mittelverwendung",
                new StartViewAction(MittelverwendungReportView.class),
                "gnome-session-switch.png"));
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Mittelverwendungssaldo",
                new StartViewAction(MittelverwendungSaldoView.class),
                "gnome-session-switch.png"));
      }
      // Jahresabschluss
      buchfuehrung
          .addChild(new JVereinNavigationItem(buchfuehrung, "Jahresabschlüsse",
              new StartViewAction(JahresabschlussListeView.class),
              "office-calendar.png"));

      // Wirtschaftsplan
      if ((Boolean) Einstellungen
          .getEinstellung(Property.WIRTSCHAFTSPLANANZEIGEN))
      {
        buchfuehrung.addChild(
            new JVereinNavigationItem(buchfuehrung, "Wirtschaftsplanung",
                new StartViewAction(WirtschaftsplanListeView.class),
                "x-office-spreadsheet.png"));
      }
      jverein.addChild(buchfuehrung);

      NavigationItem abrechnung = new JVereinNavigationItem(jverein,
          "Abrechnung", null);
      abrechnung
          .addChild(new JVereinNavigationItem(abrechnung, "Abrechnungsläufe",
              new StartViewAction(AbrechnungslaufListeView.class),
              "calculator.png"));
      abrechnung.addChild(new JVereinNavigationItem(abrechnung, "Lastschriften",
          new StartViewAction(LastschriftListeView.class), "lastschrift.png"));
      jverein.addChild(abrechnung);

      NavigationItem mail = new JVereinNavigationItem(jverein, "Druck & Mail",
          null);
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        mail.addChild(new JVereinNavigationItem(mail, "Rechnungen",
            new StartViewAction(RechnungMailView.class), "document-print.png"));
        mail.addChild(new JVereinNavigationItem(mail, "Mahnungen",
            new StartViewAction(MahnungMailView.class), "document-print.png"));
      }
      mail.addChild(new JVereinNavigationItem(mail, "Kontoauszüge",
          new StartViewAction(KontoauszugMailView.class),
          "document-print.png"));
      mail.addChild(new JVereinNavigationItem(mail, "Freie Formulare",
          new StartViewAction(FreiesFormularMailView.class),
          "document-print.png"));
      mail.addChild(new JVereinNavigationItem(mail, "Personalbogen",
          new StartViewAction(PersonalbogenMailView.class),
          "document-print.png"));
      mail.addChild(new JVereinNavigationItem(mail, "Pre-Notification",
          new StartViewAction(PreNotificationMailView.class),
          "document-print.png"));
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        mail.addChild(new JVereinNavigationItem(mail, "Spendenbescheinigungen",
            new StartViewAction(SpendenbescheinigungMailView.class),
            "document-print.png"));
      }
      mail.addChild(new JVereinNavigationItem(mail, "Mails",
          new StartViewAction(MailListeView.class), "envelope-open.png"));
      mail.addChild(new JVereinNavigationItem(mail, "Mail-Vorlagen",
          new StartViewAction(MailVorlageListeView.class),
          "envelope-open.png"));
      jverein.addChild(mail);

      NavigationItem administration = new JVereinNavigationItem(jverein,
          "Administration", null);

      NavigationItem administrationEinstellungen = new JVereinNavigationItem(
          administration, "Einstellungen", null);
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Allgemein",
          new StartViewAction(EinstellungenAllgemeinView.class), "wrench.png"));
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Anzeige",
          new StartViewAction(EinstellungenAnzeigeView.class), "wrench.png"));
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Mitglieder Ansicht",
          new StartViewAction(EinstellungenMitgliedAnsichtView.class),
          "wrench.png"));
      administrationEinstellungen.addChild(
          new JVereinNavigationItem(administrationEinstellungen, "Abrechnung",
              new StartViewAction(EinstellungenAbrechnungView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Verzeichnisse",
          new StartViewAction(EinstellungenVerzeichnisView.class),
          "wrench.png"));
      administrationEinstellungen.addChild(
          new JVereinNavigationItem(administrationEinstellungen, "Vorlagen",
              new StartViewAction(EinstellungenVorlageListeView.class),
              "wrench.png"));
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        administrationEinstellungen.addChild(new JVereinNavigationItem(
            administrationEinstellungen, "Spendenbescheinigungen",
            new StartViewAction(EinstellungenSpendenbescheinigungenView.class),
            "wrench.png"));
      }
      administrationEinstellungen.addChild(
          new JVereinNavigationItem(administrationEinstellungen, "Buchführung",
              new StartViewAction(EinstellungenBuchfuehrungView.class),
              "wrench.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        administrationEinstellungen.addChild(
            new JVereinNavigationItem(administrationEinstellungen, "Rechnungen",
                new StartViewAction(EinstellungenRechnungenView.class),
                "wrench.png"));
      }
      administrationEinstellungen.addChild(
          new JVereinNavigationItem(administrationEinstellungen, "Mail",
              new StartViewAction(EinstellungenMailView.class), "wrench.png"));
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Statistik",
          new StartViewAction(EinstellungenStatistikView.class), "wrench.png"));
      administrationEinstellungen.addChild(new JVereinNavigationItem(
          administrationEinstellungen, "Reports",
          new StartViewAction(EinstellungenReportsView.class), "wrench.png"));
      administration.addChild(administrationEinstellungen);

      NavigationItem einstellungenmitglieder = new JVereinNavigationItem(
          administration, "Mitglieder", null);
      einstellungenmitglieder.addChild(
          new JVereinNavigationItem(einstellungenmitglieder, "Beitragsgruppen",
              new StartViewAction(BeitragsgruppeListeView.class), "clone.png"));
      einstellungenmitglieder.addChild(new JVereinNavigationItem(
          einstellungenmitglieder, "Eigenschaftengruppen",
          new StartViewAction(EigenschaftGruppeListeView.class),
          "document-properties.png"));
      einstellungenmitglieder
          .addChild(new JVereinNavigationItem(einstellungenmitglieder,
              "Eigenschaften", new StartViewAction(EigenschaftListeView.class),
              "document-properties.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        einstellungenmitglieder.addChild(
            new JVereinNavigationItem(einstellungenmitglieder, "Zusatzfelder",
                new StartViewAction(ZusatzfeldListeView.class), "list.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.USELESEFELDER))
      {
        einstellungenmitglieder.addChild(
            new JVereinNavigationItem(einstellungenmitglieder, "Lesefelder",
                new LesefelddefinitionenAction(null), "list.png"));
      }

      einstellungenmitglieder.addChild(
          new JVereinNavigationItem(einstellungenmitglieder, "Formulare",
              new StartViewAction(FormularListeView.class), "columns.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE))
      {
        einstellungenmitglieder.addChild(
            new JVereinNavigationItem(einstellungenmitglieder, "Lehrgangsarten",
                new StartViewAction(LehrgangsartListeView.class),
                "chalkboard-teacher.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN))
      {
        einstellungenmitglieder.addChild(
            new JVereinNavigationItem(einstellungenmitglieder, "Mitgliedstypen",
                new StartViewAction(MitgliedstypListeView.class),
                "user-friends.png"));
      }
      administration.addChild(einstellungenmitglieder);

      NavigationItem einstellungenbuchfuehrung = new JVereinNavigationItem(
          administration, "Buchführung", null);
      einstellungenbuchfuehrung.addChild(new JVereinNavigationItem(
          einstellungenbuchfuehrung, "Buchungsklassen",
          new StartViewAction(BuchungsklasseListeView.class),
          "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new JVereinNavigationItem(
          einstellungenbuchfuehrung, "Buchungsarten",
          new StartViewAction(BuchungsartListeView.class), "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new JVereinNavigationItem(
          einstellungenbuchfuehrung, "Kontenrahmen-Export",
          new KontenrahmenExportAction(), "document-save.png"));
      einstellungenbuchfuehrung.addChild(new JVereinNavigationItem(
          einstellungenbuchfuehrung, "Kontenrahmen-Import",
          new KontenrahmenImportAction(), "file-import.png"));
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        einstellungenbuchfuehrung.addChild(new JVereinNavigationItem(
            einstellungenbuchfuehrung, "Projekte",
            new StartViewAction(ProjektListeView.class), "screwdriver.png"));
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        einstellungenbuchfuehrung.addChild(
            new JVereinNavigationItem(einstellungenbuchfuehrung, "Steuer",
                new StartViewAction(SteuerListeView.class), "coins.png"));
      }
      administration.addChild(einstellungenbuchfuehrung);

      NavigationItem einstellungenerweitert = new JVereinNavigationItem(
          administration, "Erweitert", null);
      einstellungenerweitert.addChild(
          new JVereinNavigationItem(einstellungenerweitert, "Migration",
              new StartViewAction(MigrationView.class), "file-import.png"));
      einstellungenerweitert.addChild(new JVereinNavigationItem(
          einstellungenerweitert, "QIF-Datei-Import",
          new StartViewAction(QIFBuchungsImportView.class), "file-import.png"));
      einstellungenerweitert.addChild(new JVereinNavigationItem(
          einstellungenerweitert, "Datenbank-Bereinigung",
          new StartViewAction(DbBereinigenView.class),
          "placeholder-loading.png"));
      einstellungenerweitert.addChild(new JVereinNavigationItem(
          einstellungenerweitert, "Diagnose-Backup-Export",
          new BackupCreateAction(), "document-save.png"));
      einstellungenerweitert.addChild(new JVereinNavigationItem(
          einstellungenerweitert, "Diagnose-Backup-Import",
          new BackupRestoreAction(), "file-import.png"));
      administration.addChild(einstellungenerweitert);
      jverein.addChild(administration);

      jverein.addChild(new JVereinNavigationItem(jverein, "Dokumentation",
          new DokumentationAction(), "question-circle.png"));
      jverein.addChild(new JVereinNavigationItem(jverein, "Über",
          new AboutAction(), "gtk-info.png"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend navigation");
    }

  }
}
