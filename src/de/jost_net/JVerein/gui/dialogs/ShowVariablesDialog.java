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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.willuhn.jameica.gui.GUI;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;

/**
 * Dialog, zum Anzeigen von Variablen-Namen und deren Inhalten.
 */
public class ShowVariablesDialog extends AbstractDialog<Object>
{

  private Map<String, Object> vars;

  private final Clipboard clipboard;

  private final String prependCopyText, appendCopyText;

  TablePart tab;

  public ShowVariablesDialog(Map<String, Object> vars)
  {
    this(vars, "", "");
  }

  public ShowVariablesDialog(Map<String, Object> vars, String prependCopyText,
      String appendCopyText)
  {
    super(AbstractDialog.POSITION_CENTER);
    setTitle("Liste der Variablen");
    setSize(600, 400);
    this.clipboard = new Clipboard(GUI.getDisplay());
    this.vars = vars;
    this.prependCopyText = prependCopyText;
    this.appendCopyText = appendCopyText;

    try
    {
      this.open();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
  }

  @Override
  protected void onEscape()
  {
    close();
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    List<GenericObject> list = new ArrayList<>();

    for (Entry<String, Object> entry : vars.entrySet())
    {
      list.add(new Var(entry));
    }
    tab = new TablePart(list, getCopyAction());
    tab.addColumn("Name", "name");
    tab.addColumn("Wert", "wert");
    tab.setRememberOrder(true);
    tab.paint(parent);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("In Zwischenablage kopieren", getCopyAction(), null, true,
        "edit-copy.png");
    buttons.paint(parent);
  }

  private Action getCopyAction()
  {
    return new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (tab.getSelection() instanceof Var)
        {
          Var v = (Var) tab.getSelection();
          String textData = (String) v.getAttribute("name");
          if (textData.length() > 0)
          {
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(
                new Object[] { prependCopyText + textData + appendCopyText },
                new Transfer[] { textTransfer });
          }
        }
        close();
      }
    };
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Object getData() throws Exception
  {
    return null;
  }

  public class Var implements GenericObject
  {

    private String name;

    private Object wert;

    public Var(Entry<String, Object> entry)
    {
      this.name = entry.getKey();
      this.wert = entry.getValue();
    }

    @Override
    public String[] getAttributeNames()
    {
      return new String[] { "name", "wert" };
    }

    @Override
    public String getID()
    {
      return "name";
    }

    @Override
    public boolean equals(GenericObject arg0)
    {
      return false;
    }

    @Override
    public Object getAttribute(String arg0)
    {
      if (arg0.equals("name"))
      {
        return name;
      }
      else if (arg0.equals("wert"))
      {
        return wert;
      }
      return null;
    }

    @Override
    public String getPrimaryAttribute()
    {
      return "name";
    }

  }
}
