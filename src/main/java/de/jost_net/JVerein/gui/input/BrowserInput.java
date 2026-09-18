package de.jost_net.JVerein.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.util.Font;

public class BrowserInput extends AbstractInput
{
  private String value;

  private Browser browser;

  private boolean focus;

  private boolean enabled;

  public BrowserInput(String value)
  {
    this.value = value;
  }

  @Override
  public Object getValue()
  {
    if (browser == null || browser.isDisposed())
      return value;
    return browser.getText();
  }

  @Override
  public void setValue(Object value)
  {
    String s = value == null ? null : value.toString();
    this.value = s;

    if (this.browser != null && !this.browser.isDisposed())
    {
      this.browser.setText(s == null ? "" : s);
      this.browser.redraw();
    }
  }

  @Override
  public Control getControl()
  {
    if (browser != null)
      return browser;

    browser = new Browser(getParent(), SWT.BORDER);
    browser.setFont(Font.DEFAULT.getSWTFont());
    browser.setText((value == null ? "" : value));

    if (this.focus)
      browser.setFocus();
    return browser;
  }

  @Override
  public int getStyleBits()
  {
    return GridData.FILL_BOTH;
  }

  @Override
  public void focus()
  {
    this.focus = true;
    if (browser != null && !browser.isDisposed())
      browser.setFocus();
  }

  @Override
  public void disable()
  {
    setEnabled(false);
  }

  @Override
  public void enable()
  {
    setEnabled(true);
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  @Override
  public boolean isEnabled()
  {
    return enabled;
  }

}
