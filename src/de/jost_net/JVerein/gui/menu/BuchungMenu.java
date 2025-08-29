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
import de.jost_net.JVerein.gui.action.AnlagenkontoNeuAction;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.BuchungBuchungsartZuordnungAction;
import de.jost_net.JVerein.gui.action.BuchungDeleteAction;
import de.jost_net.JVerein.gui.action.BuchungDuplizierenAction;
import de.jost_net.JVerein.gui.action.BuchungGegenbuchungAction;
import de.jost_net.JVerein.gui.action.BuchungGeprueftAction;
import de.jost_net.JVerein.gui.action.BuchungKontoauszugZuordnungAction;
import de.jost_net.JVerein.gui.action.BuchungProjektZuordnungAction;
import de.jost_net.JVerein.gui.action.BuchungSollbuchungZuordnungAction;
import de.jost_net.JVerein.gui.action.BuchungSteuerZuordnenAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungNeuAction;
import de.jost_net.JVerein.gui.action.SplitBuchungAction;
import de.jost_net.JVerein.gui.action.SplitbuchungBulkAufloesenAction;
import de.jost_net.JVerein.gui.action.SyntaxExportAction;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Buchungen.
 */
public class BuchungMenu extends ContextMenu
{
  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Buchungen.
   * 
   * @throws RemoteException
   */

  public BuchungMenu(BuchungsControl control) throws RemoteException
  {
    boolean geldkonto = control.getGeldkonto();
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new BuchungAction(false, control.getBuchungsList()),
        "text-x-generic.png"));
    addItem(new GeprueftBuchungItem("Als \"geprüft\" markieren",
        new BuchungGeprueftAction(true), "emblem-default.png", false));
    addItem(new GeprueftBuchungItem("Als \"ungeprüft\" markieren",
        new BuchungGeprueftAction(false), "edit-undo.png", true));
    addItem(new SingleBuchungItem("Duplizieren", new BuchungDuplizierenAction(),
        "edit-copy.png"));
    if (geldkonto)
    {
      addItem(new GegenBuchungItem("Gegenbuchung",
          new BuchungGegenbuchungAction(control), "edit-copy.png"));
    }
    addItem(new SplitBuchungItem("Splitbuchung", new SplitBuchungAction(),
        "edit-copy.png"));
    addItem(new AufloesenItem("Auflösen",
        new SplitbuchungBulkAufloesenAction(control), "unlocked.png"));
    addItem(new BuchungItem("Löschen", new BuchungDeleteAction(false),
        "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    if (geldkonto)
    {
      addItem(new MitgliedOeffnenItem("Mitglied anzeigen",
          new MitgliedDetailAction(), "user-friends.png"));
      addItem(new SingleGegenBuchungItem("Neues Anlagenkonto",
          new AnlagenkontoNeuAction(), "document-new.png"));
      try
      {
        if ((Boolean) Einstellungen
            .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
        {
          addItem(new SpendenbescheinigungMenuItem("Spendenbescheinigung",
              new SpendenbescheinigungNeuAction(), "file-invoice.png"));
        }
      }
      catch (RemoteException e)
      {
        // Dann nicht anzeigen
      }
    }
    addItem(new CheckedContextMenuItem("Buchungsart zuordnen",
        new BuchungBuchungsartZuordnungAction(), "view-refresh.png"));
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        addItem(new CheckedContextMenuItem("Steuer zuordnen",
            new BuchungSteuerZuordnenAction(), "view-refresh.png"));
      }
    }
    catch (RemoteException e)
    {
      // Dann nicht anzeigen
    }
    if (geldkonto)
    {
      addItem(new CheckedContextMenuItem("Sollbuchung zuordnen",
          new BuchungSollbuchungZuordnungAction(), "view-refresh.png"));
    }
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        addItem(new CheckedContextMenuItem("Projekt zuordnen",
            new BuchungProjektZuordnungAction(), "view-refresh.png"));
      }
    }
    catch (RemoteException e)
    {
      // Dann nicht anzeigen
    }
    if (geldkonto)
      addItem(new CheckedContextMenuItem("Kontoauszug zuordnen",
          new BuchungKontoauszugZuordnungAction(), "view-refresh.png"));
    Plugin syntax = Application.getPluginLoader()
        .getPlugin("de.willuhn.jameica.fibu.Fibu");
    if (syntax != null
        && syntax.getManifest().getVersion().compliesTo("2.10.5+"))
    {
      addItem(ContextMenuItem.SEPARATOR);
      addItem(new CheckedContextMenuItem("In SynTAX übernehmen",
          new SyntaxExportAction(), "document-save.png"));
    }
  }

  private static class SingleBuchungItem extends CheckedSingleContextMenuItem
  {
    private SingleBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        Buchung b = (Buchung) o;
        try
        {
          return b.getSplitId() == null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class BuchungItem extends CheckedContextMenuItem
  {
    private BuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o instanceof Buchung)
        {
          return ((Buchung) o).getSplitId() == null;
        }
        if (o instanceof Buchung[])
        {
          for (Buchung bu : ((Buchung[]) o))
          {
            if (bu.getSplitId() != null)
            {
              return false;
            }
          }
          return true;
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private static class SingleGegenBuchungItem
      extends CheckedSingleContextMenuItem
  {
    private SingleGegenBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        Buchung b = (Buchung) o;
        try
        {
          if ((b.getSplitId() != null)
              && (b.getSplitTyp() != SplitbuchungTyp.SPLIT))
          {
            return false;
          }
          if (b.getBuchungsart() != null)
          {
            return b.getBuchungsart().getArt() == ArtBuchungsart.UMBUCHUNG;
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class GegenBuchungItem extends CheckedContextMenuItem
  {
    private GegenBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        o = new Buchung[] { (Buchung) o };
      }
      if (o instanceof Buchung[])
      {

        try
        {
          for (Buchung b : (Buchung[]) o)
          {
            if ((b.getSplitId() != null)
                && (b.getSplitTyp() != SplitbuchungTyp.SPLIT))
            {
              return false;
            }
            if (b.getBuchungsart() == null
                || b.getBuchungsart().getArt() != ArtBuchungsart.UMBUCHUNG)
            {
              return false;
            }
          }
          return true;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class AufloesenItem extends CheckedContextMenuItem
  {
    private AufloesenItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o instanceof Buchung)
        {
          return ((Buchung) o).getSplitId() != null;
        }
        if (o instanceof Buchung[])
        {
          for (Buchung bu : ((Buchung[]) o))
          {
            if (bu.getSplitId() == null)
            {
              return false;
            }
          }
          return true;
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private static class SplitBuchungItem extends CheckedContextMenuItem
  {
    private SplitBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o instanceof Buchung)
        {
          return ((Buchung) o).getSplitId() == null;
        }
        if (o instanceof Buchung[])
        {
          Double betrag = ((Buchung[]) o)[0].getBetrag();
          for (Buchung bu : ((Buchung[]) o))
          {
            if ((bu.getSplitId() != null) || (bu.getBetrag() != betrag))
            {
              return false;
            }
          }
          return true;
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private static class MitgliedOeffnenItem extends CheckedContextMenuItem
  {
    private MitgliedOeffnenItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o instanceof Buchung)
        {
          return ((Buchung) o).getSollbuchung() != null;
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private static class GeprueftBuchungItem extends CheckedContextMenuItem
  {
    boolean geprueft;

    private GeprueftBuchungItem(String text, Action action, String icon,
        boolean geprueft)
    {
      super(text, action, icon);
      this.geprueft = geprueft;
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        Buchung b = (Buchung) o;
        try
        {
          return !geprueft ^ b.getGeprueft();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return true;
    }
  }

  private static class SpendenbescheinigungMenuItem
      extends CheckedSingleContextMenuItem
  {
    private SpendenbescheinigungMenuItem(String text, Action action,
        String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o instanceof Buchung)
        {
          Buchung b = (Buchung) o;
          if (b.getBuchungsart() != null)
          {
            return b.getBuchungsart().getSpende()
                && b.getSpendenbescheinigung() == null;
          }
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }
}
