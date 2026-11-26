package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.gui.control.BelegControl;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BelegExport implements Exporter
{

  @Override
  public String getName()
  {
    return "Beleg Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != BelegControl.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {
      @Override
      public String getName()
      {
        return BelegExport.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    if (!file.exists() && !file.mkdirs())
    {
      monitor.setPercentComplete(100);
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw new ApplicationException(
          "Exportverzeichnis konnte nicht erstellt werden.");
    }

    if (!file.isDirectory())
    {
      monitor.setPercentComplete(100);
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw new ApplicationException("Das Exportziel ist kein Verzeichnis.");
    }

    if (objects == null || objects.length == 0)
    {
      monitor.setPercentComplete(100);
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw new ApplicationException("Keine Belege vorhanden");
    }

    List<Beleg> belegList = new ArrayList<>();

    for (Object o : objects)
    {
      if (o instanceof AbstractBelegDBObject ref)
      {
        DBIterator<Beleg> it = ref.getBelegList();
        while (it.hasNext())
        {
          Beleg beleg = it.next();
          // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
          // werden
          boolean found = false;
          for (Beleg b : belegList)
          {
            if (BeanUtil.equals(b, beleg))
            {
              found = true;
              break;
            }
          }
          if (!found)
          {
            belegList.add(beleg);
          }
        }
      }
      else if (o instanceof Beleg beleg)
      {
        belegList.add(beleg);
      }
    }
    Path base = file.toPath().toRealPath();
    int i = 0;
    for (Beleg beleg : belegList)
    {
      monitor.setPercentComplete(i++ * 100 / belegList.size());
      File belegFile;
      try
      {
        belegFile = beleg.getFile();
        if (belegFile == null || !belegFile.isFile() || !belegFile.canRead())
        {
          monitor.log("Belegdatei nicht vorhanden oder nicht lesbar: "
              + beleg.getBelegnummer());
          continue;
        }
      }
      catch (ApplicationException e)
      {
        monitor.log(beleg.getBelegnummer() + ": " + e.getMessage());
        continue;
      }

      String dateiname = VorlageUtil.getName(VorlageTyp.BELEG_EXPORT_DATEINAME,
          beleg, belegFile.getName());

      Path target = base.resolve(dateiname).normalize();
      if (!target.startsWith(base))
      {
        monitor.log("Ungültiger Exportpfad: " + dateiname);
        continue;
      }
      Path directory = target.getParent();
      Files.createDirectories(directory);

      Files.copy(belegFile.toPath(), target,
          StandardCopyOption.REPLACE_EXISTING);

      monitor.log(target.getFileName() + " exportiert");
    }
    monitor.setPercentComplete(100);
    monitor.setStatus(ProgressMonitor.STATUS_DONE);
  }

  @Override
  public String getDateiname(Object object)
  {
    return "Belegexport";
  }

  @Override
  public int getExportDialogOptions()
  {
    return 0;
  }

  @Override
  public boolean isDirectoryExport()
  {
    return true;
  }

}
