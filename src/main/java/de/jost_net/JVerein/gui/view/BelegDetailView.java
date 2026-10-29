package de.jost_net.JVerein.gui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.control.BelegDetailControl;
import de.jost_net.JVerein.gui.control.BelegDetailControl.Tab;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SaveNeuButton;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;

public class BelegDetailView extends AbstractDetailView
{

  private AbstractBelegDBObject belegObject;

  public BelegDetailView()
  {
  }

  public BelegDetailView(AbstractBelegDBObject belegObject)
  {
    this.belegObject = belegObject;
  }

  // Statische Variable, die den zuletzt ausgewählten Tab speichert.
  private static Tab tabindex = Tab.BUCHUNG;

  private BelegDetailControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Beleg");

    control = new BelegDetailControl(this, belegObject);

    LabelGroup group = new LabelGroup(getParent(), "Beleg");
    if (!((Beleg) getCurrentObject()).isNewObject())
    {
      group.addInput(control.getBelegnummer());
    }
    group.addInput(control.getBelegArt());
    group.addInput(control.getDatum());
    group.addInput(control.getBemerkung());
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

      TabGroup tabMitglieder = new TabGroup(folder, "Mitglieder", true, 1);
      control.getMitgliederList().paint(tabMitglieder.getComposite());

      // Aktiver zuletzt ausgewählter Tab.
      folder.setSelection(tabindex.getInt());
      control.setFolderSelection(tabindex);
      folder.addSelectionListener(new SelectionListener()
      {
        @Override
        public void widgetSelected(SelectionEvent evt)
        {
          tabindex = Tab.getByInt(folder.getSelectionIndex());
          control.setFolderSelection(tabindex);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e)
        {
        }
      });
    }

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton(new HelpButton(DokumentationUtil.BUCHUNGSART));
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(control.getAnzeigenButton());
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(this.getParent());

    if (!((Beleg) getCurrentObject()).isNewObject())
    {
      GUI.getView().addPanelButton(control.exportButton(ExportArt.PDF));
      GUI.getView().addPanelButton(control.exportButton(ExportArt.CSV));
      GUI.getView().addPanelButton(control.getDetailSpaltenPanelButton());
    }
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
