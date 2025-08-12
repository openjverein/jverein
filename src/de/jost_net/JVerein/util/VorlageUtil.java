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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AbrechnungParameterMap;
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
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SaldoFilterMap;
import de.jost_net.JVerein.Variable.SollbuchungListeFilterMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungListeFilterMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
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
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.keys.VorlageTyp;
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
        case SPENDENBESCHEINIGUNG_DATEINAME:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          // Ein Dokument pro Mitglied
          map = new RechnungMap().getMap((Rechnung) obj, map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case PRENOTIFICATION_MITGLIED_DATEINAME:
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case VCARD_MITGLIED_DATEINAME:
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
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        case KONTENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case PROJEKTSALDO_DATEINAME:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
          map = new SaldoFilterMap().getMap((AbstractSaldoControl) obj, map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = new AbrechnungParameterMap().getMap((AbrechnungSEPAControl) obj,
              map);
          break;
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
          map = new AbrechnungSollbuchungenParameterMap()
              .getMap((AbrechnungslaufBuchungenControl) obj, map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
          map = new SollbuchungListeFilterMap().getMap((SollbuchungControl) obj,
              map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
          map = new ZusatzbetragListeFilterMap()
              .getMap((ZusatzbetragControl) obj, map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
          map = new SpendenbescheinigungListeFilterMap()
              .getMap((FilterControl) obj, map);
          break;
        case BUCHUNGSJOURNAL_DATEINAME:
        case EINZELBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_DATEINAME:
        case CSVBUCHUNGEN_DATEINAME:
          map = new BuchungListeFilterMap().getMap((BuchungsControl) obj, map);
          break;
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = new AnlagenbuchungListeFilterMap().getMap((BuchungsControl) obj,
              map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
          map = new AuswertungMitgliedFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
          map = new AuswertungNichtMitgliedFilterMap()
              .getMap((MitgliedControl) obj, map);
          break;
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
          map = new AuswertungKursteilnehmerFilterMap()
              .getMap((KursteilnehmerControl) obj, map);
          break;
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
          map = new AuswertungMitgliederstatistikFilterMap()
              .getMap((MitgliedControl) obj, map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
          map = new AuswertungJubilareFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
          map = new AuswertungArbeitseinsatzFilterMap()
              .getMap((ArbeitseinsatzControl) obj, map);
          break;
        case BUCHUNGSARTEN_DATEINAME:
          map = new BuchungsartListeFilterMap().getMap((BuchungsartControl) obj,
              map);
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
    return translate(map, muster);
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
    return translate(getDummyMap(typ), muster);
  }

  public static Map<String, Object> getDummyMap(VorlageTyp typ)
  {
    Map<String, Object> map = null;
    try
    {
      map = new AllgemeineMap().getMap(null);
      switch (typ)
      {
        case SPENDENBESCHEINIGUNG_DATEINAME:
          map = SpendenbescheinigungMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          map = RechnungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case PRENOTIFICATION_MITGLIED_DATEINAME:
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case VCARD_MITGLIED_DATEINAME:
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
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        case KONTENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case PROJEKTSALDO_DATEINAME:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
          map = SaldoFilterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = AbrechnungParameterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
          map = AbrechnungSollbuchungenParameterMap.getDummyMap(map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
          map = SollbuchungListeFilterMap.getDummyMap(map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
          map = ZusatzbetragListeFilterMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
          map = SpendenbescheinigungListeFilterMap.getDummyMap(map);
          break;
        case BUCHUNGSJOURNAL_DATEINAME:
        case EINZELBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_DATEINAME:
        case CSVBUCHUNGEN_DATEINAME:
          map = BuchungListeFilterMap.getDummyMap(map);
          break;
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = AnlagenbuchungListeFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
          map = AuswertungMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
          map = AuswertungNichtMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
          map = AuswertungKursteilnehmerFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
          map = AuswertungMitgliederstatistikFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
          map = AuswertungJubilareFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
          map = AuswertungArbeitseinsatzFilterMap.getDummyMap(map);
          break;
        case BUCHUNGSARTEN_DATEINAME:
          map = BuchungsartListeFilterMap.getDummyMap(map);
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

  public static String translate(Map<String, Object> map, String inString)
  {
    Velocity.init();
    VelocityContext context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("udateformat", new UniversalDateFormat());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    VarTools.add(context, map);
    StringWriter wdateiname = new StringWriter();
    String in = inString.replaceAll("-\\$", " \\$");
    Velocity.evaluate(context, wdateiname, "LOG", in);
    String str = wdateiname.toString();
    str = str.replaceAll(" ", "-");
    return str;
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
    return "";
  }
}
