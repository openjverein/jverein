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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Button.
 */
public class ButtonRtoL implements Part
{

  private String title = null;

  private String icon = null;

  private Action action = null;

  private Object context = null;

  private boolean isDefault = false;

  private boolean enabled = true;

  protected org.eclipse.swt.widgets.Button button = null;

  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   */
  public ButtonRtoL(String title, Action action)
  {
    this(title, action, null, false);
  }

  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   * @param context
   *          ein Context-Objekt, welches beim Click der Action uebergeben wird.
   */
  public ButtonRtoL(String title, Action action, Object context)
  {
    this(title, action, context, false);
  }

  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   * @param context
   *          ein Context-Objekt, welches beim Click der Action uebergeben wird.
   * @param defaultButton
   *          legt fest, ob das der Default-Button der Shell sein soll.
   */
  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton)
  {
    this(title, action, context, defaultButton, null);
  }

  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   * @param context
   *          ein Context-Objekt, welches beim Click der Action uebergeben wird.
   * @param defaultButton
   *          legt fest, ob das der Default-Button der Shell sein soll.
   * @param icon
   *          Icon, welches links neben der Beschriftung angezeigt werden soll.
   */
  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton, String icon)
  {
    this.title = title;
    this.action = action;
    this.context = context;
    this.isDefault = defaultButton;
    this.icon = icon;
  }

  /**
   * Legt fest, ob der Button aktiviert oder deaktiviert sein soll.
   * 
   * @param enabled
   *          true, wenn der Button anklickbar sein soll, sonst false.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.button != null && !this.button.isDisposed())
      this.button.setEnabled(this.enabled);
  }

  /**
   * Speichert den Text auf dem Button.
   * 
   * @param text
   *          der anzuzeigende Text.
   */
  public void setText(String text)
  {
    if (text == null)
      return;

    this.title = text;
    if (this.button != null && !this.button.isDisposed())
      this.button.setText(this.title);
  }

  /**
   * Setzt das Icon oder aendert es zur Laufzeit.
   * 
   * @param icon
   *          das anzuzeigende Icon.
   */
  public void setIcon(String icon)
  {
    this.icon = icon;
    if (this.button != null && !this.button.isDisposed())
      button.setImage(this.icon != null ? SWTUtil.getImage(this.icon) : null);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    button = new Button(parent, SWT.LEFT_TO_RIGHT | SWT.PUSH);
    button.setFont(Font.DEFAULT.getSWTFont());
    button.setText(this.title == null ? "" : this.title);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    if (this.icon != null)
      button.setImage(SWTUtil.getImage(this.icon));

    try
    {
      if (this.isDefault)
        parent.getShell().setDefaultButton(button);
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
        Logger
            .warn("unable to set default button: " + ae2.getLocalizedMessage());
      }
    }

    button.setEnabled(this.enabled);

    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        GUI.startSync(new Runnable()
        {
          public void run()
          {
            try
            {
              action.handleAction(context);
            }
            catch (ApplicationException e)
            {
              Application.getMessagingFactory()
                  .sendMessage(new StatusBarMessage(e.getMessage(),
                      StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }
    });
  }

}
