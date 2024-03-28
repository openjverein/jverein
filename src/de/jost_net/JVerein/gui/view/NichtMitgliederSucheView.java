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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;
import de.jost_net.JVerein.gui.control.MitgliedControl.Mitgliedstyp;

public class NichtMitgliederSucheView extends AbstractMitgliedSucheView
{
  public NichtMitgliederSucheView() throws RemoteException
  {
    control.getSuchAdresstyp(Mitgliedstyp.NICHTMITGLIED).getValue();
  }

  @Override
  public String getTitle()
  {
    return "Nicht-Mitglieder suchen";
  }

  @Override
  public void getFilter() throws RemoteException
  {
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    TextInput suchName = control.getSuchname();
    left.addInput(suchName);
    Input adrtyp = control.getSuchAdresstyp(Mitgliedstyp.NICHTMITGLIED);
    adrtyp.addListener(new FilterListener());
    left.addLabelPair("Mitgliedstyp", adrtyp);
    DialogInput mitgleigenschaften = control.getEigenschaftenAuswahl();
    left.addLabelPair("Eigenschaften", mitgleigenschaften);
    if (Einstellungen.getEinstellung().hasZusatzfelder())
    {
      DialogInput mitglzusatzfelder = control.getZusatzfelderAuswahl();
      mitglzusatzfelder.addListener(new FilterListener());
      left.addLabelPair("Zusatzfelder", mitglzusatzfelder);
    }
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    DateInput mitglgebdatvon = control.getGeburtsdatumvon();
    right.addLabelPair("Geburtsdatum von", mitglgebdatvon);
    DateInput mitglgebdatbis = control.getGeburtsdatumbis();
    right.addLabelPair("Geburtsdatum bis", mitglgebdatbis);
    SelectInput mitglgeschlecht = control.getGeschlecht();
    mitglgeschlecht.setMandatory(false);
    mitglgeschlecht.addListener(new FilterListener());
    right.addLabelPair("Geschlecht", mitglgeschlecht);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button("Filter-Reset", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          control.resetEigenschaftenAuswahl();
          control.getSuchname().setValue("");
          control.getSuchAdresstyp(Mitgliedstyp.NICHTMITGLIED).setValue(null);
          control.getGeburtsdatumvon().setValue(null);
          control.getGeburtsdatumbis().setValue(null);
          control.getGeschlecht().setValue(null);
          if (Einstellungen.getEinstellung().hasZusatzfelder())
          {
            control.resetZusatzfelderAuswahl();
          }
          TabRefresh();
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(e);
        }

      }
    }, null, false, "eraser.png"));
    Button suchen = new Button("Suchen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        TabRefresh();
      }
    }, null, true, "search.png");
    buttons.addButton(suchen);
    group.addButtonArea(buttons);
  }

  @Override
  public Action getDetailAction()
  {
    return new NichtMitgliedDetailAction();
  }

  @Override
  public Button getHilfeButton()
  {
    return new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.ADRESSEN, false, "question-circle.png");
  }
}