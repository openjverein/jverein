package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class WirtschaftsplanungDetailPart implements Part {
    private WirtschaftsplanungControl control;

    public WirtschaftsplanungDetailPart(WirtschaftsplanungControl control) {
        this.control = control;
    }

    @Override
    public void paint(Composite parent) throws RemoteException {
        SimpleContainer group = new SimpleContainer(parent, true, 2);

        LabelGroup links = new LabelGroup(group.getComposite(), "Einnahmen");
        TablePart test = new TablePart(new ArrayList<>(), null);
        links.addPart(test);
        LabelGroup rechts = new LabelGroup(group.getComposite(), "Ausgaben");
        TablePart test1 = new TablePart(new ArrayList<>(), null);
        rechts.addPart(test1);
    }
}
