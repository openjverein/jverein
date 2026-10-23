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
package de.jost_net.JVerein.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.server.DBSupportH2Impl;
import de.jost_net.JVerein.server.MigratH2Database;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Settings;

public class H2migration extends AbstractBox
{
  @Override
  public boolean isActive()
  {
    return isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  @Override
  public boolean isEnabled()
  {
    // Prüfen, ob jameica die Migration bereits anbietet
    try
    {
      Class.forName("de.willuhn.jameica.system.MigrateDB");
    }
    catch (ClassNotFoundException e)
    {
      return false;
    }

    String h2 = DBSupportH2Impl.class.getName();
    return h2.equals(
        new Settings(JVereinDBService.class).getString("database.driver", h2));
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return true;
  }

  @Override
  public int getDefaultIndex()
  {
    return 0;
  }

  @Override
  public String getName()
  {
    return "JVerein: H2 DB Migration";
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    // 2-spaltige Anzeige. Links das Icon, rechts Text und Buttons
    parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    parent.setLayout(new GridLayout(2, false));

    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
        | GridData.VERTICAL_ALIGN_BEGINNING);
    gd.verticalSpan = 3;
    Label icon = new Label(parent, SWT.NONE);
    icon.setLayoutData(gd);
    icon.setImage(SWTUtil.getImage("jverein-icon-64x64.png"));

    Label title = new Label(parent, SWT.NONE);
    title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    title.setFont(Font.H2.getSWTFont());
    title.setText("H2-Datenbank Migration");

    Label desc = new Label(parent, SWT.WRAP);
    desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    desc.setText("Die Datenbank von OpenJVerein muss migriert werden");

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Migration starten", e -> MigratH2Database.start(), null,
        false, "go-next.png");
    buttons.paint(parent);

  }

  @Override
  public int getHeight()
  {
    return 160;
  }
}
