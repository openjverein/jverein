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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.rmi.Version;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;

public class DBVersionTest extends AbstractBox
{

  @Override
  public boolean isActive()
  {
    return isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  @Override
  public boolean isEnabled()
  {
    try
    {
      de.willuhn.jameica.plugin.Version programIstVersion = Application
          .getPluginLoader().getPlugin(JVereinPlugin.class).getManifest()
          .getVersion();
      de.willuhn.jameica.plugin.Version programSollVersion = new de.willuhn.jameica.plugin.Version(
          ((Version) Einstellungen.getDBService().createObject(Version.class,
              "1")).getProgramVersion());

      return programIstVersion.getMajor() < programSollVersion.getMajor();
    }
    catch (RemoteException e)
    {
      return false;
    }
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
    return "JVerein: DB-Version";
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
    title.setText("Datenbank Version zu neu.");

    Label desc = new Label(parent, SWT.WRAP);
    desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    desc.setText("Die Version der Datenbank ist höher als die Programversion.\n"
        + "Das kann zu inkonsitenten Daten führen!");

  }

  @Override
  public int getHeight()
  {
    return 160;
  }
}
