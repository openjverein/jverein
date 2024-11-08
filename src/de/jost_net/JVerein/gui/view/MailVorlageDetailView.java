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

import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MailVorlageControl;
import de.jost_net.JVerein.gui.dialogs.ShowVariablesDialog;
import de.jost_net.JVerein.gui.menu.ShowVariablesMenu;
import de.jost_net.JVerein.server.MitgliedImpl;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.jost_net.JVerein.util.MitgliedDummy;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class MailVorlageDetailView extends AbstractView implements Listener
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mail-Vorlage");

    final MailVorlageControl control = new MailVorlageControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Mail-Vorlage");
    group.addInput(control.getBetreff(true));
    SimpleContainer t = new SimpleContainer(getParent(), true);
    t.addPart(control.getTxt());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MAILVORLAGE, false, "question-circle.png");
    buttons.addButton("Variablen anzeigen (F6)",
            new OpenInsertVariableDialogAction(), null, false, "bookmark.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }

  @Override
  public void handleEvent(Event event)
  {
     if (event.keyCode == org.eclipse.swt.SWT.F6)
    {
      new OpenInsertVariableDialogAction().handleAction(null);
    }
  }

  private static final class OpenInsertVariableDialogAction implements Action
  {

    @Override
    public void handleAction(Object context)
    {
      try
      {
        LesefeldAuswerter lesefeldAuswerter = new LesefeldAuswerter();
        lesefeldAuswerter.setLesefelderDefinitionsFromDatabase();
        lesefeldAuswerter.setMap(new MitgliedMap().getMap(new MitgliedDummy(), null));
        lesefeldAuswerter.evalAlleLesefelder();
        ShowVariablesDialog d = new ShowVariablesDialog(
               lesefeldAuswerter.getMap(), false);
        ShowVariablesMenu menu = new ShowVariablesMenu();
        menu.setPrependCopyText("");
        menu.setAppendCopyText("");
        d.setContextMenu(menu);
        d.setDoubleClickAction(menu.getCopyToClipboardAction());
        d.open();
      }
      catch (OperationCanceledException ignored)
      {

      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Anzeigen der Variablen.", e);
        GUI.getStatusBar().setErrorText("Fehler beim Anzeigen der Variablen.");
      }

    }

  }

}
