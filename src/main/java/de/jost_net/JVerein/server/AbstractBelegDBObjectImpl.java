package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Datenbank-Objekt, dass einen Belg referenzieren kann.
 */
public abstract class AbstractBelegDBObjectImpl extends AbstractJVereinDBObject
    implements AbstractBelegDBObject
{
  private static final long serialVersionUID = 8251782884768611316L;

  public AbstractBelegDBObjectImpl() throws RemoteException
  {
    super();
  }

  @Override
  public AbstractBelegReferenz addBeleg(Beleg beleg)
      throws RemoteException, ApplicationException
  {
    if (isNewObject())
    {
      throw new ApplicationException(getObjektName() + " bitte erst speichern");
    }
    if (beleg == null || beleg.isNewObject())
    {
      throw new ApplicationException("Beleg bitte erst speichern");
    }
    updateCheck();
    DBIterator<Beleg> it = getBelegList();
    it.addFilter("beleg = ?", beleg.getID());
    if (it.hasNext())
    {
      throw new ApplicationException("Beleg '" + beleg.getBemerkung()
          + "' bereits bei " + getObjektName() + " hinterlegt");
    }

    AbstractBelegReferenz budo = Einstellungen.getDBService()
        .createObject(getBelegReferenzClass(), null);
    budo.setReferenz(this);
    budo.setBeleg(beleg);
    budo.store();

    return budo;
  }

  /**
   * Liefert die Klasse der BelegReferenz. zB. BelegBuchung.class
   * 
   * @return
   */
  protected abstract Class<? extends AbstractBelegReferenz> getBelegReferenzClass();

  /**
   * Entfernt die Zuordnung des Beleges zum Objekt
   * 
   * @param beleg
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void removeBeleg(Beleg beleg)
      throws RemoteException, ApplicationException
  {
    if (beleg == null || beleg.isNewObject())
    {
      throw new ApplicationException(
          "Beleg existiert nicht oder wurde noch nicht gespeichert");
    }
    updateCheck();
    DBIterator<AbstractBelegReferenz> it = Einstellungen.getDBService()
        .createList(getBelegReferenzClass());
    it.addFilter(getBelegReferenzTableName() + ".beleg = ?", beleg.getID());
    it.addFilter(getBelegReferenzTableName() + ".referenz = ?", getID());
    if (!it.hasNext())
    {
      throw new ApplicationException(
          "Beleg ist nicht bei " + getObjektName() + " hinterlegt");
    }
    it.next().delete();
  }

  /**
   * Liefert eine Liste mit allen Belegen die diesem Objekt zugeordnet sind
   * 
   * @return
   * @throws RemoteException
   */
  public DBIterator<Beleg> getBelegList() throws RemoteException
  {
    DBIterator<Beleg> it = Einstellungen.getDBService().createList(Beleg.class);
    it.join(getBelegReferenzTableName());
    it.addFilter(getBelegReferenzTableName() + ".beleg = beleg.id");
    it.addFilter(getBelegReferenzTableName() + ".referenz = ?", getID());
    return it;
  }

  protected abstract String getBelegReferenzTableName();
}
