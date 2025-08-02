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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.control.RechnungControl;
import de.jost_net.JVerein.gui.control.RechnungControl.TYP;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class Rechnungsausgabe
{

  RechnungControl control;

  GenericIterator<Rechnung> rechnungen = null;

  File file = null;

  FormularAufbereitung formularaufbereitung = null;

  ZipOutputStream zos = null;

  RechnungControl.TYP typ;

  @SuppressWarnings("unchecked")
  public Rechnungsausgabe(RechnungControl control, RechnungControl.TYP typ)
      throws IOException, ApplicationException
  {
    this.control = control;
    this.typ = typ;

    Formular formular = null;
    // Bei Mahnung ist Formular nötig, bei Rechnung ist es individuell in der
    // Rechnung angegeben
    if (typ == TYP.MAHNUNG)
    {
      Formular form = (Formular) control.getFormular(FormularArt.MAHNUNG)
          .getValue();
      if (form == null)
      {
        throw new IOException("Kein Mahnungsformular ausgewählt");
      }
      formular = (Formular) Einstellungen.getDBService()
          .createObject(Formular.class, form.getID());
    }

    Object context = control.getCurrentObject();
    if (context != null && context instanceof Rechnung[])
    {
      rechnungen = PseudoIterator.fromArray((Rechnung[]) context);
    }
    else if (context != null && context instanceof Rechnung)
    {
      rechnungen = PseudoIterator
          .fromArray(new Rechnung[] { (Rechnung) context });
    }
    else
    {
      rechnungen = control.getRechnungIterator();
    }

    if (rechnungen.size() == 0)
    {
      GUI.getStatusBar().setErrorText("Keine passende Rechnung gefunden.");
      return;
    }

    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        file = getDateiAuswahl("pdf");
        if (file == null)
        {
          return;
        }
        formularaufbereitung = new FormularAufbereitung(file, true, false);
        break;
      case MAIL:
        file = getDateiAuswahl("zip");
        if (file == null)
        {
          return;
        }
        zos = new ZipOutputStream(new FileOutputStream(file));
        break;
    }

    aufbereitung(formular);
  }

  public void aufbereitung(Formular formular)
      throws IOException, ApplicationException
  {
    while (rechnungen.hasNext())
    {
      Rechnung re = rechnungen.next();
      switch ((Ausgabeart) control.getAusgabeart().getValue())
      {
        case DRUCK:
          aufbereitenFormular(re, formularaufbereitung, formular);
          break;
        case MAIL:
          File f = File.createTempFile(getDateiname(re), ".pdf");
          formularaufbereitung = new FormularAufbereitung(f, true, false);
          aufbereitenFormular(re, formularaufbereitung, formular);
          formularaufbereitung.closeFormular();
          formularaufbereitung.addZUGFeRD(re, typ == TYP.MAHNUNG);
          zos.putNextEntry(new ZipEntry(getDateiname(re) + ".pdf"));
          FileInputStream in = new FileInputStream(f);
          // buffer size
          byte[] b = new byte[1024];
          int count;
          while ((count = in.read(b)) > 0)
          {
            zos.write(b, 0, count);
          }
          in.close();
          break;
      }
    }
    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        formularaufbereitung.showFormular();
        if (rechnungen.size() == 1)
        {
          rechnungen.begin();
          formularaufbereitung.addZUGFeRD(rechnungen.next(),
              typ == TYP.MAHNUNG);
        }
        break;
      case MAIL:
        zos.close();
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue());
        break;
    }
  }

  private File getDateiAuswahl(String extension) throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = control.getSettings().getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    if (rechnungen.size() == 1)
    {
      Rechnung rechnung = rechnungen.next();
      if (typ == TYP.RECHNUNG)
      {
        fd.setFileName(
            VorlageUtil.getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME,
                rechnung, rechnung.getMitglied()) + "." + extension);
      }
      else
      {
        fd.setFileName(
            VorlageUtil.getName(VorlageTyp.MAHNUNG_MITGLIED_DATEINAME, rechnung,
                rechnung.getMitglied()) + "." + extension);
      }
    }
    else
    {
      if (typ == TYP.RECHNUNG)
      {
        fd.setFileName(VorlageUtil.getName(VorlageTyp.RECHNUNG_DATEINAME) + "."
            + extension);
      }
      else
      {
        fd.setFileName(VorlageUtil.getName(VorlageTyp.MAHNUNG_DATEINAME) + "."
            + extension);
      }
    }
    fd.setFilterExtensions(new String[] { "*." + extension });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return null;
    }
    if (!s.toLowerCase().endsWith("." + extension))
    {
      s = s + "." + extension;
    }
    final File file = new File(s);
    control.getSettings().setAttribute("lastdir", file.getParent());
    return file;
  }

  void aufbereitenFormular(Rechnung re, FormularAufbereitung fa,
      Formular formular) throws RemoteException, ApplicationException
  {
    if (formular == null)
      formular = re.getFormular();

    if (re.getSollbuchungPositionList().size() == 0)
      return;

    Map<String, Object> map = new RechnungMap().getMap(re, null);
    map = new MitgliedMap().getMap(re.getMitglied(), map);
    map = new AllgemeineMap().getMap(map);
    fa.writeForm(formular, map);

    formular.store();

    formular.setZaehlerToFormlink(formular.getZaehler());
  }

  String getDateiname(Rechnung re) throws RemoteException
  {
    // MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf
    Mitglied m = re.getMitglied();
    String filename = "";
    if (typ == TYP.RECHNUNG)
    {
      filename = m.getID() + "#rechnung#" + re.getID() + "#";
    }
    else
    {
      filename = m.getID() + "#mahnung#" + re.getID() + "#";
    }
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename + "#" + typ.name();
  }

}
