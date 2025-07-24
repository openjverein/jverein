package de.jost_net.JVerein.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;

/**
 * Hilfsiterator um Benutzerdefiniert SQL Listen zu Holen. Hier können auch
 * komplexere SQL Selects ausgefürt werden mit Agregatfunktionen, Group By,
 * Rollup, Having, Left Join.
 * 
 * @param <T>
 *          der konkrete Typ. Sollte immer ein PseudoDBObject sein.
 */
public class ExtendedDBIterator<T extends AbstractDBObject>
    implements DBIterator<T>
{

  private String table;

  private T object = null;

  private String columns = "";

  private String filter = "";

  private String order = "";

  private String having = "";

  private String group = "";

  private boolean rollup = false;

  private int limit = -1;

  private ArrayList<Object> params = new ArrayList<>();

  private ArrayList<Object> whereParams = new ArrayList<>();

  private ArrayList<Object> havingParams = new ArrayList<>();

  private ArrayList<Object> joinParams = new ArrayList<>();

  private String joins = "";

  private List<T> list = new ArrayList<>();

  private int index = 0;

  private boolean initialized = false;

  /**
   * Hilfsiterator um Benutzerdefiniert SQL Listen zu Holen Hier können auch
   * komplexere SQL Selects ausgefürt werden mit Agregatfunktionen, Group By,
   * Rollup, Having, Left Join
   * 
   * @param table
   *          die Haupttabelle
   * @throws RemoteException
   */
  public ExtendedDBIterator(String table) throws RemoteException
  {
    this.table = table;
  }

  /**
   * Fügt dem Querry eine Spalte hinzu Hier können einfache Spaltennamen wie
   * "buchung" oder "buchungen.name" aber auch Agregatfunktionen wie "count(id)"
   * oder "sum(betrag) as summe" verwendet werden.
   * 
   * @param column
   * @param p...
   *          Objekt-Array um zusaetzliche Parameter anzugeben, mit denen dann
   *          ein PreparedStatement gefuellt wird.
   * @throws RemoteException
   */
  public void addColumn(String column, Object... p) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (this.columns.equals(""))
    {
      this.columns = column;
    }
    else
    {
      this.columns += "," + column;
    }

    if (p != null)
    {
      for (Object o : p)
      {
        this.params.add(o);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#setOrder(java.lang.String)
   */
  public void setOrder(String order) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    this.order = order;
  }

  /**
   * Fügt eine GroupBy definition hinzu
   * 
   * @param having
   *          die GROUP BY spalte
   * @throws RemoteException
   */
  public void addGroupBy(String group) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (this.group.equals(""))
    {
      this.group = group;
    }
    else
    {
      this.group += "," + group;
    }
  }

  /**
   * Fügt eine Having bedingung hinzu
   * 
   * @param having
   *          die HAVING bedingung
   * @param p...
   *          Objekt-Array um zusaetzliche Parameter anzugeben, mit denen dann
   *          ein PreparedStatement gefuellt wird.
   * @throws RemoteException
   */
  public void addHaving(String having, Object... p) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (this.having.equals(""))
    {
      this.having = "(" + having + ")";
    }
    else
    {
      this.having += " AND (" + having + ")";
    }

    if (p != null)
    {
      for (Object o : p)
      {
        havingParams.add(o);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#setLimit(int)
   */
  public void setLimit(int i) throws RemoteException
  {
    this.limit = i;
  }

  /**
   * Bei der Verwendung von GROUP BY wir ein ROLLUP erzeugt
   * 
   * @throws RemoteException
   */
  public void setRollup() throws RemoteException
  {
    rollup = true;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String)
   */
  public void addFilter(String filter) throws RemoteException
  {
    this.addFilter(filter, (Object[]) null);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String,
   *      java.lang.Object[])
   */
  public void addFilter(String filter, Object... p) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (filter == null)
      return; // no filter given

    if ("".equals(this.filter))
    {
      this.filter = " (" + filter + ")";
    }
    else
    {
      this.filter += " AND (" + filter + ")";
    }

    if (p != null)
    {
      for (Object o : p)
      {
        this.whereParams.add(o);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#join(java.lang.String)
   */
  public void join(String table) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (table == null)
      return;

    this.joins += " JOIN " + table;
  }

  /**
   * Fügt einen JOIN hinzu Es ist auch AS erlaubt, zB. "buchung as
   * steuerbuchung", auch subselects sind möglich "(SELECT COUNT(*) FROM
   * buchung) as anzahl"
   * 
   * @param table
   *          die zu joninende Tabelle
   * @param on
   *          Die on clausel
   * @param p...
   *          Objekt-Array um zusaetzliche Parameter anzugeben, mit denen dann
   *          ein PreparedStatement gefüllt wird.
   * @throws RemoteException
   */
  public void join(String table, String on, Object... p) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (table == null || on == null)
      return;

    this.joins += " JOIN " + table + " ON " + on;

    if (p != null)
    {
      for (Object o : p)
      {
        joinParams.add(o);
      }
    }
  }

  /**
   * Fügt einen LEFT JOIN hinzu Es ist auch AS erlaubt, zB. "buchung as
   * steuerbuchung", auch subselects sind möglich "(SELECT COUNT(*) FROM
   * buchung) as anzahl"
   * 
   * @param table
   *          die zu joninende Tabelle
   * @param on
   *          Die on clausel
   * @param p...
   *          Objekt-Array um zusaetzliche Parameter anzugeben, mit denen dann
   *          ein PreparedStatement gefüllt wird.
   * @throws RemoteException
   */
  public void leftJoin(String table, String on, Object... p)
      throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (table == null || on == null)
      return;

    this.joins += " LEFT JOIN " + table + " ON " + on;

    if (p != null)
    {
      for (Object o : p)
      {
        joinParams.add(o);
      }
    }
  }

  /**
   * Baut das SQL-Statement für die Liste zusammen.
   * 
   * @return das erzeugte Statement.
   */
  private String prepareSQL()
  {
    String sql = "SELECT ";

    // Wenn nichts angegeben, nehmen wir alle Spalten (*)
    if (columns.isBlank())
    {
      sql += "* ";
    }
    else
    {
      sql += columns + " ";
    }

    sql += " FROM " + table + " ";

    if (!joins.isBlank())
    {
      sql += joins + " ";
    }

    if (!filter.isBlank())
    {
      sql += "WHERE " + filter + " ";
    }

    if (!group.isBlank())
    {
      sql += "GROUP BY " + group + " ";
      if (rollup)
      {
        sql += "WITH ROLLUP ";
      }
    }

    if (!having.isBlank())
    {
      sql += "HAVING " + having + " ";
    }

    sql += order + " ";

    if (limit > 0)
      sql += "LIMIT " + Integer.toString(this.limit);

    return sql;
  }

  /**
   * Initialisiert den Iterator.
   * 
   * @throws RemoteException
   */
  @SuppressWarnings("unchecked")
  private void init() throws RemoteException
  {
    if (this.initialized)
      return; // allready initialzed

    // Die Parameter in der richtigen Reihenfolge hinzufügen
    params.addAll(joinParams);
    params.addAll(whereParams);
    params.addAll(havingParams);

    Einstellungen.getDBService().execute(prepareSQL(), params.toArray(), rs -> {
      while (rs.next())
      {
        PseudoDBObject o = new PseudoDBObject();
        o.fillData(rs);
        list.add((T) o);
      }
      return null;
    });
    this.initialized = true;
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
  {
    init();
    return (index < list.size() && list.size() > 0);
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#next()
   */
  public T next() throws RemoteException
  {
    init();
    try
    {
      return list.get(index++);
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage());
    }
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#previous()
   */
  public T previous() throws RemoteException
  {
    init();
    try
    {
      return list.get(index--);
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage());
    }
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#size()
   */
  public int size() throws RemoteException
  {
    init();
    return list.size();
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#begin()
   */
  public void begin() throws RemoteException
  {
    this.index = 0;
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#contains(de.willuhn.datasource.GenericObject)
   */
  public T contains(T other) throws RemoteException
  {
    init();

    if (other == null)
      return null;

    if (!other.getClass().equals(object.getClass()))
      return null;

    T object = null;
    for (int i = 0; i < list.size(); ++i)
    {
      object = list.get(i);
      if (object.equals(other))
        return object;
    }

    return null;
  }
}
