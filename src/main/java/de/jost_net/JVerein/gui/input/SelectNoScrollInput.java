package de.jost_net.JVerein.gui.input;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.SelectInput;

public class SelectNoScrollInput extends SelectInput
{

  @SuppressWarnings("rawtypes")
  public SelectNoScrollInput(List list, Object preselected)
  {
    super(list, preselected);
  }

  public SelectNoScrollInput(Object[] list, Object preselected)
  {
    super(list, preselected);
  }

  public Control getControl()
  {
    final Combo combo = (Combo) super.getControl();
    combo.addListener(SWT.MouseVerticalWheel, new Listener()
    {
      @Override
      public void handleEvent(Event arg0)
      {
        if(!combo.getListVisible())
          arg0.doit = false;
      }
    });
    return combo;
  }
}
