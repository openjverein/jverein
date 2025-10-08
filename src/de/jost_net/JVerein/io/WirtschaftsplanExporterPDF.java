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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class WirtschaftsplanExporterPDF implements Exporter
{
  // Liste der Pläne die Ist-Beträge haben
  private Set<Wirtschaftsplan> hatIst = new HashSet<>();

  @Override
  public String getName()
  {
    return "Wirtschaftsplan PDF-Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Wirtschaftsplan.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return WirtschaftsplanExporterPDF.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.pdf" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil
        .getName(object == null ? VorlageTyp.WIRTSCHAFTSPLAN_MEHRERE_DATEINAME
            : VorlageTyp.WIRTSCHAFTSPLAN_DATEINAME, object)
        + ".pdf";
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws RemoteException, ApplicationException,
      FileNotFoundException, DocumentException, IOException
  {
    Wirtschaftsplan[] wirtschaftsplaene;
    if (objects[0] instanceof Wirtschaftsplan[])
    {
      wirtschaftsplaene = (Wirtschaftsplan[]) objects[0];
    }
    else if (objects[0] instanceof Wirtschaftsplan)
    {
      wirtschaftsplaene = new Wirtschaftsplan[] {
          (Wirtschaftsplan) objects[0] };
    }
    else
    {
      throw new ApplicationException("Keine Pläne ausgewählt");
    }

    FileOutputStream fileOutputStream = new FileOutputStream(file);

    Reporter reporter = new Reporter(fileOutputStream, "Wirtschaftsplan", "", 1,
        40, 20, 20, 20);

    // Header erstellen
    // Leider kann der Header kein Colspan, daher erst Soll/Ist anzeigen
    reporter.addHeaderColumn("Buchungsart/Posten", Element.ALIGN_CENTER, 150,
        BaseColor.LIGHT_GRAY);
    for (int i = 0; i < wirtschaftsplaene.length; i++)
    {
      reporter.addHeaderColumn("Soll", Element.ALIGN_CENTER, 90,
          BaseColor.LIGHT_GRAY);
      // Wenn es für diesen Plan noch keine Ist-Buchungen gab, spalte "Ist"
      // ausblenden. Oder wenn Ende noch in der Zukkunft und in Einstellungen so
      // festgelegt.
      if (((Boolean) Einstellungen
          .getEinstellung(Property.WIRTSCHFTSPLAN_IST_NICHT_ABGESCHLOSSEN)
          || wirtschaftsplaene[i].getDatumBis().before(new Date()))
          && (Math.abs(wirtschaftsplaene[i].getIstEinnahme()) >= 0.01d
              || Math.abs(wirtschaftsplaene[i].getIstAusgabe()) >= 0.01d))
      {
        hatIst.add(wirtschaftsplaene[i]);
        reporter.addHeaderColumn("Ist", Element.ALIGN_CENTER, 90,
            BaseColor.LIGHT_GRAY);
      }
    }
    reporter.createHeader();

    // Unter-Header
    reporter.addColumn("", Element.ALIGN_CENTER, BaseColor.LIGHT_GRAY);
    for (Wirtschaftsplan plan : wirtschaftsplaene)
    {
      reporter.addColumn(
          plan.getBezeichung() + "\n"
              + new JVDateFormatTTMMJJJJ().format(plan.getDatumVon()) + "-"
              + new JVDateFormatTTMMJJJJ().format(plan.getDatumBis()),
          Element.ALIGN_CENTER, BaseColor.LIGHT_GRAY,
          hatIst.contains(plan) ? 2 : 1);
    }

    DBIterator<Buchungsklasse> buchungsklasseIterator = Einstellungen
        .getDBService().createList(Buchungsklasse.class);
    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
    {
      case BuchungsartSort.NACH_NUMMER:
        buchungsklasseIterator.setOrder("Order by -nummer DESC");
        break;
      default:
        buchungsklasseIterator
            .setOrder("Order by bezeichnung is NULL, bezeichnung");
        break;
    }

    // Einnahmen
    reporter.addColumn("Einnahmen", Element.ALIGN_LEFT,
        new BaseColor(100, 100, 100), wirtschaftsplaene.length * 2 + 1);
    Double[][] summenEinnahmen = new Double[wirtschaftsplaene.length][2];
    while (buchungsklasseIterator.hasNext())
    {
      Double[][] s = addColumns(wirtschaftsplaene,
          buchungsklasseIterator.next(), reporter,
          WirtschaftsplanImpl.EINNAHME);
      // Gesamtsumme addieren
      if (s == null)
      {
        continue;
      }
      for (int i = 0; i < wirtschaftsplaene.length; i++)
      {
        summenEinnahmen[i][0] = (summenEinnahmen[i][0] == null ? 0
            : summenEinnahmen[i][0]) + s[i][0];
        if (hatIst.contains(wirtschaftsplaene[i]))
        {
          summenEinnahmen[i][1] = (summenEinnahmen[i][1] == null ? 0
              : summenEinnahmen[i][1]) + s[i][1];
        }
      }
    }
    // Summenzeile Einnahmen
    reporter.addColumn("Summe Einnahmen", Element.ALIGN_LEFT,
        BaseColor.LIGHT_GRAY);
    int j = -1;
    for (Double[] sollist : summenEinnahmen)
    {
      j++;
      reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[0]),
          Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      if (hatIst.contains(wirtschaftsplaene[j]))
      {
        reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[1]),
            Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      }
    }
    reporter.addColumn(" ", Element.ALIGN_LEFT,
        wirtschaftsplaene.length * 2 + 1);

    // Ausgaben
    reporter.addColumn("Ausgaben", Element.ALIGN_LEFT,
        new BaseColor(100, 100, 100), wirtschaftsplaene.length * 2 + 1);
    buchungsklasseIterator.begin();
    Double[][] summenAusgaben = new Double[wirtschaftsplaene.length][2];
    while (buchungsklasseIterator.hasNext())
    {
      Double[][] s = addColumns(wirtschaftsplaene,
          buchungsklasseIterator.next(), reporter, WirtschaftsplanImpl.AUSGABE);
      // Gesamtsumme addieren
      if (s == null)
      {
        continue;
      }
      for (int i = 0; i < wirtschaftsplaene.length; i++)
      {
        summenAusgaben[i][0] = (summenAusgaben[i][0] == null ? 0
            : summenAusgaben[i][0]) + s[i][0];
        if (hatIst.contains(wirtschaftsplaene[i]))
        {
          summenAusgaben[i][1] = (summenAusgaben[i][1] == null ? 0
              : summenAusgaben[i][1]) + s[i][1];
        }
      }
    }
    // Summenzeile Ausgaben
    reporter.addColumn("Summe Ausgaben", Element.ALIGN_LEFT,
        BaseColor.LIGHT_GRAY);
    int k = -1;
    for (Double[] sollist : summenAusgaben)
    {
      k++;
      reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[0]),
          Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      if (hatIst.contains(wirtschaftsplaene[k]))
      {
        reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[1]),
            Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      }
    }

    // Saldo
    reporter.addColumn(" ", Element.ALIGN_LEFT,
        wirtschaftsplaene.length * 2 + 1);
    reporter.addColumn("Saldo", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
    for (int i = 0; i < summenAusgaben.length; i++)
    {
      reporter.addColumn(
          Einstellungen.DECIMALFORMAT
              .format(summenEinnahmen[i][0] + summenAusgaben[i][0]),
          Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      if (hatIst.contains(wirtschaftsplaene[i]))
      {
        reporter.addColumn(
            Einstellungen.DECIMALFORMAT
                .format(summenEinnahmen[i][1] + summenAusgaben[i][1]),
            Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      }
    }

    reporter.closeTable();
    reporter.close();
    fileOutputStream.close();
  }

  @SuppressWarnings("unchecked")
  private Double[][] addColumns(Wirtschaftsplan[] wirtschaftsplaene,
      Buchungsklasse klasse, Reporter reporter, int art) throws RemoteException
  {
    int n = 0;
    Double[][] summen = new Double[wirtschaftsplaene.length][2];
    final Boolean ohneBuchungsartUnterdruecken = (Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG);

    // Daten in Map sammeln
    // Map in der Form <Buchungsart, <Node,[Plan Nummer][Soll = 0|Ist = 1]>>
    HashMap<WirtschaftsplanNode, HashMap<String, Double[][]>> map = new HashMap<>();

    for (Wirtschaftsplan plan : wirtschaftsplaene)
    {
      WirtschaftsplanNode buchungsklasseNode = new WirtschaftsplanNode(klasse,
          art, plan);

      GenericIterator<WirtschaftsplanNode> children = buchungsklasseNode
          .getChildren();
      while (children.hasNext())
      {
        WirtschaftsplanNode buchungsartNode = children.next();

        // Bestehenden eintrag für den BuchungsartNode in der Map finden
        WirtschaftsplanNode key = buchungsartNode;
        for (WirtschaftsplanNode k : map.keySet())
        {
          if (k.getBuchungsart().equals(buchungsartNode.getBuchungsart()))
          {
            key = k;
          }
        }
        HashMap<String, Double[][]> entryMap = map.getOrDefault(key,
            new HashMap<>());

        // Buchungsart in der Map mit Node-Key '-'
        Double[][] entry = entryMap.getOrDefault("-",
            new Double[wirtschaftsplaene.length][2]);
        entry[n][0] = buchungsartNode.getSoll();
        entry[n][1] = buchungsartNode.getIst();

        entryMap.put("-", entry);

        GenericIterator<WirtschaftsplanNode> postenChilds = buchungsartNode
            .getChildren();
        while (postenChilds.hasNext())
        {
          WirtschaftsplanNode posten = postenChilds.next();

          String nodekey = (String) posten
              .getAttribute("buchungsklassebezeichnung");
          entry = entryMap.getOrDefault(nodekey,
              new Double[wirtschaftsplaene.length][2]);
          entry[n][0] = posten.getSoll();
          entry[n][1] = posten.getIst();

          entryMap.put(nodekey, entry);
        }
        map.put(key, entryMap);
      }
      summen[n][0] = buchungsklasseNode.getSoll();
      summen[n][1] = buchungsklasseNode.getIst();
      n++;
    }
    if (map.size() == 0)
    {
      return null;
    }

    reporter.addColumn(new BuchungsklasseFormatter().format(klasse),
        Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY,
        wirtschaftsplaene.length * 2 + 1);

    // Spalten füllen
    // Buchungsarten
    map.entrySet().stream().sorted(Map.Entry
        .<WirtschaftsplanNode, HashMap<String, Double[][]>> comparingByKey())
        .forEach(buchungsartEntry -> {
          // Buchungsarten mit keinen Eintrag (außer Buchungsart) und Ist=0
          // überspringen
          if (ohneBuchungsartUnterdruecken
              && buchungsartEntry.getValue().size() == 1
              && Math.abs(buchungsartEntry.getKey().getIst()) < 0.01d)
          {
            return;
          }

          // Posten
          buchungsartEntry.getValue().entrySet().stream()
              .sorted(Map.Entry.<String, Double[][]> comparingByKey())
              .forEach(postenEntry -> {
                try
                {
                  if ("-".equals(postenEntry.getKey()))
                  {
                    String text = (String) buchungsartEntry.getKey()
                        .getAttribute("buchungsklassebezeichnung");
                    reporter.addColumn(text, Element.ALIGN_LEFT);
                  }
                  else
                  {
                    // wenn es nur einen Posten gibt, abbrechen
                    if (buchungsartEntry.getValue().size() <= 2)
                    {
                      return;
                    }
                    reporter.addColumn(postenEntry.getKey(),
                        Element.ALIGN_RIGHT);
                  }

                  Double[][] values = postenEntry.getValue();
                  int i = -1;
                  for (Double[] betrag : values)
                  {
                    i++;
                    reporter.addColumn(
                        Einstellungen.DECIMALFORMAT
                            .format(betrag[0] == null ? 0 : betrag[0]),
                        Element.ALIGN_RIGHT, new BaseColor(230, 230, 230));

                    if (hatIst.contains(wirtschaftsplaene[i]))
                    {
                      String text = "";
                      // Ist nur bei Buchungsart
                      if ("-".equals(postenEntry.getKey()))
                      {
                        text = Einstellungen.DECIMALFORMAT
                            .format(betrag[1] == null ? 0 : betrag[1]);

                      }
                      reporter.addColumn(text, Element.ALIGN_RIGHT);
                    }
                  }
                }
                catch (RemoteException re)
                {
                  Logger.error("Fehler beim Wirtschaftsplan Export", re);
                }
              });
        });

    // Summenzeile
    reporter.addColumn("Summe " + new BuchungsklasseFormatter().format(klasse),
        Element.ALIGN_LEFT, new BaseColor(230, 230, 230));
    int j = -1;
    for (Double[] sollist : summen)
    {
      j++;
      reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[0]),
          Element.ALIGN_RIGHT, new BaseColor(230, 230, 230));
      if (hatIst.contains(wirtschaftsplaene[j]))
      {
        reporter.addColumn(Einstellungen.DECIMALFORMAT.format(sollist[1]),
            Element.ALIGN_RIGHT, new BaseColor(230, 230, 230));
      }
    }
    return summen;
  }
}
