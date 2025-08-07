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
 * Attribute. diese können mittels <code>getAttribute("name")</code>,
 * <code>getDouble("name")</code> oder <code>getInteger("name")</code> geholt
 * werden.
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
  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName == null)
      return null;

    return properties.get(fieldName.toLowerCase());
  }

  /**
   * Lifert den Double Wert des Attributs
   * 
   * @param fieldName
   *          das Attribut
   * @return der Wert
   * @throws RemoteException
   */
  public Double getDouble(String fieldName) throws RemoteException
  {
    if (getAttribute(fieldName) == null)
    {
      return null;
    }
    return ((Number) getAttribute(fieldName)).doubleValue();
  }

  /**
   * Lifert den Double Wert des Attributs
   * 
   * @param fieldName
   *          das Attribut
   * @return der Wert
   * @throws RemoteException
   */
  public Integer getInteger(String fieldName) throws RemoteException
  {
    if (getAttribute(fieldName) == null)
    {
      return null;
    }
    return ((Number) getAttribute(fieldName)).intValue();
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
  @Override
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
