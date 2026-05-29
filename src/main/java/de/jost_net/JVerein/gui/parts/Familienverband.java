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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.logging.Logger;

public class Familienverband implements Part
{
  private TabFolder tab;

  private Beitragsgruppe gruppe;

  private MitgliedControl control;

  public Familienverband(MitgliedControl control, Beitragsgruppe gruppe)
  {
    this.control = control;
    this.gruppe = gruppe;
    this.tab = null;
  }

  /**
   * Zeichnet den Familienverband Part.
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    // Familienverband soll angezeigt werden...

    // Hier beginnt das eigentlich Zeichnen des Familienverbandes:
    LabelGroup cont = new LabelGroup(parent, "Familienverband");
    final GridData g = new GridData(GridData.FILL_HORIZONTAL);
    tab = new TabFolder(cont.getComposite(), SWT.NONE);
    tab.setLayoutData(g);
    TabGroup tg1 = new TabGroup(tab, "Familienmitglieder");
    control.getFamilienangehoerigenTable().paint(tg1.getComposite());
    TabGroup tg2 = new TabGroup(tab, "Vollzahlendes Familienmitglied");
    // erstelle neuen zahler: (force == true)
    control.getZahler(true)
        .setComment("Nur für Beitragsgruppenart: \"Familienangehörige\"");
    tg2.addLabelPair("Vollzahler", control.getZahler());

    if (gruppe != null)
    {
      setBeitragsgruppe(gruppe);
    }

  }

  /**
   * Aktiviert den ersten Tab, wenn Beitragsgruppe FAMILIE_ZAHLER ist, ansonsten
   * den zweiten Tab. So kann für Mitglieder deren Beitragsgruppe
   * FAMILIE_ANGEHOERIGER direkt auf dem zweiten Tab ihr Familien-Zahler
   * eingestellt werden.
   * 
   * @param gruppe
   */
  public void setBeitragsgruppe(Beitragsgruppe gruppe)
  {
    this.gruppe = gruppe;
    if (tab == null)
      return;
    try
    {
      if (gruppe.getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
        tab.setSelection(0);
      else if (gruppe.getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
        tab.setSelection(1);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    tab.redraw();
    tab.layout(true);
  }
}
