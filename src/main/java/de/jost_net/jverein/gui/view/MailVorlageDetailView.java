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
package de.jost_net.jverein.gui.view;

import de.jost_net.jverein.gui.action.DokumentationAction;
import de.jost_net.jverein.gui.action.InsertVariableDialogAction;
import de.jost_net.jverein.gui.action.MailTextVorschauAction;
import de.jost_net.jverein.gui.control.MailVorlageControl;
import de.jost_net.jverein.gui.control.Savable;
import de.jost_net.jverein.gui.parts.ButtonAreaRtoL;
import de.jost_net.jverein.gui.parts.ButtonRtoL;
import de.jost_net.jverein.gui.parts.SaveButton;
import de.jost_net.jverein.gui.parts.SaveNeuButton;
import de.jost_net.jverein.gui.util.JameicaUtil;
import de.jost_net.jverein.variable.AllgemeineMap;
import de.jost_net.jverein.variable.MitgliedMap;
import de.willuhn.jameica.gui.GUI;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MailVorlageDetailView extends AbstractDetailView
{
  private MailVorlageControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mail-Vorlage");

    control = new MailVorlageControl(this);

    Composite comp = new Composite(this.getParent(), SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    comp.setLayout(layout);
    JameicaUtil.addLabel("Betreff", comp, GridData.VERTICAL_ALIGN_CENTER);
    control.getBetreff(true).paint(comp);
    JameicaUtil.addLabel("Text", comp, GridData.VERTICAL_ALIGN_BEGINNING);
    control.getTxt().paint(comp);

    Map<String, Object> map = MitgliedMap
        .getDummyMap(new AllgemeineMap().getMap(null));

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MAILVORLAGE, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton("Variablen anzeigen", new InsertVariableDialogAction(map),
        control, false, "bookmark.png");
    buttons.addButton(
        new ButtonRtoL("Vorschau", new MailTextVorschauAction(map, true),
            control, false, "edit-copy.png"));
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
