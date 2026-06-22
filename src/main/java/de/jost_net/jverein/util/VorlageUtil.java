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
package de.jost_net.jverein.util;

import java.rmi.RemoteException;
import java.util.Map;

import de.jost_net.jverein.Einstellungen;
import de.jost_net.jverein.gui.control.AbstractSaldoControl;
import de.jost_net.jverein.gui.control.ArbeitseinsatzAbrechnungControl;
import de.jost_net.jverein.gui.control.BuchungsControl;
import de.jost_net.jverein.gui.control.BuchungsartControl;
import de.jost_net.jverein.gui.control.FilterControl;
import de.jost_net.jverein.gui.control.JahresabschlussControl;
import de.jost_net.jverein.gui.control.KursteilnehmerControl;
import de.jost_net.jverein.gui.control.MitgliedControl;
import de.jost_net.jverein.gui.control.ProjektControl;
import de.jost_net.jverein.gui.control.SollbuchungControl;
import de.jost_net.jverein.gui.control.WirtschaftsplanControl;
import de.jost_net.jverein.gui.control.ZusatzbetragControl;
import de.jost_net.jverein.io.AbrechnungSEPAParam;
import de.jost_net.jverein.io.VelocityTool;
import de.jost_net.jverein.io.Zeichen;
import de.jost_net.jverein.keys.VorlageTyp;
import de.jost_net.jverein.keys.Vorlageart;
import de.jost_net.jverein.rmi.Abrechnungslauf;
import de.jost_net.jverein.rmi.Lastschrift;
import de.jost_net.jverein.rmi.Mitglied;
import de.jost_net.jverein.rmi.Rechnung;
import de.jost_net.jverein.rmi.Spendenbescheinigung;
import de.jost_net.jverein.rmi.Vorlage;
import de.jost_net.jverein.variable.AbrechnungSollbuchungenParameterMap;
import de.jost_net.jverein.variable.AbrechnungslaufListeFilterMap;
import de.jost_net.jverein.variable.AbrechnungslaufParameterMap;
import de.jost_net.jverein.variable.AllgemeineMap;
import de.jost_net.jverein.variable.AnfangsbestandListeFilterMap;
import de.jost_net.jverein.variable.AnlagenbuchungListeFilterMap;
import de.jost_net.jverein.variable.ArbeitseinsatzListeFilterMap;
import de.jost_net.jverein.variable.AuswertungArbeitseinsatzFilterMap;
import de.jost_net.jverein.variable.AuswertungJubilareFilterMap;
import de.jost_net.jverein.variable.AuswertungKursteilnehmerFilterMap;
import de.jost_net.jverein.variable.AuswertungMitgliedFilterMap;
import de.jost_net.jverein.variable.AuswertungMitgliederstatistikFilterMap;
import de.jost_net.jverein.variable.AuswertungNichtMitgliedFilterMap;
import de.jost_net.jverein.variable.BuchungListeFilterMap;
import de.jost_net.jverein.variable.BuchungsartListeFilterMap;
import de.jost_net.jverein.variable.JahresabschlussListeFilterMap;
import de.jost_net.jverein.variable.KontoListeFilterMap;
import de.jost_net.jverein.variable.KursteilnehmerListeFilterMap;
import de.jost_net.jverein.variable.LastschriftListeFilterMap;
import de.jost_net.jverein.variable.LastschriftMap;
import de.jost_net.jverein.variable.LehrgangListeFilterMap;
import de.jost_net.jverein.variable.MailListeFilterMap;
import de.jost_net.jverein.variable.MitgliedListeFilterMap;
import de.jost_net.jverein.variable.MitgliedMap;
import de.jost_net.jverein.variable.NichtMitgliedListeFilterMap;
import de.jost_net.jverein.variable.ProjektListeFilterMap;
import de.jost_net.jverein.variable.RechnungListeFilterMap;
import de.jost_net.jverein.variable.RechnungMap;
import de.jost_net.jverein.variable.SaldoFilterMap;
import de.jost_net.jverein.variable.SollbuchungListeFilterMap;
import de.jost_net.jverein.variable.SpendenbescheinigungListeFilterMap;
import de.jost_net.jverein.variable.SpendenbescheinigungMap;
import de.jost_net.jverein.variable.WiedervorlageListeFilterMap;
import de.jost_net.jverein.variable.WirtschaftsplanParameterMap;
import de.jost_net.jverein.variable.ZusatzbetragListeFilterMap;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;

public class VorlageUtil
{

  // Namen Generierung aus Vorlagen Muster
  public static String getName(VorlageTyp typ)
  {
    return getName(typ, null, null);
  }

  public static String getName(VorlageTyp typ, Object obj)
  {
    return getName(typ, obj, null);
  }

  public static String getName(VorlageTyp typ, Object obj, Mitglied mitglied)
  {
    Map<String, Object> map = null;
    String muster = "";
    try
    {
      map = new AllgemeineMap().getMap(null);
      muster = getVorlageMuster(typ);
      switch (typ)
      {
        case ABRECHNUNGSLAEUFE_DATEINAME:
        case ABRECHNUNGSLAEUFE_SUBTITEL:
        case ABRECHNUNGSLAEUFE_TITEL:
          map = new AbrechnungslaufListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case ANFANGSBESTAENDE_DATEINAME:
        case ANFANGSBESTAENDE_SUBTITEL:
        case ANFANGSBESTAENDE_TITEL:
          map = new AnfangsbestandListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case KONTEN_DATEINAME:
        case KONTEN_SUBTITEL:
        case KONTEN_TITEL:
          map = new KontoListeFilterMap().getMap((FilterControl) obj, map);
          break;
        case KURSTEILNEHMER_DATEINAME:
        case KURSTEILNEHMER_SUBTITEL:
        case KURSTEILNEHMER_TITEL:
          map = new KursteilnehmerListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case LASTSCHRIFTEN_DATEINAME:
        case LASTSCHRIFTEN_SUBTITEL:
        case LASTSCHRIFTEN_TITEL:
          map = new LastschriftListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case LEHRGAENGE_DATEINAME:
        case LEHRGAENGE_SUBTITEL:
        case LEHRGAENGE_TITEL:
          map = new LehrgangListeFilterMap().getMap((FilterControl) obj, map);
          break;
        case MAILS_DATEINAME:
        case MAILS_SUBTITEL:
        case MAILS_TITEL:
          map = new MailListeFilterMap().getMap((FilterControl) obj, map);
          break;
        case RECHNUNGEN_DATEINAME:
        case RECHNUNGEN_SUBTITEL:
        case RECHNUNGEN_TITEL:
          map = new RechnungListeFilterMap().getMap((FilterControl) obj, map);
          break;
        case WIEDERVORLAGEN_DATEINAME:
        case WIEDERVORLAGEN_SUBTITEL:
        case WIEDERVORLAGEN_TITEL:
          map = new WiedervorlageListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          if (mitglied != null)
          {
            map = new MitgliedMap().getMap(mitglied, map);
          }
          break;
        case MITGLIEDER_DATEINAME:
        case MITGLIEDER_TITEL:
        case MITGLIEDER_SUBTITEL:
          map = new MitgliedListeFilterMap().getMap((FilterControl) obj, map);
          break;
        case NICHT_MITGLIEDER_DATEINAME:
        case NICHT_MITGLIEDER_TITEL:
        case NICHT_MITGLIEDER_SUBTITEL:
          map = new NichtMitgliedListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          // Ein Dokument pro Mitglied
          map = new RechnungMap().getMap((Rechnung) obj, map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case PRENOTIFICATION_MITGLIED_DATEINAME:
          map = new LastschriftMap().getMap((Lastschrift) obj, map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case PRENOTIFICATION_KURSTEILNEHMER_DATEINAME:
          map = new LastschriftMap().getMap((Lastschrift) obj, map);
          break;
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
        case MITGLIED_ZUSATZBETRAEGE_DATEINAME:
        case MITGLIED_ZUSATZBETRAEGE_TITEL:
        case MITGLIED_ZUSATZBETRAEGE_SUBTITEL:
        case MITGLIED_WIEDERVORLAGEN_DATEINAME:
        case MITGLIED_WIEDERVORLAGEN_TITEL:
        case MITGLIED_WIEDERVORLAGEN_SUBTITEL:
        case MITGLIED_MAILS_DATEINAME:
        case MITGLIED_MAILS_TITEL:
        case MITGLIED_MAILS_SUBTITEL:
        case MITGLIED_LEHRGAENGE_DATEINAME:
        case MITGLIED_LEHRGAENGE_TITEL:
        case MITGLIED_LEHRGAENGE_SUBTITEL:
        case MITGLIED_LESEFELDER_DATEINAME:
        case MITGLIED_LESEFELDER_TITEL:
        case MITGLIED_LESEFELDER_SUBTITEL:
        case MITGLIED_ARBEITSEINSAETZE_DATEINAME:
        case MITGLIED_ARBEITSEINSAETZE_TITEL:
        case MITGLIED_ARBEITSEINSAETZE_SUBTITEL:
        case MITGLIED_DOKUMENTE_DATEINAME:
        case MITGLIED_DOKUMENTE_TITEL:
        case MITGLIED_DOKUMENTE_SUBTITEL:
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case FREIES_FORMULAR_DATEINAME:
        case FORMULAR_DATEINAME:
        case FORMULARFELDER_DATEINAME:
        case FORMULARFELDER_TITEL:
        case FORMULARFELDER_SUBTITEL:
          map.put("formular_name", (String) obj);
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = new MitgliedMap().getMap(mitglied, map);
          map.put("formular_name", (String) obj);
          break;
        case KONTENSALDO_DATEINAME:
        case KONTENSALDO_TITEL:
        case KONTENSALDO_SUBTITEL:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_TITEL:
        case BUCHUNGSKLASSENSALDO_SUBTITEL:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_TITEL:
        case UMSATZSTEUER_VORANMELDUNG_SUBTITEL:
        case PROJEKTSALDO_DATEINAME:
        case PROJEKTSALDO_TITEL:
        case PROJEKTSALDO_SUBTITEL:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case ANLAGENVERZEICHNIS_TITEL:
        case ANLAGENVERZEICHNIS_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_TITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
        case MITTELVERWENDUNGSSALDO_TITEL:
        case MITTELVERWENDUNGSSALDO_SUBTITEL:
          map = new SaldoFilterMap().getMap((AbstractSaldoControl) obj, map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = new AbrechnungslaufParameterMap()
              .getMap((AbrechnungSEPAParam) obj, map);
          break;
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_BUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL:
          map = new AbrechnungSollbuchungenParameterMap()
              .getMap((Abrechnungslauf) obj, map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
        case SOLLBUCHUNGEN_TITEL:
        case SOLLBUCHUNGEN_SUBTITEL:
          map = new SollbuchungListeFilterMap().getMap((SollbuchungControl) obj,
              map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = new ZusatzbetragListeFilterMap()
              .getMap((ZusatzbetragControl) obj, map);
          break;
        case ARBEITSEINSAETZE_DATEINAME:
        case ARBEITSEINSAETZE_TITEL:
        case ARBEITSEINSAETZE_SUBTITEL:
          map = new ArbeitseinsatzListeFilterMap().getMap((FilterControl) obj,
              map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
          map = new SpendenbescheinigungListeFilterMap()
              .getMap((FilterControl) obj, map);
          break;
        case BUCHUNGEN_DATEINAME:
        case BUCHUNGEN_TITEL:
        case BUCHUNGEN_SUBTITEL:
        case BUCHUNGSJOURNAL_DATEINAME:
        case BUCHUNGSJOURNAL_TITEL:
        case BUCHUNGSJOURNAL_SUBTITEL:
        case EINZELBUCHUNGEN_DATEINAME:
        case EINZELBUCHUNGEN_TITEL:
        case EINZELBUCHUNGEN_SUBTITEL:
        case SUMMENBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_TITEL:
        case SUMMENBUCHUNGEN_SUBTITEL:
        case CSVBUCHUNGEN_DATEINAME:
          map = new BuchungListeFilterMap().getMap((BuchungsControl) obj, map);
          break;
        case ANLAGEN_BUCHUNGEN_DATEINAME:
        case ANLAGEN_BUCHUNGEN_TITEL:
        case ANLAGEN_BUCHUNGEN_SUBTITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_BUCHUNGSJOURNAL_TITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_TITEL:
        case ANLAGEN_EINZELBUCHUNGEN_SUBTITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_TITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = new AnlagenbuchungListeFilterMap().getMap((BuchungsControl) obj,
              map);
          break;
        case JAHRESABSCHLUSS_DATEINAME:
        case JAHRESABSCHLUSS_TITEL:
        case JAHRESABSCHLUSS_SUBTITEL:
          map = new JahresabschlussListeFilterMap()
              .getMap((JahresabschlussControl) obj, map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
        case AUSWERTUNG_MITGLIED_TITEL:
          map = new AuswertungMitgliedFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
        case AUSWERTUNG_NICHT_MITGLIED_TITEL:
          map = new AuswertungNichtMitgliedFilterMap()
              .getMap((MitgliedControl) obj, map);
          break;
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
        case AUSWERTUNG_KURSTEILNEHMER_TITEL:
        case AUSWERTUNG_KURSTEILNEHMER_SUBTITEL:
          map = new AuswertungKursteilnehmerFilterMap()
              .getMap((KursteilnehmerControl) obj, map);
          break;
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL:
          map = new AuswertungMitgliederstatistikFilterMap()
              .getMap((MitgliedControl) obj, map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_ALTERSJUBILARE_TITEL:
        case AUSWERTUNG_ALTERSJUBILARE_SUBTITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL:
          map = new AuswertungJubilareFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
        case AUSWERTUNG_ARBEITSEINSAETZE_TITEL:
        case AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL:
          map = new AuswertungArbeitseinsatzFilterMap()
              .getMap((ArbeitseinsatzAbrechnungControl) obj, map);
          break;
        case BUCHUNGSARTEN_DATEINAME:
        case BUCHUNGSARTEN_TITEL:
        case BUCHUNGSARTEN_SUBTITEL:
          map = new BuchungsartListeFilterMap().getMap((BuchungsartControl) obj,
              map);
          break;
        case WIRTSCHAFTSPLAN_DATEINAME:
        case WIRTSCHAFTSPLAN_TITEL:
        case WIRTSCHAFTSPLAN_SUBTITEL:
          map = new WirtschaftsplanParameterMap()
              .getMap((WirtschaftsplanControl) obj, map);
          break;
        case PROJEKTE_DATEINAME:
        case PROJEKTE_TITEL:
        case PROJEKTE_SUBTITEL:
          map = new ProjektListeFilterMap().getMap((ProjektControl) obj, map);
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case GUTSCHRIFT_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
        case BUCHUNGSKLASSEN_DATEINAME:
        case BUCHUNGSKLASSEN_TITEL:
        case BUCHUNGSKLASSEN_SUBTITEL:
        case STEUERN_DATEINAME:
        case STEUERN_TITEL:
        case STEUERN_SUBTITEL:
        case BEITRAGSGRUPPEN_DATEINAME:
        case BEITRAGSGRUPPEN_TITEL:
        case BEITRAGSGRUPPEN_SUBTITEL:
        case EIGENSCHAFTENGRUPPEN_DATEINAME:
        case EIGENSCHAFTENGRUPPEN_TITEL:
        case EIGENSCHAFTENGRUPPEN_SUBTITEL:
        case EIGENSCHAFTEN_DATEINAME:
        case EIGENSCHAFTEN_TITEL:
        case EIGENSCHAFTEN_SUBTITEL:
        case ZUSATZFELDER_DATEINAME:
        case ZUSATZFELDER_TITEL:
        case ZUSATZFELDER_SUBTITEL:
        case LESEFELDER_DATEINAME:
        case LESEFELDER_TITEL:
        case LESEFELDER_SUBTITEL:
        case FORMULARE_DATEINAME:
        case FORMULARE_TITEL:
        case FORMULARE_SUBTITEL:
        case LEHRGANGSARTEN_DATEINAME:
        case LEHRGANGSARTEN_TITEL:
        case LEHRGANGSARTEN_SUBTITEL:
        case MITGLIEDSTYPEN_DATEINAME:
        case MITGLIEDSTYPEN_TITEL:
        case MITGLIEDSTYPEN_SUBTITEL:
        case WIRTSCHAFTSPLAENE_DATEINAME:
        case WIRTSCHAFTSPLAENE_TITEL:
        case WIRTSCHAFTSPLAENE_SUBTITEL:
        case JAHRESABSCHLUESSE_DATEINAME:
        case JAHRESABSCHLUESSE_TITEL:
        case JAHRESABSCHLUESSE_SUBTITEL:
        case ZUSATZBETRAEGE_VORLAGEN_DATEINAME:
        case ZUSATZBETRAEGE_VORLAGEN_TITEL:
        case ZUSATZBETRAEGE_VORLAGEN_SUBTITEL:
        case MAILVORLAGEN_DATEINAME:
        case MAILVORLAGEN_TITEL:
        case MAILVORLAGEN_SUBTITEL:
        case ABWEICHENDE_ZAHLER_DATEINAME:
        case ABWEICHENDE_ZAHLER_SUBTITEL:
        case ABWEICHENDE_ZAHLER_TITEL:
        case FAMILIENVERBAND_DATEINAME:
        case FAMILIENVERBAND_SUBTITEL:
        case FAMILIENVERBAND_TITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return translate(map, muster,
        typ.getArtkey() == Vorlageart.DATEINAME.getKey());
  }

  // Dummy Namen Generierung aus Vorlagen Muster
  public static String getDummyName(VorlageTyp typ)
  {
    String muster = "";
    try
    {
      muster = getVorlageMuster(typ);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return getDummyName(typ, muster);
  }

  // Dummy Namen Generierung aus Vorlagen Muster
  public static String getDummyName(VorlageTyp typ, String muster)
  {
    return translate(getDummyMap(typ), muster,
        typ.getArtkey() == Vorlageart.DATEINAME.getKey());
  }

  public static Map<String, Object> getDummyMap(VorlageTyp typ)
  {
    Map<String, Object> map = null;
    try
    {
      map = new AllgemeineMap().getMap(null);
      switch (typ)
      {
        case ABRECHNUNGSLAEUFE_DATEINAME:
        case ABRECHNUNGSLAEUFE_SUBTITEL:
        case ABRECHNUNGSLAEUFE_TITEL:
          map = AbrechnungslaufListeFilterMap.getDummyMap(map);
          break;
        case ANFANGSBESTAENDE_DATEINAME:
        case ANFANGSBESTAENDE_SUBTITEL:
        case ANFANGSBESTAENDE_TITEL:
          map = AnfangsbestandListeFilterMap.getDummyMap(map);
          break;
        case KONTEN_DATEINAME:
        case KONTEN_SUBTITEL:
        case KONTEN_TITEL:
          map = KontoListeFilterMap.getDummyMap(map);
          break;
        case KURSTEILNEHMER_DATEINAME:
        case KURSTEILNEHMER_SUBTITEL:
        case KURSTEILNEHMER_TITEL:
          map = KursteilnehmerListeFilterMap.getDummyMap(map);
          break;
        case LASTSCHRIFTEN_DATEINAME:
        case LASTSCHRIFTEN_SUBTITEL:
        case LASTSCHRIFTEN_TITEL:
          map = LastschriftListeFilterMap.getDummyMap(map);
          break;
        case LEHRGAENGE_DATEINAME:
        case LEHRGAENGE_SUBTITEL:
        case LEHRGAENGE_TITEL:
          map = LehrgangListeFilterMap.getDummyMap(map);
          break;
        case MAILS_DATEINAME:
        case MAILS_SUBTITEL:
        case MAILS_TITEL:
          map = MailListeFilterMap.getDummyMap(map);
          break;
        case RECHNUNGEN_DATEINAME:
        case RECHNUNGEN_SUBTITEL:
        case RECHNUNGEN_TITEL:
          map = RechnungListeFilterMap.getDummyMap(map);
          break;
        case WIEDERVORLAGEN_DATEINAME:
        case WIEDERVORLAGEN_SUBTITEL:
        case WIEDERVORLAGEN_TITEL:
          map = WiedervorlageListeFilterMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case MITGLIEDER_DATEINAME:
        case MITGLIEDER_TITEL:
        case MITGLIEDER_SUBTITEL:
          map = MitgliedListeFilterMap.getDummyMap(map);
          break;
        case NICHT_MITGLIEDER_DATEINAME:
        case NICHT_MITGLIEDER_TITEL:
        case NICHT_MITGLIEDER_SUBTITEL:
          map = NichtMitgliedListeFilterMap.getDummyMap(map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          map = RechnungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case PRENOTIFICATION_MITGLIED_DATEINAME:
          map = LastschriftMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case PRENOTIFICATION_KURSTEILNEHMER_DATEINAME:
          map = LastschriftMap.getDummyMap(map);
          break;
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
        case MITGLIED_ZUSATZBETRAEGE_DATEINAME:
        case MITGLIED_ZUSATZBETRAEGE_TITEL:
        case MITGLIED_ZUSATZBETRAEGE_SUBTITEL:
        case MITGLIED_WIEDERVORLAGEN_DATEINAME:
        case MITGLIED_WIEDERVORLAGEN_TITEL:
        case MITGLIED_WIEDERVORLAGEN_SUBTITEL:
        case MITGLIED_MAILS_DATEINAME:
        case MITGLIED_MAILS_TITEL:
        case MITGLIED_MAILS_SUBTITEL:
        case MITGLIED_LEHRGAENGE_DATEINAME:
        case MITGLIED_LEHRGAENGE_TITEL:
        case MITGLIED_LEHRGAENGE_SUBTITEL:
        case MITGLIED_LESEFELDER_DATEINAME:
        case MITGLIED_LESEFELDER_TITEL:
        case MITGLIED_LESEFELDER_SUBTITEL:
        case MITGLIED_ARBEITSEINSAETZE_DATEINAME:
        case MITGLIED_ARBEITSEINSAETZE_TITEL:
        case MITGLIED_ARBEITSEINSAETZE_SUBTITEL:
        case MITGLIED_DOKUMENTE_DATEINAME:
        case MITGLIED_DOKUMENTE_TITEL:
        case MITGLIED_DOKUMENTE_SUBTITEL:
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIES_FORMULAR_DATEINAME:
          map.put("formular_name", "Freies Formular");
          break;
        case FORMULAR_DATEINAME:
          map.put("formular_name", "Rechnung");
          break;
        case FORMULARFELDER_DATEINAME:
        case FORMULARFELDER_TITEL:
        case FORMULARFELDER_SUBTITEL:
          map.put("formular_name", "Rechnung");
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = MitgliedMap.getDummyMap(map);
          map.put("formular_name", "Freies Formular");
          break;
        case KONTENSALDO_DATEINAME:
        case KONTENSALDO_TITEL:
        case KONTENSALDO_SUBTITEL:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_TITEL:
        case BUCHUNGSKLASSENSALDO_SUBTITEL:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_TITEL:
        case UMSATZSTEUER_VORANMELDUNG_SUBTITEL:
        case PROJEKTSALDO_DATEINAME:
        case PROJEKTSALDO_TITEL:
        case PROJEKTSALDO_SUBTITEL:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case ANLAGENVERZEICHNIS_TITEL:
        case ANLAGENVERZEICHNIS_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_TITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
        case MITTELVERWENDUNGSSALDO_TITEL:
        case MITTELVERWENDUNGSSALDO_SUBTITEL:
          map = SaldoFilterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = AbrechnungslaufParameterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_BUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL:
          map = AbrechnungSollbuchungenParameterMap.getDummyMap(map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
        case SOLLBUCHUNGEN_TITEL:
        case SOLLBUCHUNGEN_SUBTITEL:
          map = SollbuchungListeFilterMap.getDummyMap(map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = ZusatzbetragListeFilterMap.getDummyMap(map);
          break;
        case ARBEITSEINSAETZE_DATEINAME:
        case ARBEITSEINSAETZE_TITEL:
        case ARBEITSEINSAETZE_SUBTITEL:
          map = ArbeitseinsatzListeFilterMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
          map = SpendenbescheinigungListeFilterMap.getDummyMap(map);
          break;
        case BUCHUNGEN_DATEINAME:
        case BUCHUNGEN_TITEL:
        case BUCHUNGEN_SUBTITEL:
        case BUCHUNGSJOURNAL_DATEINAME:
        case BUCHUNGSJOURNAL_TITEL:
        case BUCHUNGSJOURNAL_SUBTITEL:
        case EINZELBUCHUNGEN_DATEINAME:
        case EINZELBUCHUNGEN_TITEL:
        case EINZELBUCHUNGEN_SUBTITEL:
        case SUMMENBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_TITEL:
        case SUMMENBUCHUNGEN_SUBTITEL:
        case CSVBUCHUNGEN_DATEINAME:
          map = BuchungListeFilterMap.getDummyMap(map);
          break;
        case ANLAGEN_BUCHUNGEN_DATEINAME:
        case ANLAGEN_BUCHUNGEN_TITEL:
        case ANLAGEN_BUCHUNGEN_SUBTITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_BUCHUNGSJOURNAL_TITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_TITEL:
        case ANLAGEN_EINZELBUCHUNGEN_SUBTITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_TITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = AnlagenbuchungListeFilterMap.getDummyMap(map);
          break;
        case JAHRESABSCHLUSS_DATEINAME:
        case JAHRESABSCHLUSS_TITEL:
        case JAHRESABSCHLUSS_SUBTITEL:
          map = JahresabschlussListeFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
        case AUSWERTUNG_MITGLIED_TITEL:
          map = AuswertungMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
        case AUSWERTUNG_NICHT_MITGLIED_TITEL:
          map = AuswertungNichtMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
        case AUSWERTUNG_KURSTEILNEHMER_TITEL:
        case AUSWERTUNG_KURSTEILNEHMER_SUBTITEL:
          map = AuswertungKursteilnehmerFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL:
          map = AuswertungMitgliederstatistikFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_ALTERSJUBILARE_TITEL:
        case AUSWERTUNG_ALTERSJUBILARE_SUBTITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL:
          map = AuswertungJubilareFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
        case AUSWERTUNG_ARBEITSEINSAETZE_TITEL:
        case AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL:
          map = AuswertungArbeitseinsatzFilterMap.getDummyMap(map);
          break;
        case BUCHUNGSARTEN_DATEINAME:
        case BUCHUNGSARTEN_TITEL:
        case BUCHUNGSARTEN_SUBTITEL:
          map = BuchungsartListeFilterMap.getDummyMap(map);
          break;
        case WIRTSCHAFTSPLAN_DATEINAME:
        case WIRTSCHAFTSPLAN_TITEL:
        case WIRTSCHAFTSPLAN_SUBTITEL:
          map = WirtschaftsplanParameterMap.getDummyMap(map);
          break;
        case PROJEKTE_DATEINAME:
        case PROJEKTE_TITEL:
        case PROJEKTE_SUBTITEL:
          map = ProjektListeFilterMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case GUTSCHRIFT_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
        case BUCHUNGSKLASSEN_DATEINAME:
        case BUCHUNGSKLASSEN_TITEL:
        case BUCHUNGSKLASSEN_SUBTITEL:
        case STEUERN_DATEINAME:
        case STEUERN_TITEL:
        case STEUERN_SUBTITEL:
        case BEITRAGSGRUPPEN_DATEINAME:
        case BEITRAGSGRUPPEN_TITEL:
        case BEITRAGSGRUPPEN_SUBTITEL:
        case EIGENSCHAFTENGRUPPEN_DATEINAME:
        case EIGENSCHAFTENGRUPPEN_TITEL:
        case EIGENSCHAFTENGRUPPEN_SUBTITEL:
        case EIGENSCHAFTEN_DATEINAME:
        case EIGENSCHAFTEN_TITEL:
        case EIGENSCHAFTEN_SUBTITEL:
        case ZUSATZFELDER_DATEINAME:
        case ZUSATZFELDER_TITEL:
        case ZUSATZFELDER_SUBTITEL:
        case LESEFELDER_DATEINAME:
        case LESEFELDER_TITEL:
        case LESEFELDER_SUBTITEL:
        case FORMULARE_DATEINAME:
        case FORMULARE_TITEL:
        case FORMULARE_SUBTITEL:
        case LEHRGANGSARTEN_DATEINAME:
        case LEHRGANGSARTEN_TITEL:
        case LEHRGANGSARTEN_SUBTITEL:
        case MITGLIEDSTYPEN_DATEINAME:
        case MITGLIEDSTYPEN_TITEL:
        case MITGLIEDSTYPEN_SUBTITEL:
        case WIRTSCHAFTSPLAENE_DATEINAME:
        case WIRTSCHAFTSPLAENE_TITEL:
        case WIRTSCHAFTSPLAENE_SUBTITEL:
        case JAHRESABSCHLUESSE_DATEINAME:
        case JAHRESABSCHLUESSE_TITEL:
        case JAHRESABSCHLUESSE_SUBTITEL:
        case ZUSATZBETRAEGE_VORLAGEN_DATEINAME:
        case ZUSATZBETRAEGE_VORLAGEN_TITEL:
        case ZUSATZBETRAEGE_VORLAGEN_SUBTITEL:
        case MAILVORLAGEN_DATEINAME:
        case MAILVORLAGEN_TITEL:
        case MAILVORLAGEN_SUBTITEL:
        case ABWEICHENDE_ZAHLER_DATEINAME:
        case ABWEICHENDE_ZAHLER_SUBTITEL:
        case ABWEICHENDE_ZAHLER_TITEL:
        case FAMILIENVERBAND_DATEINAME:
        case FAMILIENVERBAND_SUBTITEL:
        case FAMILIENVERBAND_TITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
      }
    }
    catch (RemoteException e)
    {
      //
    }
    return map;
  }

  public static String translate(Map<String, Object> map, String inString,
      boolean dateiname)
  {
    try
    {
      String in = inString.replaceAll("-\\$", "\\'\\#\\'\\$");
      String str = VelocityTool.eval(map, in);
      str = str.replaceAll("\\'\\#\\'", "-");
      if (dateiname)
      {
        str = Zeichen.convert(str);
        str = str.replaceAll("[^a-zA-Z0-9_\\-\\. ]", "_");
      }
      return str;
    }
    catch (Exception e)
    {
      Logger.error("Format Fehler bei der Dateinamen/Titel Ersetzung: "
          + e.getMessage());
      return "Format Fehler bei der Dateinamen/Titel Ersetzung.";
    }

  }

  public static String getVorlageMuster(VorlageTyp typ) throws RemoteException
  {
    DBIterator<Vorlage> vorlagen = Einstellungen.getDBService()
        .createList(Vorlage.class);
    vorlagen.addFilter(Vorlage.KEY + " = ?", typ.getKey());
    if (vorlagen.hasNext())
    {
      return vorlagen.next().getMuster();
    }
    return typ.getDefault();
  }
}
