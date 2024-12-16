package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.view.WirtschaftsplanungView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class OpenWirtschaftsplanungAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(WirtschaftsplanungView.class, context);
  }
}
