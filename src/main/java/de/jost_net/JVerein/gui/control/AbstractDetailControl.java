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
    throw new ApplicationException(
        "Tabelle Spaltenauswahl und Export sind nicht unterstützt");
  }

  /**
   * Liefert den Titel für die Tabellenreports
   * 
   * @return
   * @throws ApplicationException
   */
  protected String getTableTitle() throws ApplicationException
  {
    throw new ApplicationException(
        "Tabelle Spaltenauswahl und Export sind nicht unterstützt");
  }

  /**
   * Liefert den Subtitel für die Tabellenreports
   * 
   * @return
   * @throws ApplicationException
   */
  protected String getTableSubtitle() throws ApplicationException
  {
    throw new ApplicationException(
        "Tabelle Spaltenauswahl und Export sind nicht unterstützt");
  }

  /**
   * Liefert den Dateinamen für die Tabellenreports
   * 
   * @return
   * @throws ApplicationException
   */
  protected String getTableDateiname() throws ApplicationException
  {
    throw new ApplicationException(
        "Tabelle Spaltenauswahl und Export sind nicht unterstützt");
  }

}
