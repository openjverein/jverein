package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanPostenDialog;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanPostenDialogAction implements Action
{
  private final WirtschaftsplanControl control;
  private final int art;

  public WirtschaftsplanPostenDialogAction(WirtschaftsplanControl control, int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof WirtschaftsplanNode))
    {
      return;
    }

    WirtschaftsplanNode node = (WirtschaftsplanNode) context;

    if (node.getType() != WirtschaftsplanNode.Type.POSTEN)
    {
      return;
    }

    try
    {
      WirtschaftsplanPostenDialog dialog = new WirtschaftsplanPostenDialog(
          node.getWirtschaftsplanItem());
      WirtschaftsplanItem item = dialog.open();

      if (item == null)
      {
        throw new OperationCanceledException();
      }

      node.setWirtschaftsplanItem(item);
      node.setSoll(item.getSoll());

      WirtschaftsplanNode parent = (WirtschaftsplanNode) node.getParent();
      control.reloadSoll(parent, art);
    }
    catch (OperationCanceledException ignored) {}
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }
}
