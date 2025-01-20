package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungNeuDialog;
import de.jost_net.JVerein.gui.view.WirtschaftsplanungView;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungNeuAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException {
    try {
      WirtschaftsplanungNeuDialog neuDialog = new WirtschaftsplanungNeuDialog();
      WirtschaftsplanungZeile wirtschaftsplan = neuDialog.open();
      GUI.startView(WirtschaftsplanungView.class, wirtschaftsplan);
    }
    catch (Exception e) {
      throw new ApplicationException(e);
    }
  }
}
