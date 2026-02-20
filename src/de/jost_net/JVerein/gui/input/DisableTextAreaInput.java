package de.jost_net.JVerein.gui.input;

import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.input.TextAreaInput;

/**
 * Der TextAreaInput aus Jameica ist so implementiert, dass bei "disabled" nur
 * das Editieren des Textes unterbunden wird. Das Textfeld wird aber nicht
 * "disabled" (ausgegraut). Das soll erlauben, dass man noch scrollen kann.
 * 
 * Der DisableTextAreaInput setzt bei "disabled" auch das Textfeld auf
 * "disabled". Scrollen geht dann nicht!
 * 
 */
public class DisableTextAreaInput extends TextAreaInput
{

  public DisableTextAreaInput(String value, int maxLength)
  {
    super(value, maxLength);
  }

  public DisableTextAreaInput(String value)
  {
    super(value);
  }

  /**
   * Setzt das Textfeld als enabled oder disabled.
   * 
   * @param enabled
   *          wenn true
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    if (this.text != null && !this.text.isDisposed())
    {
      text.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  /**
   * Erzeugt den Control in dem Moment indem er angezeigt werden soll.
   * 
   */
  @Override
  public Control getControl()
  {
    Control text = super.getControl();
    if (this.text != null && !this.text.isDisposed())
    {
      text.setEnabled(isEnabled());
    }
    return text;
  }
}
