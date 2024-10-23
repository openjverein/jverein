package de.jost_net.JVerein.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import de.willuhn.jameica.gui.input.SpinnerInput;

public class SpinnerNoScrollInput extends SpinnerInput
{

  public SpinnerNoScrollInput(int min, int max, int value)
  {
    super(min, max, value);
  }


  public Control getControl()
  {
    final Spinner spinner = (Spinner) super.getControl();
    spinner.addListener(SWT.MouseVerticalWheel, new Listener()
    {
      @Override
      public void handleEvent(Event arg0)
      {
        arg0.doit = false;
      }
    });
    return spinner;
  }
}
