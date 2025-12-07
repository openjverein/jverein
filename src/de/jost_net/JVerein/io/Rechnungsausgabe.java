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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.zip.ZipEntry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.control.DruckMailControl;
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class Rechnungsausgabe extends AbstractAusgabe
{

  RechnungControl control;

  RechnungControl.TYP typ;

  public Rechnungsausgabe(Rechnung[] rechnungen, RechnungControl control,
      RechnungControl.TYP typ, String pdfMode)
      throws IOException, ApplicationException
  {
    this.control = control;
    this.typ = typ;
    boolean einzelnePdfs = pdfMode.equals(DruckMailControl.EINZELN)
        && rechnungen.length > 1;

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

    Ausgabeart art = (Ausgabeart) control.getAusgabeart().getValue();
    String extension = getExtension(art);

    String dateiname = null;
    if (rechnungen.length == 1)
    {
      Rechnung rechnung = rechnungen[0];
      if (typ == TYP.RECHNUNG)
      {
        dateiname = VorlageUtil.getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME,
            rechnung, rechnung.getMitglied()) + "." + extension;
      }
      else
      {
        dateiname = VorlageUtil.getName(VorlageTyp.MAHNUNG_MITGLIED_DATEINAME,
            rechnung, rechnung.getMitglied()) + "." + extension;
      }
    }
    else
    {
      if (typ == TYP.RECHNUNG)
      {
        dateiname = VorlageUtil.getName(VorlageTyp.RECHNUNG_DATEINAME) + "."
            + extension;
      }
      else
      {
        dateiname = VorlageUtil.getName(VorlageTyp.MAHNUNG_DATEINAME) + "."
            + extension;
      }
    }
    file = getDateiAuswahl(extension, dateiname, einzelnePdfs, control,
        control.getSettings());
    if (file == null)
    {
      return;
    }

    init(art, einzelnePdfs, true);
    aufbereitung(formular, rechnungen, einzelnePdfs);
  }

  public void aufbereitung(Formular formular, Rechnung[] rechnungen,
      boolean einzelnePdfs) throws IOException, ApplicationException
  {
    for (Rechnung re : rechnungen)
    {
      switch ((Ausgabeart) control.getAusgabeart().getValue())
      {
        case DRUCK:
          if (einzelnePdfs)
          {
            VorlageTyp vtyp = VorlageTyp.RECHNUNG_MITGLIED_DATEINAME;
            if (typ == TYP.MAHNUNG)
            {
              vtyp = VorlageTyp.MAHNUNG_MITGLIED_DATEINAME;
            }
            final File fx = new File(file.getParent() + File.separator
                + VorlageUtil.getName(vtyp, re, re.getMitglied()) + ".pdf");
            formularaufbereitung = new FormularAufbereitung(fx, true, false);
          }
          aufbereitenFormular(re, formularaufbereitung, formular);
          if (einzelnePdfs)
          {
            formularaufbereitung.closeFormular();
            formularaufbereitung.addZUGFeRD(re, typ == TYP.MAHNUNG);
          }
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
        if (!einzelnePdfs)
        {
          formularaufbereitung.showFormular();
          if (rechnungen.length == 1)
          {
            formularaufbereitung.addZUGFeRD(rechnungen[0], typ == TYP.MAHNUNG);
          }
        }
        else
        {
          GUI.getStatusBar()
              .setSuccessText("Die Dokumente wurden erstellt und unter: "
                  + file.getParent() + " gespeichert.");
        }
        break;
      case MAIL:
        zos.close();
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue());
        break;
    }
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
