package de.jost_net.JVerein.gui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.BelegControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SaveNeuButton;
import de.jost_net.JVerein.rmi.IBeleg;
import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;

public class BelegDetailView extends AbstractDetailView
{

  private IBeleg belegObject;

  public BelegDetailView()
  {
  }

  public BelegDetailView(IBeleg belegObject)
  {
    this.belegObject = belegObject;
  }

  // Statische Variable, die den zuletzt ausgewählten Tab speichert.
  private static int tabindex = -1;

  private BelegControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Beleg");

    control = new BelegControl(this, belegObject);

    LabelGroup group = new LabelGroup(getParent(), "Beleg");
    group.addInput(control.getBezeichnung());
    if (((Changeable) getCurrentObject()).isNewObject())
    {
      group.addInput(control.getDatei());
    }
    else
    {
      group.addInput(control.getPfad());

      TabFolder folder = new TabFolder(getParent(), SWT.BORDER);
      folder.setLayoutData(new GridData(GridData.FILL_BOTH));

      TabGroup tabBuchung = new TabGroup(folder, "Buchungen", true, 1);
      control.getBuchungList().paint(tabBuchung.getComposite());

      // Aktiver zuletzt ausgewählter Tab.
      if (tabindex != -1)
      {
        folder.setSelection(tabindex);
        control.setFolderSelection(tabindex);
      }
      folder.addSelectionListener(new SelectionListener()
      {
        @Override
        public void widgetSelected(SelectionEvent evt)
        {
          tabindex = folder.getSelectionIndex();
          control.setFolderSelection(tabindex);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e)
        {
        }
      });
    }

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGSART, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
