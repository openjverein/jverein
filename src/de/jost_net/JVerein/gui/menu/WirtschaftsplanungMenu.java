/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.gui.dialogs.DropdownDialog;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungPostenDialog;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class WirtschaftsplanungMenu extends ContextMenu
{
  @SuppressWarnings("rawtypes")
  public WirtschaftsplanungMenu(int art, WirtschaftsplanungControl control)
  {
    addItem(new BuchungsklasseItem("Buchungsart hinzufügen", context -> {
      WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;
      try
      {
        @SuppressWarnings("rawtypes") GenericIterator childrenIterator = node.getChildren();
        List<WirtschaftsplanungNode> items = new ArrayList<>();

        while (childrenIterator.hasNext())
        {
          WirtschaftsplanungNode child = (WirtschaftsplanungNode) childrenIterator.next();
          items.add(child);
        }

        DBIterator<Buchungsart> iterator;
        List<Buchungsart> buchungsarten = new ArrayList<>();

        iterator = Einstellungen.getDBService().createList(Buchungsart.class);
        iterator.addFilter("art = ?", art);
        if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
        {
          String buchungsklasseId = node.getBuchungsklasse().getID();
          iterator.addFilter("buchungsklasse = ?", buchungsklasseId);
        }

        while (iterator.hasNext())
        {
          Buchungsart buchungsart = iterator.next();
          if (items.stream().map(WirtschaftsplanungNode::getBuchungsart)
              .noneMatch(art1 -> {
                try
                {
                  return art1.equals(buchungsart);
                }
                catch (RemoteException e)
                {
                  throw new RuntimeException(e);
                }
              }))
          {
            buchungsarten.add(buchungsart);
          }
        }

        DropdownDialog<Buchungsart> dialog = new DropdownDialog<>(
            buchungsarten);
        Buchungsart buchungsart = dialog.open();

        node.addChild(new WirtschaftsplanungNode(node, buchungsart, art,
            control.getWirtschaftsplanungZeile()));

        if (art == 0)
        {
          control.getEinnahmen();
        }
        else
        {
          control.getAusgaben();
        }
      }
      catch (OperationCanceledException ignored) {}
      catch (Exception e)
      {
        throw new ApplicationException(
            "Fehler beim Hinzufügen der Buchungsart");
      }
    }, "list-add.png"));
    addItem(ContextMenuItem.SEPARATOR);

    addItem(new BuchungsartItem("Posten hinzufügen", context -> {
      WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

      try
      {
        WirtschaftsplanItem item = Einstellungen.getDBService()
            .createObject(WirtschaftsplanItem.class, null);
        item.setBuchungsartId(node.getBuchungsart().getID());
        item.setBuchungsklasseId(
            ((WirtschaftsplanungNode) node.getParent()).getBuchungsklasse()
                .getID());
        WirtschaftsplanungPostenDialog dialog = new WirtschaftsplanungPostenDialog(
            item);
        node.addChild(new WirtschaftsplanungNode(node, dialog.open()));

        control.reloadSoll(node, art);
      }
      catch (Exception e)
      {
        throw new ApplicationException("Fehler beim Anlegen des Postens");
      }
    }, "list-add.png"));
    addItem(ContextMenuItem.SEPARATOR);

    addItem(new CheckedContextMenuItem("Entfernen", context -> {
      if (!(context instanceof WirtschaftsplanungNode))
      {
        throw new ApplicationException("Fehler beim Löschen der Posten");
      }

      WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

      try
      {
        switch (node.getType())
        {
          case POSTEN:
            ((WirtschaftsplanungNode) node.getParent()).removeChild(node);
            control.reloadSoll((WirtschaftsplanungNode) node.getParent(), art);
            break;
          case BUCHUNGSART:
            GenericIterator iterator = node.getChildren();
            while (iterator.hasNext())
            {
              WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator.next();
              ((WirtschaftsplanungNode) currentNode.getParent()).removeChild(
                  currentNode);
            }
            if (node.getIst() == 0.)
            {
              ((WirtschaftsplanungNode) node.getParent()).removeChild(node);
            }

            control.reloadSoll(node, art);
            break;
          case BUCHUNGSKLASSE:
            GenericIterator iterator1 = node.getChildren();
            while (iterator1.hasNext())
            {
              boolean hasChanged = false;
              WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator1.next();
              GenericIterator iterator2 = currentNode.getChildren();
              while (iterator2.hasNext())
              {
                WirtschaftsplanungNode posten = (WirtschaftsplanungNode) iterator2.next();
                ((WirtschaftsplanungNode) posten.getParent()).removeChild(
                    posten);
                hasChanged = true;
              }

              if (currentNode.getIst() == 0.)
              {
                ((WirtschaftsplanungNode) currentNode.getParent()).removeChild(
                    currentNode);
              }

              if (hasChanged)
              {
                control.reloadSoll(currentNode, art);
              }
            }

            if (node.getIst() == 0.)
            {
              List items = art == 0 ?
                  control.getEinnahmen().getItems() :
                  control.getAusgaben().getItems();
              items.remove(node);

              if (art == 0)
              {
                control.getEinnahmen().setList(items);
              }
              else
              {
                control.getAusgaben().setList(items);
              }
            }
            break;
          case UNBEKANNT:
            throw new ApplicationException("Fehler beim Löschen der Posten");
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Löschen der Posten");
      }
    }, "user-trash-full.png"));
  }

  private static class BuchungsklasseItem extends CheckedContextMenuItem
  {
    private BuchungsklasseItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof WirtschaftsplanungNode)
      {
        WirtschaftsplanungNode node = (WirtschaftsplanungNode) o;
        return node.getType()
            .equals(WirtschaftsplanungNode.Type.BUCHUNGSKLASSE);
      }
      return false;
    }
  }

  private static class BuchungsartItem extends CheckedContextMenuItem
  {
    private BuchungsartItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof WirtschaftsplanungNode)
      {
        WirtschaftsplanungNode node = (WirtschaftsplanungNode) o;
        return node.getType().equals(WirtschaftsplanungNode.Type.BUCHUNGSART);
      }
      return false;
    }
  }
}
