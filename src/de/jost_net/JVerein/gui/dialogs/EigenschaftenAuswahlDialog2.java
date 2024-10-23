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

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.EigenschaftenNode2;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;

/**
 * Dialog, zur Auswahl von Eigenschaften eines Mitglied.
 */
public class EigenschaftenAuswahlDialog2
    extends AbstractDialog<EigenschaftenAuswahlParameter2>
{

  private FilterControl control;

  private SelectInput eigenschaftenverknuepfung;

  private String defaults = null;

  private boolean ohnePflicht;

  private boolean verknuepfung;
  
  private boolean onlyChecked;
  
  private Mitglied[] mitglieder;

  private EigenschaftenAuswahlParameter2 param;

  /**
   * Eigenschaften oder Eigenschaftengruppen auswählen
   * 
   * @param defaults
   *          Liste der Eigenschaften-IDs durch Komma separiert.
   * @param ohnePflicht
   *          Spezifiziert ob Eigenschaftengruppen mit Pflicht und Max1 
   *           ignoriert werden.  true: ignorieren
   * @param verknuepfung
   *          Spezifiziert ob der Input Verknüpfung (UND,ODER) im Dialog
   *          angezeigt werden soll.
   * @param control
   *          Control welches den EigenschaftenAuswahlTree liefert.
   * @param onlyChecked
   *          Gibt an ob nur die Checkbox Werte UNCHECKED und CHECKED 
   *          angezeigt werden.
   * @param mitglieder
   *          Liste der Mitglieder welche selektiert wurden.
   */
  public EigenschaftenAuswahlDialog2(String defaults, boolean ohnePflicht,
      boolean verknuepfung, FilterControl control, boolean onlyChecked)
  {
    this(defaults, ohnePflicht, verknuepfung, control, onlyChecked, null);
  }
  
  public EigenschaftenAuswahlDialog2(String defaults, boolean ohnePflicht,
      boolean verknuepfung, FilterControl control, boolean onlyChecked, Mitglied[] mitglieder)
  {
    super(EigenschaftenAuswahlDialog2.POSITION_CENTER);
    this.setSize(400, 400);
    this.ohnePflicht = ohnePflicht;
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
    final TreePart tree = control.getEigenschaftenAuswahlTree2(this.defaults,
        ohnePflicht, onlyChecked, mitglieder);

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
      public void handleAction(Object context)
      {
        try
        {
          param = new EigenschaftenAuswahlParameter2();
          ArrayList<?> rootnodes = (ArrayList<?>) tree.getItems();  // liefert nur den Root
          EigenschaftenNode2 root = (EigenschaftenNode2) rootnodes.get(0);
          for (EigenschaftenNode2 checkednode : root.getCheckedNodes())
          {
            param.add(checkednode);
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
    buttons.paint(parent);
  }

  @Override
  protected EigenschaftenAuswahlParameter2 getData()
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

}
