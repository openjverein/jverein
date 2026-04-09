package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.ArbeitseinsatzZusatzbetraegeDialog;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ruft den Abrechnungsdialog für Arbeitseinsätze auf
 */
public class ArbeitseinsatzZusatzbetraegeAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      ArbeitseinsatzZusatzbetraegeDialog dialog = new ArbeitseinsatzZusatzbetraegeDialog(
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
