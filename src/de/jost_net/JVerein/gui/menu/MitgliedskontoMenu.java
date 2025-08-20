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
package de.jost_net.JVerein.gui.menu;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.IstbuchungEditAction;
import de.jost_net.JVerein.gui.action.IstbuchungLoesenAction;
import de.jost_net.JVerein.gui.action.GesamtrechnungNeuAction;
import de.jost_net.JVerein.gui.action.RechnungNeuAction;
import de.jost_net.JVerein.gui.action.SollbuchungEditAction;
import de.jost_net.JVerein.gui.action.SollbuchungLoeschenAction;
import de.jost_net.JVerein.gui.action.SollbuchungRechnungAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungNeuAction;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu, welches an MitgliedskontenListen gehangen werden kann.
 */
public class MitgliedskontoMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Mitgliedskonten.
   */
  public MitgliedskontoMenu()
  {
    addItem(new SollItem("Sollbuchung bearbeiten", new SollbuchungEditAction(),
        "text-x-generic.png"));
    addItem(new SollOhneIstItem("Sollbuchung löschen",
        new SollbuchungLoeschenAction(), "user-trash-full.png"));
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        addItem(new MitRechnungItem("Rechnung anzeigen",
            new SollbuchungRechnungAction(), "file-invoice.png"));
        addItem(new OhneRechnungItem("Rechnung(en) erstellen",
            new RechnungNeuAction(), "file-invoice.png"));
        addItem(new MultiItem("Gesamtrechnung erstellen",
            new GesamtrechnungNeuAction(), "file-invoice.png"));
      }
    }
    catch (RemoteException e)
    {
      // Dann nicht anzeigen
    }
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SollMitIstItem("Istbuchung bearbeiten",
        new IstbuchungEditAction(), "text-x-generic.png"));
    addItem(new SollMitIstItem("Istbuchung von Sollbuchung lösen",
        new IstbuchungLoesenAction(), "unlocked.png"));
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        addItem(ContextMenuItem.SEPARATOR);
        addItem(new SpendenbescheinigungItem("Geldspendenbescheinigung",
            new SpendenbescheinigungNeuAction(Spendenart.GELDSPENDE),
            "file-invoice.png"));
        addItem(new MitgliedItem("Sachspendenbescheinigung",
            new SpendenbescheinigungNeuAction(Spendenart.SACHSPENDE),
            "file-invoice.png"));
      }
    }
    catch (RemoteException e)
    {
      // Dann nicht anzeigen
    }
  }

  private static class MitgliedItem extends CheckedSingleContextMenuItem
  {

    /**
     * @param text
     * @param action
     */
    private MitgliedItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() == MitgliedskontoNode.MITGLIED)
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      return super.isEnabledFor(o);
    }
  }

  private static class SollItem extends CheckedSingleContextMenuItem
  {

    /**
     * @param text
     * @param action
     */
    private SollItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() == MitgliedskontoNode.SOLL)
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      return super.isEnabledFor(o);
    }
  }

  private static class SollOhneIstItem extends CheckedSingleContextMenuItem
  {

    private SollOhneIstItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() == MitgliedskontoNode.SOLL)
        {
          DBIterator<Buchung> it;
          try
          {
            it = Einstellungen.getDBService().createList(Buchung.class);
            it.addFilter(Buchung.SOLLBUCHUNG + " = ?",
                new Object[] { mkn.getID() });
            if (it.size() == 0)
            {
              return true;
            }
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
          return false;
        }
        else
        {
          return false;
        }
      }
      return super.isEnabledFor(o);
    }
  }

  private static class SollMitIstItem extends CheckedSingleContextMenuItem
  {

    /**
     * @param text
     * @param action
     */
    private SollMitIstItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() == MitgliedskontoNode.IST)
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      return super.isEnabledFor(o);
    }
  }

  private static class SpendenbescheinigungItem
      extends CheckedSingleContextMenuItem
  {

    private SpendenbescheinigungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() == MitgliedskontoNode.MITGLIED)
        {
          return true;
        }
        else if (mkn.getType() == MitgliedskontoNode.IST)
        {
          try
          {
            Object ob = Einstellungen.getDBService().createObject(Buchung.class,
                mkn.getID());
            if (ob != null)
            {
              Buchung b = (Buchung) ob;
              if (b.getBuchungsart().getSpende())
              {
                return true;
              }
            }
          }
          catch (Exception e)
          {
            return false;
          }
        }
      }
      return false;
    }
  }

  private static class OhneRechnungItem extends CheckedSingleContextMenuItem
  {

    private OhneRechnungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() != MitgliedskontoNode.SOLL)
        {
          return false;
        }

        try
        {
          Sollbuchung sollb = Einstellungen.getDBService()
              .createObject(Sollbuchung.class, mkn.getID());
          return sollb.getRechnung() == null;
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

  private static class MitRechnungItem extends CheckedSingleContextMenuItem
  {

    private MitRechnungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {

      if (o instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) o;
        if (mkn.getType() != MitgliedskontoNode.SOLL)
        {
          return false;
        }

        try
        {
          Sollbuchung sollb = Einstellungen.getDBService()
              .createObject(Sollbuchung.class, mkn.getID());
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
