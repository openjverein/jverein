/**********************************************************************
 * basiert auf dem KontoAuswahlDialog aus Hibiscus
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.control.SollbuchungControl.DIFFERENZ;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, ueber den man eine Sollbuchung auswaehlen kann.
 */
public class SollbuchungAuswahlDialog extends AbstractDialog<Object>
{

  private de.willuhn.jameica.system.Settings settings;

  private Object choosen = null;

  private SollbuchungControl control;

  private TablePart sollbuchunglist = null;

  private TablePart mitgliedlist = null;

  private Buchung buchung;

  private boolean abort = true;

  private MyButton suchen1;

  private MyButton suchen2;

  private boolean multi;

  public SollbuchungAuswahlDialog(Buchung buchung, boolean multi)
  {
    super(SollbuchungAuswahlDialog.POSITION_MOUSE, true);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);

    this.setSize(900, 700);
    this.setTitle("Sollbuchung Auswahl");
    this.buchung = buchung;
    this.multi = multi;
    control = new SollbuchungControl(null);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.addSelectionListener(new SelectionAdapter()
    {

      @Override
      public void widgetSelected(SelectionEvent evt)
      {
        TabItem item = folder.getSelection()[0];
        if (item.getText().startsWith("Ist"))
        {
          suchen1.setDefaultButton();
        }
        else if (item.getText().startsWith("Soll"))
        {
          suchen2.setDefaultButton();
        }
      }
    });

    TabGroup tabNurIst = new TabGroup(folder,
        "Istbuchung einer Sollbuchung zuordnen", false, 1);
    LabelGroup grNurIst = new LabelGroup(tabNurIst.getComposite(), "Filter");

    control.getSuchName1(true).setValue(buchung.getName());
    grNurIst.addLabelPair("Name", control.getSuchName1(false));
    grNurIst.addLabelPair("Differenz",
        control.getDifferenz(DIFFERENZ.FEHLBETRAG));

    ButtonArea button1 = new ButtonArea();
    suchen1 = new MyButton("Suchen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.refreshSollbuchungenList();
      }
    }, null, false, "search.png");
    button1.addButton(suchen1);
    grNurIst.addButtonArea(button1);

    Action action = new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (context == null || !(context instanceof Sollbuchung))
        {
          return;
        }
        choosen = context;
        abort = false;
        close();
      }
    };
    sollbuchunglist = control.getSollbuchungenList(action, true, multi);
    sollbuchunglist.paint(tabNurIst.getComposite());

    TabGroup tabSollIst = new TabGroup(folder,
        "Sollbuchung erzeugen und Istbuchung zuordnen", true, 1);
    LabelGroup grSollIst = new LabelGroup(tabSollIst.getComposite(), "Filter");

    control.getSuchName2(true).setValue(buchung.getName());
    grSollIst.addLabelPair("Name", control.getSuchName2(false));
    grSollIst.addInput(control.getSpezialSuche());

    ButtonArea button2 = new ButtonArea();
    suchen2 = new MyButton("Suchen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.refreshMitgliederList();
      }
    }, null, false, "search.png");

    button2.addButton(suchen2);
    grSollIst.addButtonArea(button2);

    final Action action2 = new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (context == null || !(context instanceof Mitglied))
        {
          return;
        }
        choosen = context;
        abort = false;
        close();
      }
    };
    mitgliedlist = control.getMitgliederList(action2, null);
    mitgliedlist.paint(tabSollIst.getComposite());

    ButtonArea b = new ButtonArea();

    b.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_AUSWAHL, false, "question-circle.png");

    b.addButton("Übernehmen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        Object o = sollbuchunglist.getSelection();

        if (o instanceof Sollbuchung || o instanceof Sollbuchung[])
        {
          choosen = o;
          abort = false;
          close();
        }
        else
        {
          o = mitgliedlist.getSelection();

          if (o instanceof Mitglied)
          {
            try
            {
              if (buchung.isNewObject())
              {
                GUI.getStatusBar()
                    .setErrorText("Neue Buchung bitte erst speichern.");
                throw new OperationCanceledException();
              }
            }
            catch (RemoteException e)
            {
              String error = "Fehler bei Auswahl der Sollbuchung";
              Logger.error(error, e);
              GUI.getStatusBar().setErrorText(error);
              throw new OperationCanceledException();
            }
            choosen = o;
            abort = false;
            close();
          }
        }
        return;
      }
    }, null, false, "ok.png");

    b.addButton("Entfernen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        choosen = null;
        abort = false;
        close();
      }
    }, null, false, "user-trash-full.png");

    b.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");

    b.paint(parent);
  }

  /**
   * Liefert das ausgewaehlte Sollbuchung zurueck oder <code>null</code> wenn
   * der Abbrechen-Knopf gedrueckt wurde.
   * 
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return choosen;
  }

  public boolean getAbort()
  {
    return abort;
  }

  public class MyButton extends Button
  {
    public MyButton(String title, Action action, Object context,
        boolean defaultButton, String icon)
    {
      super(title, action, context, defaultButton, icon);
    }

    public void setDefaultButton()
    {
      try
      {
        getShell().setDefaultButton(button);
      }
      catch (IllegalArgumentException ae)
      {
        // Kann unter MacOS wohl passieren. Siehe Mail von
        // Jan Lolling vom 22.09.2006. Mal schauen, ob wir
        // Fehlertext: "Widget has the wrong parent"
        // Wir versuchen es mal mit der Shell der GUI.
        try
        {
          GUI.getShell().setDefaultButton(button);
        }
        catch (IllegalArgumentException ae2)
        {
          // Geht auch nicht? Na gut, dann lassen wir es halt bleiben
          Logger.warn(
              "unable to set default button: " + ae2.getLocalizedMessage());
        }
      }
    }
  }

}
