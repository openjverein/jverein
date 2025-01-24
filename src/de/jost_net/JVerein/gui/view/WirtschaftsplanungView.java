package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.gui.dialogs.DropdownDialog;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanungMenu;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class WirtschaftsplanungView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    if (! (this.getCurrentObject() instanceof WirtschaftsplanungZeile)) {
      throw new ApplicationException("Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung vom " +
            new JVDateFormatTTMMJJJJ().format(((WirtschaftsplanungZeile) this.getCurrentObject()).getWirtschaftsplan().getDatumVon()) +
            " bis " +
            new JVDateFormatTTMMJJJJ().format(((WirtschaftsplanungZeile) this.getCurrentObject()).getWirtschaftsplan().getDatumBis()));

    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(control);
    uebersicht.paint(this.getParent());
    control.setUebersicht(uebersicht);

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen");
    TreePart treeEinnahmen = control.getEinnahmen();
    treeEinnahmen.setContextMenu(new WirtschaftsplanungMenu(0, control));
    einnahmen.addPart(treeEinnahmen);
    ButtonArea buttonsEinnahmen = new ButtonArea();
    buttonsEinnahmen.addButton("Buchungsklasse hinzufügen", context -> {
      try
      {
        //noinspection unchecked
        List<WirtschaftsplanungNode> items = (List<WirtschaftsplanungNode>) treeEinnahmen.getItems();
        Buchungsklasse buchungsklasse = showBuchungsklassenDialog(items);

        WirtschaftsplanungNode node = new WirtschaftsplanungNode(buchungsklasse, 0, control.getWirtschaftsplanungZeile());
        items.add(node);
        treeEinnahmen.removeAll();
        treeEinnahmen.setList(items);
      }
      catch (Exception e) {
        throw new ApplicationException("Fehler beim Hinzufügen der Buchungsklasse");
      }
    }, false, false, "list-add.png");
    einnahmen.addButtonArea(buttonsEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben");
    TreePart treeAusgaben = control.getAusgaben();
    ausgaben.addPart(treeAusgaben);
    ButtonArea buttonsAusgaben = new ButtonArea();
    buttonsAusgaben.addButton("Buchungsklasse hinzufügen", context -> {
      try
      {
        //noinspection unchecked
        List<WirtschaftsplanungNode> items = (List<WirtschaftsplanungNode>) treeAusgaben.getItems();
        Buchungsklasse buchungsklasse = showBuchungsklassenDialog(items);

        WirtschaftsplanungNode node = new WirtschaftsplanungNode(buchungsklasse, 1, control.getWirtschaftsplanungZeile());
        items.add(node);
        treeAusgaben.removeAll();
        treeAusgaben.setList(items);
      }
      catch (Exception e) {
        throw new ApplicationException("Fehler beim Hinzufügen der Buchungsklasse");
      }
    }, false, false, "list-add.png");
    ausgaben.addButtonArea(buttonsAusgaben);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
            DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.addButton("CSV", null, null, false, "xsd.png");
    buttons.addButton("PDF", null, null, false, "file-pdf.png");
    buttons.addButton("Speichern", context -> control.handleStore(), null, false, "document-save.png");
    buttons.paint(this.getParent());
  }

  private Buchungsklasse showBuchungsklassenDialog(List<WirtschaftsplanungNode> items)
      throws Exception
  {
    DBIterator<Buchungsklasse> iterator;
    List<Buchungsklasse> buchungsklassen = new ArrayList<>();

    iterator = Einstellungen.getDBService().createList(Buchungsklasse.class);
    while (iterator.hasNext()) {
      Buchungsklasse buchungsklasse = iterator.next();
      if (items.stream()
          .map(WirtschaftsplanungNode::getBuchungsklasse)
          .noneMatch(klasse -> {
            try
            {
              return klasse.equals(buchungsklasse);
            }
            catch (RemoteException e)
            {
              throw new RuntimeException(e);
            }
          })
      ) {
        buchungsklassen.add(buchungsklasse);
      }
    }

    DropdownDialog<Buchungsklasse> dialog = new DropdownDialog<>(buchungsklassen);
    return dialog.open();
  }
}
