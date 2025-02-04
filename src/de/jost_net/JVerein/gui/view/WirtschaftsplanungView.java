/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
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
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
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
  private static int EINNAHME = 0;
  private static int AUSGABE = 1;

  @Override
  public void bind() throws Exception
  {
    if (!(this.getCurrentObject() instanceof WirtschaftsplanungZeile))
    {
      throw new ApplicationException(
          "Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung");

    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(
        this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(
        control);
    uebersicht.paint(this.getParent());
    control.setUebersicht(uebersicht);

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen", true);
    TreePart treeEinnahmen = control.getEinnahmen();
    treeEinnahmen.setContextMenu(new WirtschaftsplanungMenu(EINNAHME, control));
    einnahmen.addPart(treeEinnahmen);
    ButtonArea buttonsEinnahmen = new ButtonArea();
    buttonsEinnahmen.addButton("Buchungsklasse hinzufügen", context -> {
      try
      {
        @SuppressWarnings("unchecked")
        List<WirtschaftsplanungNode> items = (List<WirtschaftsplanungNode>) treeEinnahmen.getItems();
        Buchungsklasse buchungsklasse = showBuchungsklassenDialog(items);

        WirtschaftsplanungNode node = new WirtschaftsplanungNode(buchungsklasse,
            0, control.getWirtschaftsplanungZeile());
        items.add(node);
        treeEinnahmen.removeAll();
        treeEinnahmen.setList(items);
      }
      catch (Exception e)
      {
        throw new ApplicationException(
            "Fehler beim Hinzufügen der Buchungsklasse");
      }
    }, false, false, "list-add.png");
    einnahmen.addButtonArea(buttonsEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben", true);
    TreePart treeAusgaben = control.getAusgaben();
    treeAusgaben.setContextMenu(new WirtschaftsplanungMenu(AUSGABE, control));
    ausgaben.addPart(treeAusgaben);
    ButtonArea buttonsAusgaben = new ButtonArea();
    buttonsAusgaben.addButton("Buchungsklasse hinzufügen", context -> {
      try
      {
        //noinspection unchecked
        List<WirtschaftsplanungNode> items = (List<WirtschaftsplanungNode>) treeAusgaben.getItems();
        Buchungsklasse buchungsklasse = showBuchungsklassenDialog(items);

        WirtschaftsplanungNode node = new WirtschaftsplanungNode(buchungsklasse,
            1, control.getWirtschaftsplanungZeile());
        items.add(node);
        treeAusgaben.removeAll();
        treeAusgaben.setList(items);
      }
      catch (Exception e)
      {
        throw new ApplicationException(
            "Fehler beim Hinzufügen der Buchungsklasse");
      }
    }, false, false, "list-add.png");
    ausgaben.addButtonArea(buttonsAusgaben);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.addButton("CSV", context -> control.starteAuswertung(
        WirtschaftsplanungControl.AUSWERTUNG_CSV), null, false, "xsd.png");
    buttons.addButton("PDF", context -> control.starteAuswertung(
        WirtschaftsplanungControl.AUSWERTUNG_PDF), null, false, "file-pdf.png");
    buttons.addButton("Speichern", context -> control.handleStore(), null,
        false, "document-save.png");
    buttons.paint(this.getParent());
  }

  private Buchungsklasse showBuchungsklassenDialog(
      List<WirtschaftsplanungNode> items) throws Exception
  {
    DBIterator<Buchungsklasse> iterator;
    List<Buchungsklasse> buchungsklassen = new ArrayList<>();

    iterator = Einstellungen.getDBService().createList(Buchungsklasse.class);
    while (iterator.hasNext())
    {
      Buchungsklasse buchungsklasse = iterator.next();
      if (items.stream().map(WirtschaftsplanungNode::getBuchungsklasse)
          .noneMatch(klasse -> {
            try
            {
              return klasse.equals(buchungsklasse);
            }
            catch (RemoteException e)
            {
              throw new RuntimeException(e);
            }
          }))
      {
        buchungsklassen.add(buchungsklasse);
      }
    }

    DropdownDialog<Buchungsklasse> dialog = new DropdownDialog<>(
        buchungsklassen);
    return dialog.open();
  }
}
