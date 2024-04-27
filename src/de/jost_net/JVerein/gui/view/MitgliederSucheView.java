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
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
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
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.jost_net.JVerein.gui.control.MitgliedControl.Mitgliedstyp;

public class MitgliederSucheView extends AbstractMitgliedSucheView
{

  public MitgliederSucheView() throws RemoteException
  {
    control.getSuchAdresstyp(Mitgliedstyp.MITGLIED).getValue();
  }

  @Override
  public String getTitle()
  {
    return "Mitglieder suchen";
  }

  @Override
  public void getFilter() throws RemoteException
  {
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    Input mitglstat = control.getMitgliedStatus();
    mitglstat.addListener(new FilterListener());
    left.addLabelPair("Mitgliedschaft", mitglstat);
    if (Einstellungen.getEinstellung().getExterneMitgliedsnummer())
    {
      left.addLabelPair("Externe Mitgliedsnummer",
          control.getSuchExterneMitgliedsnummer());
    }
    DialogInput mitgleigenschaften = control.getEigenschaftenAuswahl();
    left.addLabelPair("Eigenschaften", mitgleigenschaften);
    SelectInput mitglbeitragsgruppe = control.getBeitragsgruppeAusw();
    mitglbeitragsgruppe.addListener(new FilterListener());
    left.addLabelPair("Beitragsgruppe", mitglbeitragsgruppe);

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    TextInput suchName = control.getSuchname();
    middle.addInput(suchName);
    DateInput mitglgebdatvon = control.getGeburtsdatumvon();
    middle.addLabelPair("Geburtsdatum von", mitglgebdatvon);
    DateInput mitglgebdatbis = control.getGeburtsdatumbis();
    middle.addLabelPair("Geburtsdatum bis", mitglgebdatbis);
    SelectInput mitglgeschlecht = control.getSuchGeschlecht();
    mitglgeschlecht.addListener(new FilterListener());
    middle.addLabelPair("Geschlecht", mitglgeschlecht);
    DateInput stichtag = control.getStichtag();
    middle.addLabelPair("Stichtag", stichtag);

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    DateInput mitgleintrittvon = control.getEintrittvon();
    right.addLabelPair("Eintrittsdatum von", mitgleintrittvon);
    DateInput mitgleintrittbis = control.getEintrittbis();
    right.addLabelPair("Eintrittsdatum bis", mitgleintrittbis);
    DateInput mitglaustrittvon = control.getAustrittvon();
    right.addLabelPair("Austrittsdatum von", mitglaustrittvon);
    DateInput mitglaustrittbis = control.getAustrittbis();
    right.addLabelPair("Austrittsdatum bis", mitglaustrittbis);

    if (Einstellungen.getEinstellung().hasZusatzfelder())
    {
      DialogInput mitglzusatzfelder = control.getZusatzfelderAuswahl();
      left.addLabelPair("Zusatzfelder", mitglzusatzfelder);
    }
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getProfileButton());
    buttons.addButton(new Button("Filter-Reset", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          Settings s = control.getSettings();
          s.setAttribute("id", "");
          s.setAttribute("profilname", "");

          control.getMitgliedStatus().setValue("Angemeldet");
          control.getSuchExterneMitgliedsnummer().setValue("");
          control.resetEigenschaftenAuswahl();
          control.getBeitragsgruppeAusw().setValue(null);
          control.getSuchname().setValue("");
          control.getGeburtsdatumvon().setValue(null);
          control.getGeburtsdatumbis().setValue(null);
          control.getSuchGeschlecht().setValue(null);
          control.getEintrittvon().setValue(null);
          control.getEintrittbis().setValue(null);
          control.getAustrittvon().setValue(null);
          control.getAustrittbis().setValue(null);
          control.getStichtag().setValue(null);
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
    return new MitgliedDetailAction();
  }

  @Override
  public Button getHilfeButton()
  {
    return new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIED, false, "question-circle.png");
  }

}