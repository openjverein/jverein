package de.jost_net.JVerein.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;

public class IntegerNullInput extends TextInput
{

  /**
   * ct. Parameterloser Konstruktor fuer ein Eingabefeld ohne Wert-Vorbelegung.
   * BUGZILLA 1275
   */
  public IntegerNullInput()
  {
    super("");
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * 
   * @param value
   *          anzuzeigender Wert.
   */
  public IntegerNullInput(int value)
  {
    super(value < 0 ? "" : "" + value);
  }

  @Override
  public Control getControl()
  {
    Control c = super.getControl();
    text.addListener(SWT.Verify, new Listener()
    {
      public void handleEvent(Event e)
      {
        char[] chars = e.text.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
          if (!('0' <= chars[i] && chars[i] <= '9'))
          {
            e.doit = false;
            return;
          }
        }
      }
    });
    return c;
  }

  /**
   * Die Funktion liefert ein Objekt des Typs {@link java.lang.Integer} zurueck
   * oder {@code null} wenn nichts eingegeben wurde.
   */
  @Override
  public Object getValue()
  {
    Object value = super.getValue();
    if (value == null || value.toString().length() == 0)
      return null;
    try
    {
      return Integer.valueOf(value.toString());
    }
    catch (NumberFormatException e)
    {
      Logger.error("error while parsing from int input", e);
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs {@link java.lang.Integer}.
   */
  @Override
  public void setValue(Object value)
  {
    if (value == null || (value instanceof Integer))
      super.setValue(value);
  }

}
