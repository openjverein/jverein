package de.jost_net.JVerein.keys;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.ResultSetExtractor;

public class Ausfuehrung implements KeyEnum
{

  public static final String ALLE = "Alle";

  public static final String AKTIVE = "Aktive";

  public static final String NOCH_NICHT = "Noch nicht ausgeführt";

  private int key;

  private String text;

  private Ausfuehrung(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public static Ausfuehrung[] getList() throws RemoteException
  {
    final Vector<String> werte = new Vector<>();

    String sql = "select ausfuehrung from " + Zusatzbetrag.TABLE_NAME
        + " where ausfuehrung is not null "
        + "group by ausfuehrung order by ausfuehrung desc";
    JVereinDBService service = Einstellungen.getDBService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        while (rs.next())
        {
          werte.addElement(new JVDateFormatTTMMJJJJ().format(rs.getDate(1)));
        }
        return null;
      }
    };
    service.execute(sql, new Object[] {}, rs);

    Ausfuehrung[] liste = new Ausfuehrung[werte.size() + 3];
    liste[0] = new Ausfuehrung(1, ALLE);
    liste[1] = new Ausfuehrung(2, AKTIVE);
    liste[2] = new Ausfuehrung(3, NOCH_NICHT);
    int i = 3;
    for (String wert : werte)
    {
      liste[i] = new Ausfuehrung(i + 1, wert);
      i++;
    }
    return liste;
  }

  @Override
  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  @Override
  public String toString()
  {
    return text;
  }
}
