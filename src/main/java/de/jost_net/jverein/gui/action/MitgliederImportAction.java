package de.jost_net.jverein.gui.action;

import de.jost_net.jverein.gui.dialogs.ImportDialog;
import de.jost_net.jverein.gui.view.DokumentationUtil;
import de.jost_net.jverein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliederImportAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      ImportDialog d = new ImportDialog(null, Mitglied.class, true,
          DokumentationUtil.MITGLIEDIMPORT);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while importing transfers", e);
      GUI.getStatusBar()
          .setErrorText("Fehler beim Importieren von MItgliedern");
    }
  }

}
