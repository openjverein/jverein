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
package de.jost_net.JVerein.util;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Variable.AbrechnungslaufParameterMap;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AbrechnungSollbuchungenParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.AnlagenbuchungListeFilterMap;
import de.jost_net.JVerein.Variable.AuswertungArbeitseinsatzFilterMap;
import de.jost_net.JVerein.Variable.AuswertungJubilareFilterMap;
import de.jost_net.JVerein.Variable.AuswertungKursteilnehmerFilterMap;
import de.jost_net.JVerein.Variable.AuswertungMitgliedFilterMap;
import de.jost_net.JVerein.Variable.AuswertungMitgliederstatistikFilterMap;
import de.jost_net.JVerein.Variable.AuswertungNichtMitgliedFilterMap;
import de.jost_net.JVerein.Variable.BuchungListeFilterMap;
import de.jost_net.JVerein.Variable.BuchungsartListeFilterMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SaldoFilterMap;
import de.jost_net.JVerein.Variable.SollbuchungListeFilterMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungListeFilterMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.Variable.WirtschaftsplanParameterMap;
import de.jost_net.JVerein.Variable.ZusatzbetragListeFilterMap;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.control.AbrechnungslaufBuchungenControl;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.ArbeitseinsatzControl;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.BuchungsartControl;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.control.KursteilnehmerControl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Vorlageart;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Vorlage;
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
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          if (mitglied != null)
          {
            map = new MitgliedMap().getMap(mitglied, map);
          }
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
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case FREIES_FORMULAR_DATEINAME:
        case FORMULAR_DATEINAME:
        case FORMULARFELDER_DATEINAME:
          map.put("formular_name", (String) obj);
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = new MitgliedMap().getMap(mitglied, map);
          map.put("formular_name", (String) obj);
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
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
              .getMap((AbrechnungSEPAControl) obj, map);
          break;
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
          map = new AbrechnungSollbuchungenParameterMap()
              .getMap((AbrechnungslaufBuchungenControl) obj, map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
          map = new SollbuchungListeFilterMap().getMap((SollbuchungControl) obj,
              map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = new ZusatzbetragListeFilterMap()
              .getMap((ZusatzbetragControl) obj, map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
          map = new SpendenbescheinigungListeFilterMap()
              .getMap((FilterControl) obj, map);
          break;
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
              .getMap((ArbeitseinsatzControl) obj, map);
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
        default:
          Logger.error("Dateiname Typ nicht implementiert: " + typ.toString());
          return "";
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
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
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
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIES_FORMULAR_DATEINAME:
          map.put("formular_name", "Freies Formular");
          break;
        case FORMULAR_DATEINAME:
          map.put("formular_name", "Rechnung");
          break;
        case FORMULARFELDER_DATEINAME:
          map.put("formular_name", "Rechnung");
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = MitgliedMap.getDummyMap(map);
          map.put("formular_name", "Freies Formular");
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
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
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
          map = AbrechnungSollbuchungenParameterMap.getDummyMap(map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
          map = SollbuchungListeFilterMap.getDummyMap(map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = ZusatzbetragListeFilterMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
          map = SpendenbescheinigungListeFilterMap.getDummyMap(map);
          break;
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
        default:
          Logger.error("Dateiname Typ nicht implementiert: " + typ.toString());
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
      Velocity.init();
      VelocityContext context = new VelocityContext();
      context.put("dateformat", new JVDateFormatTTMMJJJJ());
      context.put("udateformat", new UniversalDateFormat());
      context.put("decimalformat", Einstellungen.DECIMALFORMAT);
      VarTools.add(context, map);
      StringWriter wdateiname = new StringWriter();
      String in = inString.replaceAll("-\\$", "\\'\\#\\'\\$");
      Velocity.evaluate(context, wdateiname, "LOG", in);
      String str = wdateiname.toString();
      str = str.replaceAll("\\'\\#\\'", "-");
      if (dateiname)
      {
        str = str.replaceAll("[^a-zA-Z0-9äöüÄÖÜß_\\-\\. ]", "_");
      }
      return str;
    }
    catch (Exception e)
    {
      Logger.error(
          "Format Fehler bei der Dateinamen Ersetzung: " + e.getMessage());
      return "Format Fehler bei der Dateinamen Ersetzung.";
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
