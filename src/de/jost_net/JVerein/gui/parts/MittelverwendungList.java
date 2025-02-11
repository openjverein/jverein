/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.willuhn.datasource.rmi.ResultSetExtractor;

public class MittelverwendungList
{

  protected Date datumvon = null;

  protected Date datumbis = null;

  protected static double LIMIT = 0.005;

  protected static String BLANK = " ";

  protected static String BLANKS = "          ";

  ResultSetExtractor rsd = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      if (!rs.next())
      {
        return Double.valueOf(0);
      }
      return Double.valueOf(rs.getDouble(1));
    }
  };

  ResultSetExtractor rsmap = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      HashMap<Long, String> map = new HashMap<>();
      while (rs.next())
      {
        map.put(rs.getLong(1), rs.getString(2));
      }
      return map;
    }
  };

  ResultSetExtractor rsmapa = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      HashMap<Long, String[]> map = new HashMap<>();
      while (rs.next())
      {
        map.put(rs.getLong(1),
            new String[] { rs.getString(2), rs.getString(3) });
      }
      return map;
    }
  };

  public void setDatumvon(Date datumvon)
  {
    this.datumvon = datumvon;
  }

  public void setDatumbis(Date datumbis)
  {
    this.datumbis = datumbis;
  }

  protected void addZeile(ArrayList<MittelverwendungZeile> zeilen, int status,
      Integer position, String bezeichnung, Double einnahme, Double ausgabe,
      String kommentar) throws RemoteException
  {
    Double summe = 0.0;
    if (status == MittelverwendungZeile.ART
        || status == MittelverwendungZeile.SUMME)
    {
      summe = einnahme + ausgabe;
      if (Math.abs(summe) < LIMIT)
      {
        summe = 0.0;
      }
    }
    if (einnahme != null && Math.abs(einnahme) < LIMIT)
    {
      einnahme = 0.0;
    }
    if (ausgabe != null && Math.abs(ausgabe) < LIMIT)
    {
      ausgabe = 0.0;
    }
    switch (status)
    {
      case MittelverwendungZeile.EINNAHME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            einnahme, null, kommentar));
        break;
      case MittelverwendungZeile.AUSGABE:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            ausgabe, null, kommentar));
        break;
      case MittelverwendungZeile.SUMME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            null, summe, kommentar));
        break;
      case MittelverwendungZeile.ART:
        zeilen.add(new MittelverwendungZeile(status, position, null, null,
            summe, kommentar, bezeichnung));
        break;
    }
  }

}
