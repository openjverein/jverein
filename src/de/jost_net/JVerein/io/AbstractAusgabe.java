package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.gui.control.DruckMailControl;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;

public class AbstractAusgabe
{
  FormularAufbereitung formularaufbereitung = null;

  ZipOutputStream zos = null;

  File file = null;

  public String getExtension(Ausgabeart art)
  {
    switch (art)
    {
      case DRUCK:
        return "pdf";
      case MAIL:
        return "zip";
      default:
        return "";
    }
  }

  public void init(Ausgabeart art, boolean einzelnePdfs, boolean pdfa)
      throws RemoteException, FileNotFoundException
  {
    switch (art)
    {
      case DRUCK:
        if (!einzelnePdfs)
        {
          formularaufbereitung = new FormularAufbereitung(file, pdfa, false);
        }
        break;
      case MAIL:
        zos = new ZipOutputStream(new FileOutputStream(file));
        break;
    }
  }

  public File getDateiAuswahl(String extension, String dateiname,
      boolean einzelnePdfs, DruckMailControl control, Settings settings)
      throws RemoteException
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
        s = s + "." + extension;
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
        s = s + File.separator + dateiname;
      }
    }

    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
    return file;
  }

}
