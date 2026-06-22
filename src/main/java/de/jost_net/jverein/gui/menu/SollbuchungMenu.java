/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/

package de.jost_net.jverein.gui.menu;

import java.rmi.RemoteException;

import de.jost_net.jverein.Einstellungen;
import de.jost_net.jverein.Einstellungen.Property;
import de.jost_net.jverein.gui.action.DeleteAction;
import de.jost_net.jverein.gui.action.EditAction;
import de.jost_net.jverein.gui.action.GesamtrechnungNeuAction;
import de.jost_net.jverein.gui.action.GutschriftAction;
import de.jost_net.jverein.gui.action.MitgliedDetailAction;
import de.jost_net.jverein.gui.action.RechnungNeuAction;
import de.jost_net.jverein.gui.action.SollbuchungRechnungAction;
import de.jost_net.jverein.gui.parts.JVereinTablePart;
import de.jost_net.jverein.gui.view.SollbuchungDetailView;
import de.jost_net.jverein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Sollbuchungen.
 */
public class SollbuchungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer Sollbuchungen.
   */
  public SollbuchungMenu(JVereinTablePart part)
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new EditAction(SollbuchungDetailView.class, part),
        "text-x-generic.png"));
    addItem(new CheckedContextMenuItem("Löschen", new DeleteAction(),
        "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        addItem(new MitRechnungItem("Rechnung anzeigen",
            new SollbuchungRechnungAction(), "file-invoice.png"));
        addItem(new OhneRechnungItem("Rechnung erstellen",
            new RechnungNeuAction(), "file-invoice.png"));
        addItem(new MultiItem("Gesamtrechnung erstellen",
            new GesamtrechnungNeuAction(), "file-invoice.png"));
      }
    }
    catch (RemoteException e)
    {
      // Dann nicht anzeigen
    }
    addItem(new CheckedContextMenuItem("Gutschrift erstellen",
        new GutschriftAction(), "ueberweisung.png"));
  }

  private static class OhneRechnungItem extends CheckedContextMenuItem
  {

    private OhneRechnungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Sollbuchung)
      {
        Sollbuchung sollb = (Sollbuchung) o;
        try
        {
          return sollb.getRechnung() == null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return false;
      }
      // Bei mehreren Sollbuchungen zeigen wir es immer mit an
      return true;
    }
  }

  private static class MultiItem extends CheckedContextMenuItem
  {

    private MultiItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      return o instanceof Object[];
    }
  }

  private static class MitRechnungItem extends CheckedContextMenuItem
  {

    private MitRechnungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Sollbuchung)
      {
        Sollbuchung sollb = (Sollbuchung) o;
        try
        {
          return sollb.getRechnung() != null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return false;
      }
      return false;
    }
  }
}
