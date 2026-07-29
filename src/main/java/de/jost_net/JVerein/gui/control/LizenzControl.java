/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.FileInputStream;

import de.jost_net.JVerein.JVereinPlugin;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

public class LizenzControl extends AbstractControl
{
  public LizenzControl(AbstractView view)
  {
    super(view);
  }

  public FormTextPart getLibList()
  {
    String path = Application.getPluginLoader().getPlugin(JVereinPlugin.class)
        .getManifest().getPluginDir();

    File file = new File(path + "/THIRD-PARTY.txt");

    String text;
    try (FileInputStream fis = new FileInputStream(file))
    {
      text = new String(fis.readAllBytes());
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim lesen der Datei", e);
      text = "Fehler beim lesen der Datei";
    }
    return new FormTextPart(text);
  }

}
