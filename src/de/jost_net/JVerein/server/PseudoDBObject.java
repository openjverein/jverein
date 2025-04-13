package de.jost_net.JVerein.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Hilfsobjekt für den ExtendedDBIterator. Dieses Object enthält alle geholten
 * Attribute. diese können mittels <code>getAttribute("name")</code> geholt
 * werden. Dabei wir ein Object mit dem Tp aus der Datenbenk zurückgegeben. Da
 * je nach System auch BigDecimal oder Long dabei sein können, empfiehlt es sich
 * die Zahlen mittels
 * <code>((Number) it.getAttribute("anzahl")).intValue()</code> oder
 * <code>((Number) it.getAttribute("betrag")).doubleValue()</code> zu holen.
 */
public class PseudoDBObject extends AbstractDBObject implements DBObject
{

  private static final long serialVersionUID = 1L;

  public PseudoDBObject() throws RemoteException
  {
    super();
  }

  // Haelt die Eigenschaften des Objektes.
  private HashMap<String, Object> properties = new HashMap<>();

  /**
   * Fuellt das Objekt mit den Daten aus dem Resultset.
   * 
   * @param rs
   * @throws SQLException
   * @throws RemoteException
   */
  public void fillData(ResultSet rs) throws SQLException, RemoteException
  {
    ResultSetMetaData metadata = rs.getMetaData();
    int columnCount = metadata.getColumnCount();
    for (int i = 1; i <= columnCount; i++)
    {
      setAttribute(metadata.getColumnLabel(i).toLowerCase(), rs.getObject(i));
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName == null)
      return null;

    return properties.get(fieldName.toLowerCase());
  }

  /**
   * Speichert einen neuen Wert in den Properties und liefert den vorherigen
   * zurueck.
   * 
   * @param fieldName
   *          Name des Feldes.
   * @param value
   *          neuer Wert des Feldes.
   * @return vorheriger Wert des Feldes.
   * @throws RemoteException
   */
  public Object setAttribute(String fieldName, Object value)
      throws RemoteException
  {
    if (fieldName == null)
      return null;
    return properties.put(fieldName, value);
  }

  @Override
  protected String getTableName()
  {
    return null;
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return null;
  }
}
