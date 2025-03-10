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

package de.jost_net.JVerein.gui.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Einstellung;
import de.jost_net.JVerein.rmi.Version;
import de.jost_net.JVerein.util.JVDateFormatJJJJMMTT;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Einspielen eines XML-Backups.
 */
public class BackupRestoreAction implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context)
  {
    try
    {
      if (Einstellungen.getDBService().createList(Einstellung.class).size() > 0)
      {
        String text = "Die JVerein-Datenbank enthält bereits Daten.\n"
            + "Das Backup kann nur in eine neue JVerein-Installation importiert werden.";
        Application.getCallback().notifyUser(text);
        return;
      }

      DBTransaction.starten();
      
      // Vom System eingefügte Sätze löschen. Ansonsten gibt es duplicate keys      
      DBIterator<EigenschaftGruppe> iteigr = Einstellungen.getDBService()
          .createList(EigenschaftGruppe.class);
      while (iteigr.hasNext())
      {
        EigenschaftGruppe gr = (EigenschaftGruppe) iteigr.next();
        gr.delete();
      }

    }
    catch (Exception e1)
    {
      DBTransaction.rollback();
      Logger.error("Fehler: ", e1);
    }

    FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN);
    fd.setFileName(
        "jverein-" + new JVDateFormatJJJJMMTT().format(new Date()) + ".xml");
    fd.setFilterExtensions(new String[] { "*.xml" });
    fd.setText("Bitte wählen Sie die Backup-Datei aus");
    String f = fd.open();
    if (f == null || f.length() == 0)
    {
      DBTransaction.rollback();
      return;
    }

    final File file = new File(f);
    if (!file.exists())
    {
      DBTransaction.rollback();
      return;
    }

    Application.getController().start(new BackgroundTask()
    {

      private boolean cancel = false;

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      @SuppressWarnings("unused")
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        monitor.setStatusText("Importiere Backup");
        Logger.info("importing backup " + file.getAbsolutePath());
        final ClassLoader loader = Application.getPluginLoader()
            .getPlugin(JVereinPlugin.class).getManifest().getClassLoader();

        Reader reader = null;
        try
        {
          InputStream is = new BufferedInputStream(new FileInputStream(file));
          reader = new XmlReader(is, new ObjectFactory()
          {

            @Override
            @SuppressWarnings("rawtypes")
            public GenericObject create(String type, String id, Map values)
                throws Exception
            {
              @SuppressWarnings("unchecked")
              AbstractDBObject object = (AbstractDBObject) Einstellungen
                  .getDBService().createObject(
                      (Class<AbstractDBObject>) loader.loadClass(type), null);
              object.setID(id);
              Iterator i = values.keySet().iterator();
              while (i.hasNext())
              {
                String name = (String) i.next();
                object.setAttribute(name, values.get(name));
              }
              return object;
            }

          });

          long count = 1;
          GenericObject o = null;
          String classOld = null;
          while ((o = reader.read()) != null)
          {
            if(isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Backup abgebrochen");
              monitor.setPercentComplete(100);
              DBTransaction.rollback();
              return;
            }
            if(classOld != null && !o.getClass().getSimpleName().equals(classOld))
            {
              monitor.setStatusText(String.format("%s importiert", classOld));
              classOld = o.getClass().getSimpleName();
            }
            if(classOld == null)
            {
              classOld = o.getClass().getSimpleName();
            }
            
            try
            {
              if(o instanceof Version)
              {
                int vBackup = ((Version)o).getVersion();
                int vDB = ((Version)Einstellungen.getDBService().createObject(Version.class, "1")).getVersion();
                if(vBackup != vDB)
                {
                  String text = "Die Datenbank Version (" + vDB + ") entspricht nicht der des Backups (" + vBackup +").\n"
                      + "Das Backup kann nur in eine identische Datenbank Version importiert werden.";
                  Application.getCallback().notifyUser(text);
                  monitor.setStatus(ProgressMonitor.STATUS_ERROR);
                  monitor.setStatusText("Backup abgebrochen");
                  monitor.setPercentComplete(100);
                  DBTransaction.rollback();
                  return;
                }
                continue;
              }
              ((AbstractDBObject) o).insert();
              if(o instanceof Einstellung)
              {
                Einstellungen.reloadEinstellung();
              }
            }
            catch (Exception e)
            {
              //Fehler Bei Adresstyp ignorieren, da hier bereits "Spender" und "Mitglied" existiert und es einen DUPLICATE KEY gibt
              if(!(o instanceof Mitgliedstyp))
              {
                Logger.error("unable to import " + o.getClass().getName() + ":"
                    + o.getID() + ", skipping", e);
                monitor.log(String.format("  %s fehlerhaft %s, überspringe",
                    BeanUtil.toString(o), e.getMessage()));
              }
            }

            if (count++ % 1000 == 0)
              monitor.addPercentComplete(1);
          }
          if(o != null)
            monitor.setStatusText(String.format("%s importiert", o.getClass().getSimpleName()));

          DBTransaction.commit();;
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Backup importiert");
          monitor.setPercentComplete(100);
        }
        catch (Exception e)
        {
          DBTransaction.rollback();
          Logger.error("error while importing data", e);
          throw new ApplicationException(e.getMessage());
        }
        finally
        {
          if (reader != null)
          {
            try
            {
              reader.close();
              Logger.info("backup imported");
            }
            catch (Exception e)
            {
              /* useless */}
          }
        }
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      @Override
      public boolean isInterrupted()
      {
        return this.cancel;
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      @Override
      public void interrupt()
      {
        this.cancel = true;
      }

    });
  }
}
