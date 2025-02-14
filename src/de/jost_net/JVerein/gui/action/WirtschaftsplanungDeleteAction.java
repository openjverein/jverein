package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import java.util.Arrays;

public class WirtschaftsplanungDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Wirtschaftsplan[] wirtschaftsplaene;
    if (context instanceof WirtschaftsplanungZeile)
    {
      wirtschaftsplaene = new Wirtschaftsplan[] { ((WirtschaftsplanungZeile) context).getWirtschaftsplan() };
    }
    else if (context instanceof WirtschaftsplanungZeile[])
    {
      wirtschaftsplaene = (Wirtschaftsplan[]) Arrays.stream(((WirtschaftsplanungZeile[]) context)).map(
          WirtschaftsplanungZeile::getWirtschaftsplan).toArray();
    }
    else
    {
      throw new ApplicationException("Kein Wirtschaftsplan ausgewählt");
    }

    String mehrzahl = wirtschaftsplaene.length > 1 ? "Wirtschaftspläne" : "Wirtschaftsplan";
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(mehrzahl + " löschen");
    d.setPanelText(mehrzahl + " löschen?");
    d.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
    String text = "Wollen Sie diese" + (wirtschaftsplaene.length > 1 ? "":"s") + " " + mehrzahl + " wirklich löschen?"
        + "\nDiese Daten können nicht wieder hergestellt werden!";
    d.setText(text);

    try
    {
      if (!((Boolean) d.open()))
      {
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Löschen des Wirtschaftsplans", e);
      return;
    }

    DBTransaction.starten();

    try
    {
      for (Wirtschaftsplan wirtschaftsplan : wirtschaftsplaene)
      {
        wirtschaftsplan.delete();
      }
      DBTransaction.commit();
      GUI.getStatusBar().setSuccessText(mehrzahl + " gelöscht!");
    }
    catch (Exception e)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Löschen!");
      Logger.error("Fehler beim Löschen des Wirtschaftsplans", e);
      DBTransaction.rollback();
    }

    GUI.getCurrentView().reload();
  }
}
