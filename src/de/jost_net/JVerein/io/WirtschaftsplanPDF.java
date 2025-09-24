/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanPDF
{
  private final Map<WirtschaftsplanNode, Double> sollSummen;

  public WirtschaftsplanPDF(List<WirtschaftsplanNode> einnahmenList,
      List<WirtschaftsplanNode> ausgabenList, File file,
      Wirtschaftsplan wirtschaftsplan) throws ApplicationException
  {
    sollSummen = new HashMap<>();
    try
    {
      double sollEinnahmenGesamt = calculateSolls(einnahmenList);
      double sollAusgabenGesamt = calculateSolls(ausgabenList);

      FileOutputStream fileOutputStream = new FileOutputStream(file);
      String subtitle = new JVDateFormatTTMMJJJJ()
          .format(wirtschaftsplan.getDatumVon()) + " - "
          + new JVDateFormatTTMMJJJJ().format(wirtschaftsplan.getDatumBis());
      int size = einnahmenList.stream()
          .mapToInt(WirtschaftsplanNode::anzahlLeafs).sum()
          + ausgabenList.stream().mapToInt(WirtschaftsplanNode::anzahlLeafs)
              .sum();
      Reporter reporter = new Reporter(fileOutputStream, "Wirtschaftsplan",
          subtitle, size);

      Paragraph detailParagraph = new Paragraph("\n Detailansicht",
          Reporter.getFreeSans(11));
      reporter.add(detailParagraph);

      reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 90,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Posten", Element.ALIGN_CENTER, 90,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Einnahmen Soll", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Ausgaben Soll", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      reporter.addColumn("Einnahmen", Element.ALIGN_CENTER,
          new BaseColor(220, 220, 220), 4);

      einnahmenList.forEach(einnahme -> {

        try
        {
          if (!einnahme.hasLeaf())
          {
            return;
          }
          reporter.addColumn(einnahme.getBuchungsklasse().getBezeichnung(),
              Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 2);
          reporter.addColumn(sollSummen.get(einnahme));
          reporter.addColumn("", Element.ALIGN_CENTER);

          iterateOverNodes(einnahme.getChildren(), reporter, true);
        }
        catch (RemoteException e)
        {
          throw new RuntimeException(e);
        }
      });

      reporter.addColumn("Ausgaben", Element.ALIGN_CENTER,
          new BaseColor(220, 220, 220), 4);

      ausgabenList.forEach(ausgabe -> {
        try
        {
          if (!ausgabe.hasLeaf())
          {
            return;
          }
          reporter.addColumn(ausgabe.getBuchungsklasse().getBezeichnung(),
              Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 2);
          reporter.addColumn("", Element.ALIGN_CENTER);
          reporter.addColumn(sollSummen.get(ausgabe));

          iterateOverNodes(ausgabe.getChildren(), reporter, false);
        }
        catch (RemoteException e)
        {
          throw new RuntimeException(e);
        }
      });

      reporter.closeTable();

      Paragraph zusammenfassungParagraph = new Paragraph("\n Zusammenfassung",
          Reporter.getFreeSans(11));
      reporter.add(zusammenfassungParagraph);

      reporter.addHeaderColumn("Einnahmen Soll", Element.ALIGN_CENTER, 40,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Ausgaben Soll", Element.ALIGN_CENTER, 40,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Saldo", Element.ALIGN_CENTER, 40,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      reporter.addColumn(sollEinnahmenGesamt);
      reporter.addColumn(sollAusgabenGesamt);
      reporter.addColumn(sollEinnahmenGesamt + sollAusgabenGesamt);

      reporter.closeTable();
      reporter.close();
      fileOutputStream.close();
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException(e);
    }
  }

  private double calculateSolls(List<WirtschaftsplanNode> nodeList)
  {
    return nodeList.stream().mapToDouble(node -> {
      try
      {
        double soll = calculateSolls(node.getChildren());
        sollSummen.put(node, soll);
        return soll;
      }
      catch (RemoteException e)
      {
        throw new RuntimeException(e);
      }
    }).sum();
  }

  @SuppressWarnings("rawtypes")
  private double calculateSolls(GenericIterator iterator) throws RemoteException
  {
    double soll = 0;

    while (iterator.hasNext())
    {
      WirtschaftsplanNode currentNode = (WirtschaftsplanNode) iterator.next();
      double currentSoll;

      if (currentNode.getType().equals(WirtschaftsplanNode.Type.POSTEN))
      {
        currentSoll = currentNode.getSoll();
      }
      else
      {
        currentSoll = calculateSolls(currentNode.getChildren());
      }

      sollSummen.put(currentNode, currentSoll);
      soll += currentSoll;
    }

    return soll;
  }

  @SuppressWarnings("rawtypes")
  private void iterateOverNodes(GenericIterator iterator, Reporter reporter,
      boolean einnahme) throws RemoteException
  {
    while (iterator.hasNext())
    {
      WirtschaftsplanNode currentNode = (WirtschaftsplanNode) iterator.next();

      switch (currentNode.getType())
      {
        case BUCHUNGSART:
          if (!currentNode.hasLeaf())
          {
            continue;
          }
          reporter.addColumn(currentNode.getBuchungsart().getBezeichnung(),
              Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_CENTER);

          break;
        case POSTEN:
          reporter.addColumn("", Element.ALIGN_CENTER);
          reporter.addColumn(currentNode.getWirtschaftsplanItem().getPosten(),
              Element.ALIGN_LEFT);
          break;
        default:
          break;
      }
      if (einnahme)
      {
        reporter.addColumn(sollSummen.get(currentNode));
        reporter.addColumn("", Element.ALIGN_CENTER);
      }
      else
      {
        reporter.addColumn("", Element.ALIGN_CENTER);
        reporter.addColumn(sollSummen.get(currentNode));
      }
      if (!currentNode.getType().equals(WirtschaftsplanNode.Type.POSTEN))
      {
        iterateOverNodes(currentNode.getChildren(), reporter, einnahme);
      }
    }
  }
}
