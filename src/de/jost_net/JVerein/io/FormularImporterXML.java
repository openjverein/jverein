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
import java.util.HashMap;
import java.util.Iterator;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class FormularImporterXML implements Importer
{

  @SuppressWarnings("unchecked")
  @Override
  public void doImport(Object context, IOFormat format, File file,
      String encoding, ProgressMonitor monitor) throws Exception
  {

    if (file == null)
    {
      throw new ApplicationException("Keine zu importierende Datei ausgewählt");
    }

    if (format == null)
    {
      throw new ApplicationException("Kein Datei-Format ausgewählt");
    }

    HashMap<String, HashMap<String, String>> foreignMap = getForeignMap();
    HashMap<String, HashMap<String, String>> idMap = new HashMap<>();

    final ClassLoader loader = Application.getPluginLoader()
        .getManifest(JVereinPlugin.class).getClassLoader();
    Reader reader = null;
    try
    {
      DBTransaction.starten();

      reader = new XmlReader(new FileInputStream(file), (type, id, values) -> {
        AbstractDBObject object = (AbstractDBObject) Einstellungen
            .getDBService().createObject(
                (Class<AbstractDBObject>) loader.loadClass(type), null);
        object.setID(id); // ID setzen, sie muss vor dem Speichern wieder
                          // entfernt werden um nichts zu überschreiben

        Iterator<?> i = values.keySet().iterator();
        while (i.hasNext())
        {
          String name = (String) i.next();
          HashMap<String, String> fieldMap = foreignMap.get(type);
          if (fieldMap != null && fieldMap.get(name) != null)
          {
            if (idMap.get(fieldMap.get(name)) == null)
            {
              throw new ApplicationException(
                  "Kein Object der referenzierten Klasse " + fieldMap.get(name)
                      + " gefunden.");
            }
            String foreignId = idMap.get(fieldMap.get(name))
                .get(values.get(name).toString());
            if (foreignId == null)
            {
              throw new ApplicationException(
                  "Kein Object der referenzierten Klasse " + fieldMap.get(name)
                      + " mit id " + values.get(name).toString()
                      + " gefunden.");
            }
            object.setAttribute(name, foreignId);
          }
          else
          {
            object.setAttribute(name, values.get(name));
          }
        }
        return object;
      });

      if (monitor != null)
      {
        monitor.setStatusText("Lese Datei ein");
      }

      int created = 0;

      AbstractDBObject object = null;
      while ((object = (AbstractDBObject) reader.read()) != null)
      {
        if (monitor != null)
        {
          Object name = BeanUtil.toString(object);
          if (name != null && monitor != null)
          {
            monitor.log("Importiere " + name.toString());
          }
          if (created > 0 && created % 10 == 0 && monitor != null) // nur
          {
            // geschaetzt
            monitor.addPercentComplete(1);
          }
        }

        try
        {
          String idAlt = object.getID();
          object.setID(null);
          object.store();
          HashMap<String, String> map = idMap
              .getOrDefault(object.getClass().getName(), new HashMap<>());
          map.put(idAlt, object.getID());
          idMap.put(object.getClass().getName(), map);

          created++;
        }
        catch (ApplicationException ae)
        {
          if (monitor != null)
          {
            monitor.log("  " + ae.getMessage());
          }
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to import line", e);
          if (monitor != null)
          {
            monitor
                .log("Fehler beim Import des Datensatzes: " + e.getMessage());
          }
          throw e;
        }
      }
      DBTransaction.commit();
      if (monitor != null)
      {
        monitor.setStatusText(created + " Datensätze erfolgreich importiert");
        monitor.setPercentComplete(100);
      }
    }
    catch (OperationCanceledException oce)
    {
      DBTransaction.rollback();
      Logger.warn("operation cancelled");
      throw new ApplicationException("Import abgebrochen");
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      Logger.error("error while reading file", e);
      throw new ApplicationException("Fehler beim Import der XML-Datei");
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException e)
        {
          Logger.error("error while closing inputstream", e);
        }
      }
    }
  }

  private HashMap<String, HashMap<String, String>> getForeignMap()
  {
    HashMap<String, HashMap<String, String>> foreignMap = new HashMap<>();

    HashMap<String, String> map = new HashMap<>();
    map.put("formular", "de.jost_net.JVerein.server.FormularImpl");

    foreignMap.put("de.jost_net.JVerein.server.FormularfeldImpl", map);

    return foreignMap;
  }

  @Override
  public String getName()
  {
    return "Formular XML-Import";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Formular.class)
    {
      return null;
    }

    IOFormat f = new IOFormat()
    {
      @Override
      public String getName()
      {
        return FormularImporterXML.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.xml" };
      }
    };
    return new IOFormat[] { f };
  }
}
