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
import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, zur Auswahl von Eigenschaften eines Mitglied.
 */
public class EigenschaftenAuswahlDialog
    extends AbstractDialog<EigenschaftenAuswahlParameter>
{

  private FilterControl control;

  private SelectInput eigenschaftenverknuepfung;

  private String defaults = null;

  private boolean verknuepfung;

  private boolean onlyChecked;

  private Mitglied[] mitglieder;

  private EigenschaftenAuswahlParameter param;

  /**
   * Eigenschaften oder Eigenschaftengruppen auswählen
   * 
   * @param defaults
   *          Liste der Eigenschaften-IDs durch Komma separiert.
   * @param ohnePflicht
   *          Spezifiziert ob Eigenschaftengruppen mit Pflicht und Max1
   *          ignoriert werden. true: ignorieren
   * @param verknuepfung
   *          Spezifiziert ob der Input Verknüpfung (UND,ODER) im Dialog
   *          angezeigt werden soll.
   * @param control
   *          Control welches den EigenschaftenAuswahlTree liefert.
   * @param onlyChecked
   *          Gibt an ob nur die Checkbox Werte UNCHECKED und CHECKED angezeigt
   *          werden.
   * @param mitglieder
   *          Liste der Mitglieder welche selektiert wurden.
   */
  public EigenschaftenAuswahlDialog(String defaults, boolean verknuepfung,
      FilterControl control, boolean onlyChecked)
  {
    this(defaults, verknuepfung, control, onlyChecked, null);
  }

  public EigenschaftenAuswahlDialog(String defaults, boolean verknuepfung,
      FilterControl control, boolean onlyChecked, Mitglied[] mitglieder)
  {
    super(EigenschaftenAuswahlDialog.POSITION_CENTER);
    this.setSize(400, 400);
    this.verknuepfung = verknuepfung;
    setTitle("Eigenschaften auswählen ");
    this.control = control;
    this.setDefaults(defaults);
    this.onlyChecked = onlyChecked;
    this.mitglieder = mitglieder;
  }

  /**
   * Speichert die Default-Werte.
   * 
   * @param defaults
   *          Liste der Eigenschaften-IDs durch Komma separiert.
   */
  public void setDefaults(String defaults)
  {
    this.defaults = defaults != null ? defaults : "";
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    final TreePart tree = control.getEigenschaftenAuswahlTree(this.defaults,
        onlyChecked, mitglieder);

    LabelGroup group = new LabelGroup(parent, "Eigenschaften", true);
    group.addPart(tree);
    if (verknuepfung)
    {
      group.addInput(getEigenschaftenVerknuepfung());
    }
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("OK", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          param = new EigenschaftenAuswahlParameter();
          ArrayList<?> rootNodes = (ArrayList<?>) tree.getItems(); // liefert
                                                                   // nur den
                                                                   // Root
          EigenschaftenNode root = (EigenschaftenNode) rootNodes.get(0);
          if (mitglieder != null)
          {
            checkRestrictions(root, mitglieder);
          }
          for (EigenschaftenNode checkedNode : root.getCheckedNodes())
          {
            param.add(checkedNode);
          }
          if (verknuepfung)
          {
            param
                .setVerknuepfung((String) eigenschaftenverknuepfung.getValue());
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        close();
      }
    }, null, true, "ok.png");
    buttons.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");
    buttons.paint(parent);
  }

  @Override
  protected EigenschaftenAuswahlParameter getData()
  {
    return param;
  }

  private SelectInput getEigenschaftenVerknuepfung()
  {
    if (eigenschaftenverknuepfung != null
        && !eigenschaftenverknuepfung.getControl().isDisposed())
    {
      return eigenschaftenverknuepfung;
    }
    ArrayList<String> werte = new ArrayList<>();
    werte.add("und");
    werte.add("oder");
    eigenschaftenverknuepfung = new SelectInput(werte,
        control.getEigenschaftenVerknuepfung());
    eigenschaftenverknuepfung.setName("Gruppen-Verknüpfung");
    return eigenschaftenverknuepfung;
  }

  private boolean checkRestrictions(EigenschaftenNode root,
      Mitglied[] mitglieder) throws RemoteException, ApplicationException
  {
    HashMap<String, Boolean> pflichtgruppenMap = new HashMap<>();
    HashMap<String, Boolean> max1gruppenMap = new HashMap<>();
    ArrayList<EigenschaftGruppe> pflichtgruppen = root.getPflichtGruppen();
    ArrayList<EigenschaftGruppe> max1gruppen = root.getMax1Gruppen();
    if (!pflichtgruppen.isEmpty())
    {
      for (Mitglied mitglied : mitglieder)
      {
        if (mitglied.getMitgliedstyp().getJVereinid() != Mitgliedstyp.MITGLIED
            && !(Boolean) Einstellungen
                .getEinstellung(Property.NICHTMITGLIEDPFLICHTEIGENSCHAFTEN))
        {
          continue;
        }
        if (mitglied.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED
            && mitglied.getPersonenart().equalsIgnoreCase("j")
            && !(Boolean) Einstellungen
                .getEinstellung(Property.JURISTISCHEPERSONPFLICHTEIGENSCHAFTEN))
        {
          continue;
        }
        // 1. Prüfen auf Pflicht
        // Erst alle Pflicht Gruppen auf false setzten
        pflichtgruppenMap.clear();
        for (EigenschaftGruppe eg : pflichtgruppen)
        {
          pflichtgruppenMap.put(eg.getID(), Boolean.valueOf(false));
        }
        // Gesetzte Eigenschaften Gruppen bestimmen
        // Es muss die Eigenschaft im Mitglied gesetzt sein
        // und darf nicht im Dialog auf "-" stehen
        for (Long[] eigenschaften : root.getEigenschaften())
        {
          EigenschaftenNode node = root
              .getEigenschaftenNode(eigenschaften[1].toString());
          String gruppenId = node.getEigenschaftGruppe().getID();
          if (eigenschaften[0].toString().equals(mitglied.getID())
              && !node.getPreset().equals(EigenschaftenNode.MINUS))
          {
            pflichtgruppenMap.put(gruppenId, Boolean.valueOf(true));
          }
        }
        // Check ob ein Wert neu mit "+" gesetzt wird
        for (EigenschaftenNode node : root.getCheckedNodes())
        {
          String gruppenId = node.getEigenschaftGruppe().getID().toString();
          if (node.getPreset().equals(EigenschaftenNode.PLUS))
          {
            pflichtgruppenMap.put(gruppenId, Boolean.valueOf(true));
          }
        }
        for (String key : pflichtgruppenMap.keySet())
        {
          if (!pflichtgruppenMap.get(key))
          {
            EigenschaftGruppe eg = root.getEigenschaftGruppe(key);
            throw new ApplicationException(String.format(
                "In der Eigenschaftengruppe \"%s\" fehlt ein Eintrag bei Mitglied %s!",
                eg.getBezeichnung(), mitglied.getAttribute("namevorname")));
          }
        }
      }
    }

    if (!max1gruppen.isEmpty())
    {
      for (Mitglied mitglied : mitglieder)
      {
        // 2. Prüfen auf Max1
        // Max eine Eigenschaft pro Gruppe
        max1gruppenMap.clear();
        for (EigenschaftGruppe eg : max1gruppen)
        {
          max1gruppenMap.put(eg.getID(), Boolean.valueOf(false));
        }
        // Gesetzte Eigenschaften Gruppen bestimmen
        // Es darf höchstens eine Eigenschaft im Mitglied gesetzt sein
        // Hier nur gesetzte Werte ohne "+" und "-", "+" kommt nachher
        for (Long[] eigenschaften : root.getEigenschaften())
        {
          EigenschaftenNode node = root
              .getEigenschaftenNode(eigenschaften[1].toString());
          if (eigenschaften[0].toString().equals(mitglied.getID())
              && !node.getPreset().equals(EigenschaftenNode.MINUS)
              && !node.getPreset().equals(EigenschaftenNode.PLUS))
          {
            EigenschaftGruppe gruppe = node.getEigenschaftGruppe();
            Boolean m1 = max1gruppenMap.get(gruppe.getID());
            if (m1 != null)
            {
              if (m1)
              {
                throw new ApplicationException(String.format(
                    "In der Eigenschaftengruppe \"%s\" ist bei Mitglied %s mehr als ein Eintrag markiert!",
                    gruppe.getBezeichnung(),
                    mitglied.getAttribute("namevorname")));
              }
              else
              {
                max1gruppenMap.put(gruppe.getID(), Boolean.valueOf(true));
              }
            }
          }
        }
        // Check ob ein Wert neu mit "+" gesetzt wird
        for (EigenschaftenNode node : root.getCheckedNodes())
        {
          if (node.getPreset().equals(EigenschaftenNode.PLUS))
          {
            EigenschaftGruppe gruppe = node.getEigenschaftGruppe();
            Boolean m1 = max1gruppenMap.get(gruppe.getID());
            if (m1 != null)
            {
              if (m1)
              {
                throw new ApplicationException(String.format(
                    "In der Eigenschaftengruppe '%s' ist bei Mitglied %s mehr als ein Eintrag markiert!",
                    gruppe.getBezeichnung(),
                    mitglied.getAttribute("namevorname")));
              }
              else
              {
                max1gruppenMap.put(gruppe.getID(), Boolean.valueOf(true));
              }
            }
          }
        }
      }
    }
    return true;
  }

}
