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
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.Ct1Ausgabe;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.jost_net.OBanToo.StringLatin.Zeichen;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.SepaUeberweisungMerge;
import de.willuhn.jameica.hbci.gui.dialogs.PainVersionDialog;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class Ct1Ueberweisung
{

  public Ct1Ueberweisung()
  {
  }

  public int write(Abrechnungslauf abrl, File file, Date faell,
      Ct1Ausgabe ct1ausgabe, String verwendungszweck) throws Exception
  {
    Velocity.init();
    switch (ct1ausgabe)
    {
      case SEPA_DATEI:
        return dateiausgabe(abrl, file, faell, ct1ausgabe, verwendungszweck);

      case HIBISCUS:
        return hibiscusausgabe(abrl, file, faell, ct1ausgabe, verwendungszweck);
    }
    return -1;
  }

  private int dateiausgabe(Abrechnungslauf abrl, File file, Date faell,
      Ct1Ausgabe ct1ausgabe, String verwendungszweck) throws Exception
  {
    SepaVersion sepaVersion;  
    if (Einstellungen.getEinstellung().getCt1SepaVersion() != null)
    {
      sepaVersion = Einstellungen.getEinstellung().getCt1SepaVersion();
    }
    else
    {
      PainVersionDialog d = new PainVersionDialog(org.kapott.hbci.sepa.SepaVersion.Type.PAIN_001);
      sepaVersion = (SepaVersion) d.open();
      if (sepaVersion == null)
      {
        throw new OperationCanceledException();
      }
    }
    Properties ls_properties = new Properties();
    ls_properties.setProperty("src.bic", Einstellungen.getEinstellung().getBic());
    ls_properties.setProperty("src.iban", Einstellungen.getEinstellung().getIban());
    ls_properties.setProperty("src.name", Einstellungen.getEinstellung().getName().toUpperCase());
    long epochtime = Calendar.getInstance().getTimeInMillis();
    String epochtime_string = Long.toString(epochtime);
    DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);
    ls_properties.setProperty("sepaid", epochtime_string);
    ls_properties.setProperty("pmtinfid", epochtime_string);
    ls_properties.setProperty("date", ISO_DATE.format(faell) );
    ls_properties.setProperty("batchbook", "false");
    int counter = 0;
    DBIterator<Lastschrift> it = getIterator(abrl);
    while (it.hasNext())
    {
      Lastschrift ls = (Lastschrift) it.next();
      ls_properties.setProperty(SepaUtil.insertIndex("dst.bic", counter),       StringUtils.trimToEmpty(ls.getBIC()));
      ls_properties.setProperty(SepaUtil.insertIndex("dst.iban", counter),      StringUtils.trimToEmpty(ls.getIBAN()));
      ls_properties.setProperty(SepaUtil.insertIndex("dst.name", counter),      StringUtils.trimToEmpty(ls.getMitglied()
          .getKontoinhaber(1).toUpperCase()));
      ls_properties.setProperty(SepaUtil.insertIndex("btg.value", counter),     (new BigDecimal("0.01")).toString());
      ls_properties.setProperty(SepaUtil.insertIndex("btg.curr", counter),      HBCIProperties.CURRENCY_DEFAULT_DE);
      ls_properties.setProperty(SepaUtil.insertIndex("usage", counter),         StringUtils.trimToEmpty(eval(ls, verwendungszweck)));
      ls_properties.setProperty(SepaUtil.insertIndex("endtoendid", counter),    "NOTPROVIDED");
      ls_properties.setProperty(SepaUtil.insertIndex("mandateid", counter),     StringUtils.trimToEmpty(ls.getMandatID()));
      ls_properties.setProperty(SepaUtil.insertIndex("manddateofsig", counter), ISO_DATE.format(ls.getMandatDatum()));
      counter += 1;
    }
    final OutputStream os = Files.newOutputStream(file.toPath());
    System.setProperty("sepa.pain.formatted", "true");
    ISEPAGenerator sepagenerator = SEPAGeneratorFactory.get("UebSEPA", sepaVersion);
    sepagenerator.generate(ls_properties, os, true);
    os.close();
    return counter;
  }

  private int hibiscusausgabe(Abrechnungslauf abrl, File file, Date faell,
      Ct1Ausgabe ct1ausgabe, String verwendungszweck) throws Exception
  {
    try
    {
      de.willuhn.jameica.hbci.rmi.Konto hibk = Einstellungen.getEinstellung()
          .getHibiscusKonto();
      DBIterator<Lastschrift> it = getIterator(abrl);
      AuslandsUeberweisung[] ueberweisungen = new AuslandsUeberweisung[it
          .size()];
      int i = 0;

      while (it.hasNext())
      {
        Lastschrift ls = (Lastschrift) it.next();
        DBService service = (DBService) Application.getServiceFactory()
            .lookup(HBCI.class, "database");

        AuslandsUeberweisung ue = (AuslandsUeberweisung) service
            .createObject(AuslandsUeberweisung.class, null);
        ue.setBetrag(0.01);
        HibiscusAddress ad = (HibiscusAddress) service
            .createObject(HibiscusAddress.class, null);
        ad.setBic(ls.getBIC());
        ad.setIban(ls.getIBAN());
        ue.setGegenkonto(ad);
        ue.setEndtoEndId(ls.getMandatID());
        ue.setGegenkontoName(StringTool
            .getStringWithMaxLength(Zeichen.convert(ls.getName()), 255));
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

  private DBIterator<Lastschrift> getIterator(Abrechnungslauf abrl)
      throws RemoteException
  {
    DBIterator<Lastschrift> it = Einstellungen.getDBService()
        .createList(Lastschrift.class);
    it.addFilter("abrechnungslauf = ?", abrl.getID());
    it.setOrder("order by name, vorname");
    return it;
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
