package de.jost_net.JVerein.gui.dialogs;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

public class DropdownDialog<T> extends AbstractDialog<T>
{
  private final List<T> auswahl;

  private SelectInput auswahlInput;

  private T value;

  public DropdownDialog(List<T> auswahl) {
    super(AbstractDialog.POSITION_CENTER);

    this.auswahl = auswahl;
    setTitle("Auswahl");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer group = new SimpleContainer(parent);

    auswahlInput = new SelectInput(auswahl, auswahl.isEmpty() ? null : auswahl.get(0));
    group.addLabelPair("Bitte wählen", auswahlInput);

    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton("OK", e -> {
      value = (T) auswahlInput.getValue();
      close();
    }, null, false, "ok.png");
    buttonArea.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttonArea.paint(parent);
  }

  @Override
  protected T getData() throws Exception
  {
    return value;
  }
}
