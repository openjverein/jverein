package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.ArbeitseinsatzAuswertungDialog;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ruft den Auswertungsdialog für Arbeitseinsätze auf
 */
public class ArbeitseinsatzAuswertungAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      ArbeitseinsatzAuswertungDialog dialog = new ArbeitseinsatzAuswertungDialog(
          AbstractDialog.POSITION_CENTER);
      dialog.open();
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Datenbank Zugriff!";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
