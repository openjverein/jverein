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
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.GV.generators.ISEPAGenerator;
import org.kapott.hbci.GV.generators.SEPAGeneratorFactory;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.jost_net.OBanToo.StringLatin.Zeichen;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.SepaUeberweisungMerge;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class Ueberweisung
{
  private Double betrag = null;

  public Ueberweisung(Double betrag)
  {
    this.betrag = betrag;
  }

  public int write(ArrayList<Lastschrift> lastschriften, File file, Date faell,
      UeberweisungAusgabe ausgabe, String verwendungszweck) throws Exception
  {
    Velocity.init();
    switch (ausgabe)
    {
      case SEPA_DATEI:
        return dateiausgabe(lastschriften, file, faell, ausgabe,
            verwendungszweck);

      case HIBISCUS:
        return hibiscusausgabe(lastschriften, file, faell, ausgabe,
            verwendungszweck);
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  private int dateiausgabe(ArrayList<Lastschrift> lastschriften, File file,
      Date faell, UeberweisungAusgabe ausgabe, String verwendungszweck)
      throws Exception
  {
    SepaVersion sepaVersion = SepaVersion
        .byURN((String) Einstellungen.getEinstellung(Property.CT1SEPAVERSION));
    Properties ls_properties = new Properties();
    ls_properties.setProperty("src.bic",
        (String) Einstellungen.getEinstellung(Property.BIC));
    ls_properties.setProperty("src.iban",
        (String) Einstellungen.getEinstellung(Property.IBAN));
    ls_properties.setProperty("src.name",
        ((String) Einstellungen.getEinstellung(Property.NAME)).toUpperCase());
    long epochtime = Calendar.getInstance().getTimeInMillis();
    String epochtime_string = Long.toString(epochtime);
    DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);
    ls_properties.setProperty("sepaid", epochtime_string);
    ls_properties.setProperty("pmtinfid", epochtime_string);
    ls_properties.setProperty("date", ISO_DATE.format(faell));
    ls_properties.setProperty("batchbook", "false");
    int counter = 0;
    for (Lastschrift ls : lastschriften)
    {
      ls_properties.setProperty(SepaUtil.insertIndex("dst.bic", counter),
          StringUtils.trimToEmpty(ls.getBIC()));
      ls_properties.setProperty(SepaUtil.insertIndex("dst.iban", counter),
          StringUtils.trimToEmpty(ls.getIBAN()));
      if (ls.getMitglied() != null)
      {
        ls_properties.setProperty(SepaUtil.insertIndex("dst.name", counter),
            StringUtils.trimToEmpty(ls.getMitglied()
                .getKontoinhaber(Mitglied.namenformat.KONTOINHABER)
                .toUpperCase()));
      }
      else if (ls.getKursteilnehmer() != null)
      {
        ls_properties.setProperty(SepaUtil.insertIndex("dst.name", counter),
            StringUtils.trimToEmpty(Adressaufbereitung
                .getNameVorname(ls.getKursteilnehmer()).toUpperCase()));
      }
      if (betrag != null)
      {
        ls_properties.setProperty(SepaUtil.insertIndex("btg.value", counter),
            (BigDecimal.valueOf(betrag)).toString());
      }
      else
      {
        ls_properties.setProperty(SepaUtil.insertIndex("btg.value", counter),
            (BigDecimal.valueOf(ls.getBetrag())).toString());
      }
      ls_properties.setProperty(SepaUtil.insertIndex("btg.curr", counter),
          HBCIProperties.CURRENCY_DEFAULT_DE);
      ls_properties.setProperty(SepaUtil.insertIndex("usage", counter),
          StringUtils.trimToEmpty(eval(ls, verwendungszweck)));
      ls_properties.setProperty(SepaUtil.insertIndex("endtoendid", counter),
          "NOTPROVIDED");
      ls_properties.setProperty(SepaUtil.insertIndex("mandateid", counter),
          StringUtils.trimToEmpty(ls.getMandatID()));
      ls_properties.setProperty(SepaUtil.insertIndex("manddateofsig", counter),
          ISO_DATE.format(ls.getMandatDatum()));
      counter += 1;
    }
    final OutputStream os = Files.newOutputStream(file.toPath());
    System.setProperty("sepa.pain.formatted", "true");
    ISEPAGenerator<Properties> sepagenerator = SEPAGeneratorFactory
        .get("UebSEPA", sepaVersion);
    sepagenerator.generate(ls_properties, os, true);
    os.close();
    return counter;
  }

  private int hibiscusausgabe(ArrayList<Lastschrift> lastschriften, File file,
      Date faell, UeberweisungAusgabe ct1ausgabe, String verwendungszweck)
      throws Exception
  {
    try
    {
      Konto hibk;
      try
      {
        // DB-Service holen
        DBService service = (DBService) Application.getServiceFactory()
            .lookup(HBCI.class, "database");
        DBIterator<Konto> konten = service.createList(Konto.class);
        konten.addFilter("iban = ?",
            (String) Einstellungen.getEinstellung(Property.IBAN));
        Logger.debug("Vereinskonto: "
            + (String) Einstellungen.getEinstellung(Property.IBAN));
        if (konten.hasNext())
        {
          hibk = (Konto) konten.next();
        }
        else
        {
          throw new RemoteException("Vereinskonto nicht in Hibiscus gefunden");
        }
      }
      catch (Exception e)
      {
        throw new RemoteException(e.getMessage());
      }
      AuslandsUeberweisung[] ueberweisungen = new AuslandsUeberweisung[lastschriften
          .size()];
      int i = 0;
      for (Lastschrift ls : lastschriften)
      {
        DBService service = (DBService) Application.getServiceFactory()
            .lookup(HBCI.class, "database");

        AuslandsUeberweisung ue = (AuslandsUeberweisung) service
            .createObject(AuslandsUeberweisung.class, null);
        if (betrag != null)
        {
          ue.setBetrag(betrag);
        }
        else
        {
          ue.setBetrag(ls.getBetrag());
        }
        HibiscusAddress ad = (HibiscusAddress) service
            .createObject(HibiscusAddress.class, null);
        ad.setBic(ls.getBIC());
        ad.setIban(ls.getIBAN());
        ue.setGegenkonto(ad);
        ue.setEndtoEndId(ls.getMandatID());
        if (ls.getMitglied() != null)
        {
          ue.setGegenkontoName(
              StringTool.getStringWithMaxLength(
                  Zeichen.convert(ls.getMitglied()
                      .getKontoinhaber(Mitglied.namenformat.KONTOINHABER)),
                  255));
        }
        else if (ls.getKursteilnehmer() != null)
        {
          ue.setGegenkontoName(
              StringTool.getStringWithMaxLength(
                  Zeichen.convert(ls.getKursteilnehmer()
                      .getKontoinhaber(Mitglied.namenformat.KONTOINHABER)),
                  255));
        }
        ue.setTermin(faell);
        ue.setZweck(StringTool.getStringWithMaxLength(
            Zeichen.convert(eval(ls, verwendungszweck)), 140));
        ue.setKonto(hibk);
        ueberweisungen[i] = ue;
        i++;
      }
      SepaUeberweisungMerge merge = new SepaUeberweisungMerge();
      merge.handleAction(ueberweisungen);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
    catch (SEPAException e)
    {
      throw new ApplicationException(e);
    }
    return 1;
  }

  public String eval(Lastschrift ls, String verwendungszweck)
      throws ParseErrorException, MethodInvocationException,
      ResourceNotFoundException, IOException
  {
    VelocityContext context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    Map<String, Object> map = new LastschriftMap().getMap(ls, null);
    map = new AllgemeineMap().getMap(map);
    VarTools.add(context, map);
    StringWriter vzweck = new StringWriter();
    Velocity.evaluate(context, vzweck, "LOG", verwendungszweck);
    return vzweck.getBuffer().toString().toUpperCase();
  }

}
