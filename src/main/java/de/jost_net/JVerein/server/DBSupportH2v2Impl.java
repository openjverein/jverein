/**********************************************************************
 * Kopie aus Hibiscus
 * Copyright (c) by willuhn software & services
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

package de.jost_net.JVerein.server;

import java.sql.Connection;

import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Datenbank-Supports fuer H2-Database
 * (http://www.h2database.com).
 */
public class DBSupportH2v2Impl extends AbstractDBSupportImpl
{

  private static final long serialVersionUID = 8429636916402991936L;

  public DBSupportH2v2Impl()
  {
    // H2-Datenbank verwendet uppercase Identifier
    Logger.info("switching dbservice to uppercase");
    System.setProperty(JVereinDBServiceImpl.class.getName() + ".uppercase",
        "true");
  }

  @Override
  public String getJdbcDriver()
  {
    return "org.h2v2.Driver";
  }

  @Override
  public String getJdbcPassword()
  {
    return "jverein";
  }

  @Override
  public String getJdbcUrl()
  {
    String url = "jdbc:h2v2:" + Application.getPluginLoader()
        .getPlugin(JVereinPlugin.class).getResources().getWorkPath()
        + "/h2db/jverein-new";

    if (JVereinDBService.SETTINGS.getBoolean("database.driver.h2.auto_server",
        false))
    {
      url += ";AUTO_SERVER=TRUE";
    }
    return url;
  }

  @Override
  public String getJdbcUsername()
  {
    return "jverein";
  }

  @Override
  public void checkConsistency(Connection conn) throws ApplicationException
  {
    if (!Application.inClientMode())
    {
      try
      {
        new JVereinUpdateProvider(conn,
            Application.getCallback().getStartupMonitor());
      }
      catch (Exception e2)
      {
        Logger.error("Datenbankupdate kann nicht ausgeführt werden.", e2);
        throw new ApplicationException(e2.getMessage(), e2);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  @Override
  public String getSQLTimestamp(String content)
  {
    return content;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  @Override
  public boolean getInsertWithID()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  @Override
  public void checkConnection(Connection conn)
  {
    // brauchen wir bei nicht, da Embedded
  }
}
