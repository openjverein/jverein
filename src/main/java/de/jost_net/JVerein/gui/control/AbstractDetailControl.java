package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public abstract class AbstractDetailControl extends AbstractJVereinControl
{

  public AbstractDetailControl(AbstractView view)
  {
    super(view);
  }

  // Diese Methoden werden nur von Detail Views mit Tabellen benutzt.
  // Darum hier eine defaultimplementierung

  /**
   * Holten den TablePart mit der Auflistung aller Objecte
   * 
   * @return
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    return null;
  }

  /**
   * Liefert den Titel für die Tabellenreports
   * 
   * @return
   */
  protected String getTableTitle()
  {
    return null;
  }

  /**
   * Liefert den Subtitel für die Tabellenreports
   * 
   * @return
   */
  protected String getTableSubtitle()
  {
    return null;
  }

  /**
   * Liefert den Dateinamen für die Tabellenreports
   * 
   * @return
   */
  protected String getTableDateiname()
  {
    return null;
  }

}
