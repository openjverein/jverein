package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungNeuAction implements Action
{
  private WirtschaftsplanungControl control;

  public WirtschaftsplanungNeuAction(WirtschaftsplanungControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    //TODO
  }
}
