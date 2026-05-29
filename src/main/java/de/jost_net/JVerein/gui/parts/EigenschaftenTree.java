package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;

public class EigenschaftenTree extends TreePart
{

  public EigenschaftenTree(Mitglied mitglied) throws RemoteException
  {
    this(mitglied, "", true, null);
  }

  public EigenschaftenTree(Mitglied mitglied, String vorbelegung,
      boolean onlyChecked, Mitglied[] mitglieder) throws RemoteException
  {
    super(new EigenschaftenNode(mitglied, vorbelegung, onlyChecked, mitglieder),
        null);
    addSelectionListener(new EigenschaftListener());
    setFormatter(new EigenschaftTreeFormatter());
  }

  // Ändert den Status und das Icon des EigenschftenTree-Items
  static class EigenschaftListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      // "o" ist das Objekt, welches gerade markiert wurde
      GenericObjectNode o = (GenericObjectNode) event.data;

      if (o instanceof EigenschaftenNode)
      {
        EigenschaftenNode node = (EigenschaftenNode) o;
        if (node.getNodeType() == EigenschaftenNode.EIGENSCHAFTEN)
        {
          node.incPreset();
          TreeItem item = (TreeItem) event.item;
          new EigenschaftTreeFormatter().format(item);
        }
      }
    }
  }

  private static class EigenschaftTreeFormatter implements TreeFormatter
  {

    @Override
    public void format(TreeItem item)
    {
      EigenschaftenNode eigenschaftitem = (EigenschaftenNode) item.getData();
      if (eigenschaftitem.getNodeType() == EigenschaftenNode.ROOT)
      {
        item.setImage(SWTUtil.getImage("document-properties.png"));
      }
      else if (eigenschaftitem
          .getNodeType() == EigenschaftenNode.EIGENSCHAFTGRUPPE)
      {
        try
        {
          boolean pflicht = eigenschaftitem.getEigenschaftGruppe().getPflicht();
          boolean maxeins = eigenschaftitem.getEigenschaftGruppe().getMax1();
          if (pflicht && maxeins)
          {
            item.setImage(SWTUtil.getImage("pflicht-maxeins.png"));
          }
          if (pflicht && !maxeins)
          {
            item.setImage(SWTUtil.getImage("pflicht.png"));
          }
          if (!pflicht && maxeins)
          {
            item.setImage(SWTUtil.getImage("maxeins.png"));
          }
        }
        catch (RemoteException e)
        {

        }
      }
      else
      {
        if (eigenschaftitem.getPreset().equals(EigenschaftenNode.PLUS))
        {
          item.setImage(SWTUtil.getImage("list-add.png"));
        }
        else if (eigenschaftitem.getPreset().equals(EigenschaftenNode.MINUS))
        {
          item.setImage(SWTUtil.getImage("list-remove.png"));
        }
        else if (eigenschaftitem.getPreset().equals(EigenschaftenNode.CHECKED))
        {
          item.setImage(SWTUtil.getImage("tree-checked.png"));
        }
        else if (eigenschaftitem.getPreset()
            .equals(EigenschaftenNode.CHECKED_PARTLY))
        {
          item.setImage(SWTUtil.getImage("tree-checked-partly.png"));
        }
        else
        {
          item.setImage(SWTUtil.getImage("tree-empty.png"));
        }
      }
    }
  }
}
