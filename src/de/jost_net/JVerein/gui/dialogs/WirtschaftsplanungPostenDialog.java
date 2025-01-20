package de.jost_net.JVerein.gui.dialogs;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;

public class WirtschaftsplanungPostenDialog extends AbstractDialog<WirtschaftsplanItem> {
    private final WirtschaftsplanItem item;

    public WirtschaftsplanungPostenDialog(WirtschaftsplanItem item) throws RemoteException {
        super(AbstractDialog.POSITION_CENTER);

        this.item = item;

        String title = "Bearbeite Posten " + item.getPosten() + " für Buchungsart " +
                item.getBuchungsart().getBezeichnung() +
                " (" + item.getBuchungsklasse().getBezeichnung() + ")";

        setTitle(title);
        setSize(620, SWT.DEFAULT);
    }

    @Override
    protected void paint(Composite parent) throws Exception {
        SimpleContainer group = new SimpleContainer(parent);

        TextInput postenInput = new TextInput(item.getPosten());
        group.addLabelPair("Posten Name", postenInput);
        DecimalInput sollInput = new DecimalInput(item.getSoll(), Einstellungen.DECIMALFORMAT);
        group.addLabelPair("Soll", sollInput);

        ButtonArea buttonArea = new ButtonArea();
        buttonArea.addButton("OK", context -> {
            try {
                item.setPosten((String) postenInput.getValue());
                item.setSoll((Double) sollInput.getValue());
            }
            catch (RemoteException e) {
                throw new ApplicationException(e);
            }
            close();
        }, null, false, "ok.png");
        buttonArea.addButton("Abbrechen", context -> {
            throw new OperationCanceledException();
        }, null, false, "process-stop.png");
        buttonArea.paint(parent);
    }

    @Override
    protected WirtschaftsplanItem getData() throws Exception {
        return item;
    }
}
