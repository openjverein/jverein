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

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MailControl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.Eigenschaftenauswahl;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, ueber den man Empfänger für eine Mail auswählen kann.
 */
public class MailEmpfaengerAuswahlDialog extends AbstractDialog<Object>
{

  private MailControl control;

  private TextInput auswahlinput;

  private TextInput eigenschafteninput;

  private TextInput verknuepfunginput;

  public static final String MITGLIED_ID = "id";

  public static final String EIGENSCHAFT = "eigenschaft";

  private EigenschaftenAuswahlParameter param = null;

  private Eigenschaftenauswahl selection = Eigenschaftenauswahl.KEINE;

  public MailEmpfaengerAuswahlDialog(MailControl control, int position)
  {
    super(position);
    this.control = control;
    setTitle("Mail-Empfänger");
    setSize(650, 500);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Auswahl", getAuswahl());
    group.addLabelPair("Eigenschaften", getEigenschaften());
    group.addLabelPair("Verknüpfung", getVerknuepfung());

    control.getMitgliedMitMail().paint(parent);
    for (Object o : control.getMitgliedMitMail().getItems(true))
    {
      control.getMitgliedMitMail().setChecked(o, false);
    }

    ButtonArea b = new ButtonArea();

    b.addButton("Eigenschaften", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          EigenschaftenAuswahlDialog ead = new EigenschaftenAuswahlDialog(null,
              true, new MitgliedControl(null), true);
          param = ead.open();
          setSelection();
        }
        catch (OperationCanceledException oce)
        {
          return;
        }
        catch (Exception e)
        {
          throw new ApplicationException("Fehler" + e);
        }
      }
    });

    b.addButton("Alle", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        selection = Eigenschaftenauswahl.ALLE;
        setSelection();
      }
    });

    b.addButton("Keinen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        try
        {
          for (Object o : control.getMitgliedMitMail().getItems(false))
          {
            control.getMitgliedMitMail().setChecked(o, false);
          }
          selection = Eigenschaftenauswahl.KEINE;
          auswahlinput.setValue(selection.getText());
          eigenschafteninput.setValue(getEigenschaftenString());
          verknuepfunginput
              .setValue(param != null ? param.getVerknuepfung() : "");
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    b.addButton("Aktive Mitglieder", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        Eigenschaftenauswahl selection_old = selection;
        switch (selection_old)
        {
          case KEINE:
            selection = Eigenschaftenauswahl.AKTIVE_MITGLIEDER;
            break;
          case INAKTIVE_MITGLIEDER:
            selection = Eigenschaftenauswahl.MITGLIEDER;
            break;
          case NICHT_MITGLIEDER:
            selection = Eigenschaftenauswahl.AKTIVE_MITGLIEDER_NICHT_MITGLIEDER;
            break;
          case INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
            selection = Eigenschaftenauswahl.ALLE;
            break;
          case ALLE:
          case MITGLIEDER:
          case AKTIVE_MITGLIEDER:
          case AKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
            break;
        }
        setSelection();
      }
    });

    b.addButton("Inaktive Mitglieder", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        Eigenschaftenauswahl selection_old = selection;
        switch (selection_old)
        {
          case KEINE:
            selection = Eigenschaftenauswahl.INAKTIVE_MITGLIEDER;
            break;
          case AKTIVE_MITGLIEDER:
            selection = Eigenschaftenauswahl.MITGLIEDER;
            break;
          case NICHT_MITGLIEDER:
            selection = Eigenschaftenauswahl.INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER;
            break;
          case AKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
            selection = Eigenschaftenauswahl.ALLE;
            break;
          case ALLE:
          case MITGLIEDER:
          case INAKTIVE_MITGLIEDER:
          case INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
            break;
        }
        setSelection();
      }
    });

    b.addButton("Nicht-Mitglieder", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        Eigenschaftenauswahl selection_old = selection;
        switch (selection_old)
        {
          case KEINE:
            selection = Eigenschaftenauswahl.NICHT_MITGLIEDER;
            break;
          case AKTIVE_MITGLIEDER:
            selection = Eigenschaftenauswahl.AKTIVE_MITGLIEDER_NICHT_MITGLIEDER;
            break;
          case INAKTIVE_MITGLIEDER:
            selection = Eigenschaftenauswahl.INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER;
            break;
          case MITGLIEDER:
            selection = Eigenschaftenauswahl.ALLE;
            break;
          case ALLE:
          case NICHT_MITGLIEDER:
          case AKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
          case INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
            break;
        }
        setSelection();
      }
    });

    b.paint(parent);

    ButtonArea c = new ButtonArea();
    c.addButton("Übernehmen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          for (Object o : control.getMitgliedMitMail().getItems())
          {
            Mitglied m = (Mitglied) o;
            MailEmpfaenger me = (MailEmpfaenger) Einstellungen.getDBService()
                .createObject(MailEmpfaenger.class, null);
            me.setMitglied(m);
            control.addEmpfaenger(me);
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        close();
      }
    }, null, true, "ok.png");

    c.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");

    c.paint(parent);
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

  private void setSelection()
  {
    try
    {
      auswahlinput.setValue(selection.getText());
      eigenschafteninput.setValue(getEigenschaftenString());
      verknuepfunginput.setValue(param != null ? param.getVerknuepfung() : "");

      if (selection == Eigenschaftenauswahl.KEINE)
      {
        return;
      }

      ArrayList<EigenschaftenNode> eigenschaftenNodes = null;
      if (param != null && param.getEigenschaftenNodes().size() > 0)
      {
        eigenschaftenNodes = param.getEigenschaftenNodes();
      }

      ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
          "mitglied");
      it.addColumn("mitglied.id as " + MITGLIED_ID);
      if (eigenschaftenNodes != null)
      {
        it.addColumn("eigenschaft as " + EIGENSCHAFT);
        it.join("eigenschaften", "mitglied.id = eigenschaften.mitglied");
      }
      // Filtern nach Mitgliedstyp
      switch (selection)
      {
        case AKTIVE_MITGLIEDER:
        case INAKTIVE_MITGLIEDER:
        case MITGLIEDER:
          it.addFilter("adresstyp = ?", Long.valueOf(Mitgliedstyp.MITGLIED));
          break;
        case NICHT_MITGLIEDER:
          it.addFilter("adresstyp != ?", Long.valueOf(Mitgliedstyp.MITGLIED));
          break;
        case AKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
        case INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
        case KEINE:
        case ALLE:
          break;
      }
      // Filtern nach Aktiv
      Date d = new Date();
      switch (selection)
      {
        case AKTIVE_MITGLIEDER:
          it.addFilter("(eintritt IS NULL OR eintritt <= ?)"
              + " AND (austritt IS NULL OR austritt > ?)", d, d);
          break;
        case AKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
          it.addFilter(
              "adresstyp != ? OR ((eintritt IS NULL OR eintritt <= ?)"
                  + " AND (austritt IS NULL OR austritt > ?))",
              Long.valueOf(Mitgliedstyp.MITGLIED), d, d);
          break;
        case INAKTIVE_MITGLIEDER:
          it.addFilter("NOT ((eintritt IS NULL OR eintritt <= ?)"
              + " AND (austritt IS NULL OR austritt > ?))", d, d);
          break;
        case INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER:
          it.addFilter(
              "adresstyp != ? OR NOT ((eintritt IS NULL OR eintritt <= ?)"
                  + " AND (austritt IS NULL OR austritt > ?))",
              Long.valueOf(Mitgliedstyp.MITGLIED), d, d);
          break;
        case MITGLIEDER:
        case NICHT_MITGLIEDER:
        case KEINE:
        case ALLE:
          break;
      }
      it.setOrder("Order by mitglied.id");

      ArrayList<Long> selectedIds = new ArrayList<>();
      HashMap<Long, Integer> map = new HashMap<>();

      while (it.hasNext())
      {
        PseudoDBObject o = it.next();
        Long mitglied_id = (Long) o.getAttribute(MITGLIED_ID);
        if (eigenschaftenNodes != null)
        {
          Long eigenschaft = (Long) o.getAttribute(EIGENSCHAFT);
          if (param.getVerknuepfung().equals(EigenschaftenAuswahlDialog.ODER))
          {
            for (EigenschaftenNode node : eigenschaftenNodes)
            {
              if (Long.valueOf(node.getEigenschaft().getID()) == eigenschaft)
              {
                selectedIds.add(mitglied_id);
              }
            }
          }
          else if (param.getVerknuepfung()
              .equals(EigenschaftenAuswahlDialog.UND))
          {
            for (EigenschaftenNode node : eigenschaftenNodes)
            {
              if (Long.valueOf(node.getEigenschaft().getID()) == eigenschaft)
              {
                Integer count = map.get(mitglied_id) != null
                    ? map.get(mitglied_id) + 1
                    : 1;
                map.put(mitglied_id, count);
              }
            }
          }
        }
        else
        {
          selectedIds.add(mitglied_id);
        }
      }

      if (eigenschaftenNodes != null
          && param.getVerknuepfung().equals(EigenschaftenAuswahlDialog.UND))
      {
        int anzahl = eigenschaftenNodes.size();
        for (Map.Entry<Long, Integer> entry : map.entrySet())
        {
          // Wenn alle Eigenschaften enthalten waren, dann Mitglied selektieren
          if (entry.getValue() == anzahl)
          {
            selectedIds.add(entry.getKey());
          }
        }
      }

      for (Object o : control.getMitgliedMitMail().getItems(false))
      {
        Mitglied m = (Mitglied) o;
        if (selectedIds.contains(Long.valueOf(m.getID())))
        {
          control.getMitgliedMitMail().setChecked(o, true);
        }
        else
        {
          control.getMitgliedMitMail().setChecked(o, false);
        }
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  private TextInput getAuswahl()
  {
    if (auswahlinput != null)
    {
      return auswahlinput;
    }
    auswahlinput = new TextInput(selection.getText());
    auswahlinput.disable();
    return auswahlinput;
  }

  private TextInput getEigenschaften()
  {
    if (eigenschafteninput != null)
    {
      return eigenschafteninput;
    }
    eigenschafteninput = new TextInput(getEigenschaftenString());
    eigenschafteninput.disable();
    return eigenschafteninput;
  }

  private TextInput getVerknuepfung()
  {
    if (verknuepfunginput != null)
    {
      return verknuepfunginput;
    }
    verknuepfunginput = new TextInput(
        param != null ? param.getVerknuepfung() : "");
    verknuepfunginput.disable();
    return verknuepfunginput;
  }

  private String getEigenschaftenString()
  {
    if (param != null && param.getEigenschaftenNodes().size() > 0)
    {
      StringBuilder text = new StringBuilder();
      for (Object o : param.getEigenschaftenNodes())
      {
        if (text.length() > 0)
        {
          text.append(", ");
        }
        EigenschaftenNode node = (EigenschaftenNode) o;
        try
        {
          String prefix = "+";
          if (node.getPreset().equals(EigenschaftenNode.MINUS))
            prefix = "-";
          text.append(prefix + node.getEigenschaft().getBezeichnung());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return text.toString();
    }
    return "";
  }
}
