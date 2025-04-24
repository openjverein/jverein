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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.control.SollbuchungControl.DIFFERENZ;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.GUI;

public class Kontoauszug
{

  private de.willuhn.jameica.system.Settings settings;

  private File file;

  private Reporter rpt;

  private Kontoauszug() throws IOException, DocumentException
  {
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Kontoauszug(Object object, SollbuchungControl control) throws Exception
  {
    this();
    ArrayList<Mitglied> mitglieder = new ArrayList<>();

    if (object == null && control.isSuchMitgliedstypActive() && 
        control.getSuchMitgliedstyp(Mitgliedstypen.ALLE).getValue() != null)
    {
      Mitgliedstyp mt = (Mitgliedstyp) control.getSuchMitgliedstyp(Mitgliedstypen.ALLE).getValue();
      mitglieder = new MitgliedQuery(control).
          get(Integer.parseInt(mt.getID()), null);
    }
    else if (object == null && control.isSuchMitgliedstypActive() && 
        control.getSuchMitgliedstyp(Mitgliedstypen.ALLE).getValue() == null)
    {
      mitglieder = new MitgliedQuery(control).get(-1, null);
    }
    else if (object != null && object instanceof Mitglied)
    {
      mitglieder.add((Mitglied) object);
    }
    else if (object != null && object instanceof Mitglied[])
    {
      mitglieder = new ArrayList<>(Arrays.asList((Mitglied[]) object));
    }
    else
    {
      GUI.getStatusBar().setErrorText(
          "Kein Mitglied ausgewählt. Vorgang abgebrochen.");
      return;
    }
    
    int anzahl = 0;
    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        init("pdf");
        if (file == null)
        {
          return;
        }
        rpt = new Reporter(new FileOutputStream(file), 40, 20, 20, 40, false);
        for (Mitglied mg : mitglieder)
        {
          if (generiereMitglied(mg, control))
              anzahl++;
        }
        if (anzahl == 0)
        {
          GUI.getStatusBar().setErrorText(
              "Kein Mitglied erfüllt das Differenz Kriterium.");
          file.delete();
          return;
        }
        rpt.close();
        zeigeDokument();
        break;
      case MAIL:
        init("zip");
        if (file == null)
        {
          return;
        }
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        for (Mitglied mg : mitglieder)
        {
          if (mg.getEmail() == null || mg.getEmail().isEmpty())
          {
            continue;
          }
          File f = File.createTempFile(getDateiname(mg), ".pdf");
          rpt = new Reporter(new FileOutputStream(f), 40, 20, 20, 40, false);
          if (generiereMitglied(mg, control) == false)
          {
            continue;
          }
          rpt.close();
          anzahl++;
          zos.putNextEntry(new ZipEntry(getDateiname(mg) + ".pdf"));
          FileInputStream in = new FileInputStream(f);
          // buffer size
          byte[] b = new byte[1024];
          int count;
          while ((count = in.read(b)) > 0)
          {
            zos.write(b, 0, count);
          }
          in.close();
        }
        zos.close();
        if (anzahl == 0)
        {
          GUI.getStatusBar().setErrorText(
              "Kein Mitglied erfüllt das Differenz Kriterium.");
          file.delete();
          return;
        }
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue(), "Kontoauszug.pdf");
        break;
    } 
  }

  private void init(String extension) throws IOException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = settings
        .getString("lastdir", System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("KONTOAUSZUG", "", Einstellungen
        .getEinstellung().getDateinamenmuster(), extension).get());
    fd.setFilterExtensions(new String[] { "*." + extension });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith("." + extension))
    {
      s = s + "*." + extension;
    }
    file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
  }

  private boolean generiereMitglied(Mitglied m, SollbuchungControl control)
      throws RemoteException, DocumentException
  {
    DIFFERENZ diff = DIFFERENZ.EGAL;
    if (control.isDifferenzAktiv() && control.getDifferenz().getValue() != null)
    {
      diff = (DIFFERENZ) control.getDifferenz().getValue();
    }
    
    MitgliedskontoNode node = new MitgliedskontoNode(m, (Date) control.getDatumvon().getValue(), 
        (Date) control.getDatumbis().getValue());
    
    Double limit = Double.valueOf(0d);
    if (control.isDoubleAuswAktiv()
        && control.getDoubleAusw().getValue() != null)
    {
      // Es ist egal ob der Betrag positiv oder negativ eingetragen wurde
      limit = Math.abs((Double) control.getDoubleAusw().getValue());
    }

    if (diff == DIFFERENZ.FEHLBETRAG && node.getIst() >= node.getSoll() - limit)
    {
      return false;
    }
    if (diff == DIFFERENZ.UEBERZAHLUNG && node.getSoll() >= node.getIst())
    {
      return false;
    }
    
    rpt.newPage();
    rpt.add(Einstellungen.getEinstellung().getName(), 20);
    rpt.add(
        String.format("Kontoauszug %s", Adressaufbereitung.getVornameName(m)),
        18);
    JVDateFormatTTMMJJJJ jv = new JVDateFormatTTMMJJJJ();
    rpt.add(String.format("Stand: %s", jv.format(new Date())), 16);

    rpt.addHeaderColumn(" ", Element.ALIGN_CENTER, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Datum", Element.ALIGN_CENTER, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Zweck", Element.ALIGN_LEFT, 50, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Zahlungsweg", Element.ALIGN_LEFT, 20,
        BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Soll", Element.ALIGN_RIGHT, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Ist", Element.ALIGN_RIGHT, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Differenz", Element.ALIGN_RIGHT, 20,
        BaseColor.LIGHT_GRAY);
    rpt.createHeader();

    generiereZeile(node);
    @SuppressWarnings("rawtypes")
    GenericIterator gi1 = node.getChildren();
    while (gi1.hasNext())
    {
      MitgliedskontoNode n1 = (MitgliedskontoNode) gi1.next();
      generiereZeile(n1);
      @SuppressWarnings("rawtypes")
      GenericIterator gi2 = n1.getChildren();
      while (gi2.hasNext())
      {
        MitgliedskontoNode n2 = (MitgliedskontoNode) gi2.next();
        generiereZeile(n2);
      }
    }
    rpt.closeTable();
    return true;
  }

  private void generiereZeile(MitgliedskontoNode node)
  {
    switch (node.getType())
    {
      case MitgliedskontoNode.MITGLIED:
        rpt.addColumn("Gesamt", Element.ALIGN_LEFT);
        break;
      case MitgliedskontoNode.SOLL:
        rpt.addColumn("Soll", Element.ALIGN_CENTER);
        break;
      case MitgliedskontoNode.IST:
        rpt.addColumn("Ist", Element.ALIGN_RIGHT);
        break;
    }
    rpt.addColumn((Date) node.getAttribute("datum"), Element.ALIGN_CENTER);
    rpt.addColumn((String) node.getAttribute("zweck1"), Element.ALIGN_LEFT);
    rpt.addColumn(Zahlungsweg.get((Integer) node.getAttribute("zahlungsweg")),
        Element.ALIGN_LEFT);
    rpt.addColumn((Double) node.getAttribute("soll"));
    rpt.addColumn((Double) node.getAttribute("ist"));
    rpt.addColumn((Double) node.getAttribute("differenz"));
  }

  private void zeigeDokument()
  {
    GUI.getStatusBar().setSuccessText("Kontoauszug erstellt");
    FileViewer.show(file);
  }
  
  String getDateiname(Mitglied m) throws RemoteException
  {
    String filename = m.getID() + "#";
    String email = StringTool.toNotNullString(m.getEmail());
    filename += email;
    return filename;
  }
}