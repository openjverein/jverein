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
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import com.itextpdf.text.DocumentException;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.server.IVersand;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

public abstract class AbstractAusgabe
{

  private Settings settings;

  public AbstractAusgabe()
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Die Dokumente aufbereiten und ggf. per Mail versenden
   * 
   * @param list
   * @param control
   * @param pdfa
   * @param encrypt
   * @throws IOException
   * @throws ApplicationException
   * @throws DocumentException
   */
  public void aufbereiten(ArrayList<? extends DBObject> list, Ausgabeart art,
      String betreff, String text, boolean pdfa, boolean encrypt,
      boolean versanddatum)
      throws IOException, ApplicationException, DocumentException
  {
    String extension = "";
    switch (art)
    {
      case PDF:
      case PDF_EINZELN:
        extension = "pdf";
        break;
      case MAIL:
        extension = "zip";
        break;
    }
    // Wenn nur ein Dokument gedruck wird, immer PDF verwenden, so dass
    // der Dateiname und nicht der Ordner gewählt wird
    if (art == Ausgabeart.PDF_EINZELN && list.size() == 1)
    {
      art = Ausgabeart.PDF;
    }
    DBObject dateinameContext = null;
    if (list.size() == 1)
    {
      dateinameContext = list.get(0);
    }

    File file = getDateiAuswahl(extension,
        getDateiname(dateinameContext) + "." + extension,
        art == Ausgabeart.PDF_EINZELN);
    if (file == null)
    {
      return;
    }
    FormularAufbereitung formularaufbereitung = null;
    switch (art)
    {
      case PDF:
        formularaufbereitung = new FormularAufbereitung(file, pdfa, encrypt);
        for (DBObject object : list)
        {
          createPDF(getFormular(object), formularaufbereitung, file, object);
          if (versanddatum && object instanceof IVersand)
          {
            IVersand versand = (IVersand) object;
            versand.setVersanddatum(new Date());
            versand.store();
          }
        }
        closeDocument(formularaufbereitung, null);
        formularaufbereitung.showFormular();
        break;
      case PDF_EINZELN:
        for (DBObject object : list)
        {
          final File fx = new File(file.getParent() + File.separator
              + getDateiname(object) + "." + extension);
          formularaufbereitung = new FormularAufbereitung(fx, pdfa, encrypt);
          createPDF(getFormular(object), formularaufbereitung, fx, object);
          closeDocument(formularaufbereitung, object);
          if (versanddatum && object instanceof IVersand)
          {
            IVersand versand = (IVersand) object;
            versand.setVersanddatum(new Date());
            versand.store();
          }
        }
        GUI.getStatusBar()
            .setSuccessText("Die Dokumente wurden erstellt und unter: "
                + file.getParent() + " gespeichert.");
        break;
      case MAIL:
        try (ZipOutputStream zos = new ZipOutputStream(
            new FileOutputStream(file)))
        {
          for (DBObject object : list)
          {
            String name = getZipDateiname(object);
            File f = File.createTempFile(name, ".pdf");

            formularaufbereitung = new FormularAufbereitung(f, pdfa, encrypt);
            createPDF(getFormular(object), formularaufbereitung, f, object);
            closeDocument(formularaufbereitung, object);

            zos.putNextEntry(new ZipEntry(name + ".pdf"));
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
        }
        new ZipMailer(file, betreff, text);
        break;
    }
  }

  /**
   * Das Dokument erstellen
   * 
   * @param file
   * @param object
   * @throws IOException
   * @throws DocumentException
   * @throws ApplicationException
   */
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object)
      throws IOException, DocumentException, ApplicationException
  {
    if (formular == null)
    {
      throw new ApplicationException("Kein Formular angegeben.");
    }
    aufbereitung.writeForm(formular, getMap(object));

    // Formular speichern, um die hochgezählte nummer zu speichern
    formular.store();
    formular.setZaehlerToFormlink(formular.getZaehler());
  }

  /**
   * Schließt das Dakument. Kann überschrieben werden, um das Dokument zu
   * erweitern, zB. ZUGFeRD Rechnung erstellen
   * 
   * @param aufbereitung
   * @param object
   *          das Object. Null wenn ein Dokument für mehrere Einträge erstellt
   *          wurde
   * @throws IOException
   * @throws DocumentException
   */
  protected void closeDocument(FormularAufbereitung aufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    if (aufbereitung != null)
    {
      aufbereitung.closeFormular();
    }
  }

  /**
   * Holt den Dateinamen für die Datei im ZIP.<br>
   * Der Datiname muss folgende Form haben:<br>
   * MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf
   * 
   * @throws RemoteException
   */
  protected abstract String getZipDateiname(DBObject object)
      throws RemoteException;

  /**
   * Liefert die Map zu dem angegebenen Eintrag
   * 
   * @param object
   * @return
   * @throws RemoteException
   */
  protected abstract Map<String, Object> getMap(DBObject object)
      throws RemoteException;

  /**
   * Liefert den Dateinamen für das Object
   * 
   * @param object
   *          Das Object zu dem ein Dateiname erzeugt werden soll. Wenn es ohne
   *          Object-Bezug ist, wird null üergeben.
   * 
   * @return Der Dateiname
   * @throws RemoteException
   */
  protected abstract String getDateiname(DBObject object)
      throws RemoteException;

  /**
   * Liefert das Formular für das Object
   * 
   * @param object
   * @return
   * @throws RemoteException
   */
  protected abstract Formular getFormular(DBObject object)
      throws RemoteException;

  private File getDateiAuswahl(String extension, String dateiname,
      boolean einzelnePdfs) throws RemoteException
  {
    String s = null;
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (!einzelnePdfs)
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(dateiname);
      fd.setFilterExtensions(new String[] { "*." + extension });
      s = fd.open();
      if (s == null || s.length() == 0)
      {
        return null;
      }
      if (!s.toLowerCase().endsWith("." + extension))
      {
        s += "." + extension;
      }
    }
    else
    {
      DirectoryDialog dd = new DirectoryDialog(GUI.getShell(), SWT.SAVE);
      dd.setText("Ausgabepfad wählen.");
      if (path != null && path.length() > 0)
      {
        dd.setFilterPath(path);
        s = dd.open();
        if (s == null || s.length() == 0)
        {
          return null;
        }
        // Filename für das zip File
        s += File.separator + dateiname;
      }
    }

    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
    return file;
  }

}
