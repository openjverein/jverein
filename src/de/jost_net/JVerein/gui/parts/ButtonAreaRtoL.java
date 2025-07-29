/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer Buttons. Das ist die neue
 * Button-Area. Sie hat den Vorteil, dass sie {@link Part} implementiert und
 * daher erzeugt werden kann, bevor das {@link Composite} bekannt ist.
 * 
 * Die Buttons werden rechts bündig gezeichnet. Damit werden auch bei nicht
 * gezeichneten Buttons diese von rechts aufgefüllt und damit übrige Spalten auf
 * der linken Seite.
 * 
 */
public class ButtonAreaRtoL implements Part
{
  private List<ButtonRtoL> buttons = new ArrayList<ButtonRtoL>();

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.numColumns = buttons.size();

    Composite comp = new Composite(parent, SWT.RIGHT_TO_LEFT);
    comp.setLayout(layout);
    comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    for (int i = buttons.size() - 1; i >= 0; i--)
    {
      buttons.get(i).paint(comp);
    }
  }

  /**
   * fuegt der Area einen Button hinzu.
   * 
   * @param button
   *          der Button.
   */
  public void addButton(ButtonRtoL button)
  {
    this.buttons.add(button);
  }

  /**
   * Fuegt der Area einen Button hinzu. Beim Klick wird die Action ausgeloest.
   * 
   * @param name
   *          Bezeichnung des Buttons.
   * @param action
   *          auszuloesende Action.
   */
  public void addButton(String name, final Action action)
  {
    addButton(name, action, null);
  }

  /**
   * Fuegt der Area einen Button hinzu. Beim Klick wird die Action ausgeloest.
   * 
   * @param name
   *          Bezeichnung des Buttons.
   * @param action
   *          auszuloesende Action.
   * @param context
   *          Optionaler Context, der der Action mitgegeben wird.
   */
  public void addButton(String name, final Action action, final Object context)
  {
    addButton(name, action, context, false);
  }

  /**
   * Fuegt der Area einen Button hinzu. Beim Klick wird die Action ausgeloest.
   * 
   * @param name
   *          Bezeichnung des Buttons.
   * @param action
   *          auszuloesende Action.
   * @param context
   *          Optionaler Context, der der Action mitgegeben wird.
   * @param isDefault
   *          markiert den per Default aktiven Button.
   */
  public void addButton(String name, final Action action, final Object context,
      boolean isDefault)
  {
    addButton(name, action, context, isDefault, null);
  }

  /**
   * Fuegt der Area einen Button hinzu. Beim Klick wird die Action ausgeloest.
   * 
   * @param name
   *          Bezeichnung des Buttons.
   * @param action
   *          auszuloesende Action.
   * @param context
   *          Optionaler Context, der der Action mitgegeben wird.
   * @param isDefault
   *          markiert den per Default aktiven Button.
   * @param icon
   *          Icon, welches links neben dem Button angezeigt werden soll.
   */
  public void addButton(String name, final Action action, final Object context,
      boolean isDefault, String icon)
  {
    this.buttons.add(new ButtonRtoL(name, action, context, isDefault, icon));
  }

}
