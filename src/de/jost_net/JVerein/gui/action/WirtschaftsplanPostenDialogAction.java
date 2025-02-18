package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungPostenDialog;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanPostenDialogAction implements Action
{
  private final WirtschaftsplanungControl control;
  private final int art;

  public WirtschaftsplanPostenDialogAction(WirtschaftsplanungControl control, int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof WirtschaftsplanungNode))
    {
      return;
    }

    WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

    if (node.getType() != WirtschaftsplanungNode.Type.POSTEN)
    {
      return;
    }

    try
    {
      WirtschaftsplanungPostenDialog dialog = new WirtschaftsplanungPostenDialog(
          node.getWirtschaftsplanItem());
      WirtschaftsplanItem item = dialog.open();

      if (item == null)
      {
        throw new OperationCanceledException();
      }

      node.setWirtschaftsplanItem(item);
      node.setSoll(item.getSoll());

      WirtschaftsplanungNode parent = (WirtschaftsplanungNode) node.getParent();
      control.reloadSoll(parent, art);
    }
    catch (OperationCanceledException ignored) {}
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }
}
