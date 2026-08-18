/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.gui.dialogs;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Ein Dialog, zur Auswahl eines Kalenderjahres.
 */
public class ProfilnameNeuDialog extends AbstractDialog<String>
{
  private TextInput profilInput;

  private LabelInput status = null;

  private String profilName;

  private List<String> list;

  public ProfilnameNeuDialog(int position, List<String> list)
  {
    super(position);
    this.list = list;
    setTitle("Profilname eingeben");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer sc = new SimpleContainer(parent);
    sc.addInput(getProfilInput());
    sc.addInput(getStatus());

    ButtonArea b = new ButtonArea();
    b.addButton("Übernehmen", context -> {
      profilName = (String) getProfilInput().getValue();
      if (profilName == null || profilName.isBlank())
      {
        status.setValue("Bitte Profilnamen eingeben.");
        status.setColor(Color.ERROR);
        return;
      }
      String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
      for (int i = 0; i < profilName.length(); i++)
      {
        char c = profilName.charAt(i);
        if (validChars.indexOf(c) == -1)
        {
          status.setValue(String.format(
              "Ungültiges Zeichen (%s) an Position %d, nur A-Z, a-z, 0-9, _ erlaubt!",
              c, i + 1));
          status.setColor(Color.ERROR);
          return;
        }
      }
      if (list != null && list.contains(profilName))
      {
        status.setValue("Ein Profil mit dem namen existiert bereits.");
        status.setColor(Color.ERROR);
        return;
      }
      close();
    }, null, true, "ok.png");

    b.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  private TextInput getProfilInput()
  {
    if (profilInput != null)
    {
      return profilInput;
    }
    profilInput = new TextInput("", 50);
    profilInput.setMandatory(true);
    return profilInput;
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  @Override
  protected String getData() throws Exception
  {
    return profilName;
  }

}
