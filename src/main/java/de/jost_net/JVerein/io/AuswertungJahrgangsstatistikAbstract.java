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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public abstract class AuswertungJahrgangsstatistikAbstract implements Exporter
{

  protected File file;

  protected Date stichtag;

  protected TreeMap<String, StatistikJahrgang> statistik;

  protected ExportLayoutParam params;

  @SuppressWarnings("unchecked")
  @Override
  public void doExport(final Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws ApplicationException, DocumentException, IOException
  {
    /*
     * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
     * objects[2] ist Mitgliedstyp, objects[3] ist der Filter
     */
    this.file = file;
    this.params = params;
    Map<Filter, Object> filter = (Map<Filter, Object>) objects[3];
    Integer jahr = (Integer) filter.get(Filter.JAHR);
    if (jahr == null)
    {
      throw new ApplicationException("Auswertungsjahr ist leer");
    }
    statistik = new TreeMap<>();
    try
    {
      stichtag = Datum.toDate("31.12." + jahr);
    }
    catch (ParseException e)
    {
      Logger.error("Datum kann nicht geparsed werden: ", e);
    }
    /*
     * Teil 1: natürliche Personen
     */
    DBIterator<Mitglied> mitgl = Einstellungen.getDBService()
        .createList(Mitglied.class);
    MitgliedUtils.setNurAktive(mitgl, stichtag);
    MitgliedUtils.setMitglied(mitgl);
    MitgliedUtils.setMitgliedNatuerlichePerson(mitgl);
    mitgl.addFilter("geburtsdatum is not null");
    mitgl.setOrder("order by geburtsdatum");
    Calendar cal = Calendar.getInstance();
    while (mitgl.hasNext())
    {
      Mitglied m = (Mitglied) mitgl.next();
      cal.setTime(m.getGeburtsdatum());
      String jg = cal.get(Calendar.YEAR) + "";
      StatistikJahrgang dsbj = statistik.get(jg);
      if (dsbj == null)
      {
        dsbj = new StatistikJahrgang();
        statistik.put(jg, dsbj);
      }
      dsbj.incrementGesamt();
      if (m.getGeschlecht().equals(GeschlechtInput.MAENNLICH))
      {
        dsbj.incrementMaennlich();
      }
      if (m.getGeschlecht().equals(GeschlechtInput.WEIBLICH))
      {
        dsbj.incrementWeiblich();
      }
      if (m.getGeschlecht().equals(GeschlechtInput.OHNEANGABE))
      {
        dsbj.incrementOhne();
      }
    }
    /*
     * Teil 2: Juristische Personen
     */
    DBIterator<Mitglied> mitglj = Einstellungen.getDBService()
        .createList(Mitglied.class);
    MitgliedUtils.setNurAktive(mitglj, stichtag);
    MitgliedUtils.setMitglied(mitglj);
    MitgliedUtils.setMitgliedJuristischePerson(mitglj);
    while (mitglj.hasNext())
    {
      String jg = "Juristische Personen";
      StatistikJahrgang dsbj = statistik.get(jg);
      if (dsbj == null)
      {
        dsbj = new StatistikJahrgang();
        statistik.put(jg, dsbj);
      }
      dsbj.incrementGesamt();
      dsbj.incrementOhne();
      mitglj.next();
    }
    /*
     * Teil 3: Ohne Geburtsdatum
     */
    DBIterator<Mitglied> mitglo = Einstellungen.getDBService()
        .createList(Mitglied.class);
    MitgliedUtils.setNurAktive(mitglo, stichtag);
    MitgliedUtils.setMitglied(mitglo);
    MitgliedUtils.setMitgliedNatuerlichePerson(mitglo);
    mitglo.addFilter("geburtsdatum is null");
    while (mitglo.hasNext())
    {
      Mitglied m = (Mitglied) mitglo.next();
      String jg = "Ohne Datum";
      StatistikJahrgang dsbj = statistik.get(jg);
      if (dsbj == null)
      {
        dsbj = new StatistikJahrgang();
        statistik.put(jg, dsbj);
      }
      dsbj.incrementGesamt();
      if (m.getGeschlecht().equals(GeschlechtInput.MAENNLICH))
      {
        dsbj.incrementMaennlich();
      }
      if (m.getGeschlecht().equals(GeschlechtInput.WEIBLICH))
      {
        dsbj.incrementWeiblich();
      }
      if (m.getGeschlecht().equals(GeschlechtInput.OHNEANGABE))
      {
        dsbj.incrementOhne();
      }
    }

    open();
    close();
  }

  protected abstract void open() throws DocumentException, IOException;

  protected abstract void close() throws IOException, DocumentException;

  @Override
  public String getTitle(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL,
        object);
  }

  @Override
  public String getSubtitle(Object object)
  {
    return VorlageUtil
        .getName(VorlageTyp.AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL, object);
  }

  public class StatistikJahrgang
  {

    private int anzahlgesamt = 0;

    private int anzahlmaennlich = 0;

    private int anzahlweiblich = 0;

    private int anzahlohne = 0;

    public int getAnzahlgesamt()
    {
      return anzahlgesamt;
    }

    public void incrementGesamt()
    {
      this.anzahlgesamt++;
    }

    public int getAnzahlmaennlich()
    {
      return anzahlmaennlich;
    }

    public void incrementMaennlich()
    {
      this.anzahlmaennlich++;
    }

    public int getAnzahlweiblich()
    {
      return anzahlweiblich;
    }

    public void incrementWeiblich()
    {
      this.anzahlweiblich++;
    }

    public int getAnzahlOhne()
    {
      return anzahlohne;
    }

    public void incrementOhne()
    {
      this.anzahlohne++;
    }
  }

}
