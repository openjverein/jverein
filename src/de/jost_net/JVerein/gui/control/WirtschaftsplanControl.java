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
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode.Type;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanListMenu;
import de.jost_net.JVerein.gui.parts.EditTreePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.gui.view.WirtschaftsplanDetailView;
import de.jost_net.JVerein.io.WirtschaftsplanCSV;
import de.jost_net.JVerein.io.WirtschaftsplanPDF;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class WirtschaftsplanControl extends VorZurueckControl implements Savable
{
  public final static String AUSWERTUNG_PDF = "PDF";

  public final static String AUSWERTUNG_CSV = "CSV";

  private EditTreePart einnahmen;

  private EditTreePart ausgaben;

  private WirtschaftsplanUebersichtPart uebersicht;

  private Wirtschaftsplan wirtschaftsplan;

  private boolean tableChanged = false;

  /**
   * Erzeugt einen neuen WirtschaftsplanControl fuer die angegebene View.
   *
   * @param view
   *          die View, fuer die dieser WirtschaftsplanControl zustaendig ist.
   * @throws RemoteException
   */
  public WirtschaftsplanControl(AbstractView view) throws RemoteException
  {
    super(view);
    de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(
        this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Liefert die Liste der Wirtschaftsplaene.
   *
   * @return die Liste der Wirtschaftsplaene.
   * @throws RemoteException
   *           wenn ein Fehler beim Zugriff auf die Datenbank auftritt.
   */
  public Part getWirtschaftsplanungList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();

    JVereinTablePart wirtschaftsplaene = new JVereinTablePart(
        service.createList(Wirtschaftsplan.class), null);

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);
    DateFormatter dateFormatter = new DateFormatter(new JVDateFormatTTMMJJJJ());

    wirtschaftsplaene.addColumn("ID", "id");
    wirtschaftsplaene.addColumn("Bezeichnung", "bezeichnung");
    wirtschaftsplaene.addColumn("Von", "datum_von", dateFormatter);
    wirtschaftsplaene.addColumn("Bis", "datum_bis", dateFormatter);
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Soll", "planSaldo", formatter);
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istPlus", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istMinus", formatter);
    wirtschaftsplaene.addColumn("Saldo Ist", "istSaldo", formatter);
    wirtschaftsplaene.addColumn("Saldo Differenz", "differenz", formatter);

    wirtschaftsplaene
        .setContextMenu(new WirtschaftsplanListMenu(wirtschaftsplaene));
    wirtschaftsplaene.setAction(
        new EditAction(WirtschaftsplanDetailView.class, wirtschaftsplaene));
    VorZurueckControl.setObjektListe(null, null);

    return wirtschaftsplaene;
  }

  /**
   * Liefert den aktuellen Wirtschaftsplan.
   *
   * @return der aktuelle Wirtschaftsplan oder null, wenn kein Wirtschaftsplan
   *         geladen ist.
   * @throws RemoteException
   *           wenn ein Fehler beim Zugriff auf die Datenbank auftritt.
   */
  public Wirtschaftsplan getWirtschaftsplan() throws RemoteException
  {
    if (wirtschaftsplan != null)
    {
      return wirtschaftsplan;
    }
    wirtschaftsplan = (Wirtschaftsplan) getCurrentObject();
    return wirtschaftsplan;
  }

  public EditTreePart getEinnahmen() throws RemoteException
  {
    if (einnahmen == null)
    {
      einnahmen = generateTree(WirtschaftsplanImpl.EINNAHME);
    }
    else
    {
      @SuppressWarnings("rawtypes")
      List items = einnahmen.getItems();
      einnahmen.setList(items);
    }
    return einnahmen;
  }

  public EditTreePart getAusgaben() throws RemoteException
  {
    if (ausgaben == null)
    {
      ausgaben = generateTree(WirtschaftsplanImpl.AUSGABE);
    }
    else
    {
      @SuppressWarnings("rawtypes")
      List items = ausgaben.getItems();
      ausgaben.setList(items);
    }
    return ausgaben;
  }

  @SuppressWarnings("unchecked")
  private EditTreePart generateTree(int art) throws RemoteException
  {
    Wirtschaftsplan wirtschaftsplan = getWirtschaftsplan();

    if (wirtschaftsplan == null)
    {
      return null;
    }

    ArrayList<WirtschaftsplanNode> nodes = new ArrayList<>();

    DBService service = Einstellungen.getDBService();

    DBIterator<Buchungsklasse> buchungsklasseIterator = service
        .createList(Buchungsklasse.class);
    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
    {
      case BuchungsartSort.NACH_NUMMER:
        buchungsklasseIterator.setOrder("Order by -nummer DESC");
        break;
      case BuchungsartSort.NACH_BEZEICHNUNG_NR:
      default:
        buchungsklasseIterator
            .setOrder("Order by bezeichnung is NULL, bezeichnung");
        break;
    }
    while (buchungsklasseIterator.hasNext())
    {
      Buchungsklasse klasse = buchungsklasseIterator.next();
      nodes.add(new WirtschaftsplanNode(klasse, art, wirtschaftsplan));
    }

    EditTreePart treePart = new EditTreePart(nodes, null)
    {
      @Override
      protected Context createFeatureEventContext(Feature.Event e, Object data)
      {
        Context ctx = super.createFeatureEventContext(e, data);
        if (e.equals(Feature.Event.PAINT))
        {
          autoExpand(this);
        }
        return ctx;
      }
    };

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);
    treePart.addColumn("Buchungsklasse / Buchungsart / Posten",
        "buchungsklassebezeichnung", null, true);
    treePart.addColumn("Soll", "soll", formatter, true);
    treePart.addColumn("Ist", "ist", formatter);

    treePart.addChangeListener(((object, attribute, newValue) -> {
      if (!(object instanceof WirtschaftsplanNode))
      {
        throw new ApplicationException("Fehler!");
      }

      WirtschaftsplanNode node = (WirtschaftsplanNode) object;

      WirtschaftsplanItem item = node.getWirtschaftsplanItem();

      if (item == null)
      {
        throw new ApplicationException("Fehler beim Bearbeiten!");
      }
      try
      {
        switch (attribute)
        {
          case "buchungsartbezeichnung_posten":
            item.setPosten(newValue);
            break;
          case "soll":
            try
            {
              NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
              item.setSoll(nf.parse(newValue).doubleValue());
            }
            catch (NumberFormatException | ParseException e)
            {
              GUI.getStatusBar().setErrorText(
                  "Bitte gib eine gültige Zahl im deutschen Format ein!");
              throw new ApplicationException("Keine Zahl eingegeben");
            }
            break;
          default:
            throw new ApplicationException("Fehler!");
        }

        node.setWirtschaftsplanItem(item);
        node.setSoll(item.getSoll());

        GenericIterator<WirtschaftsplanNode> children = node.getChildren();
        if (node.getType() == Type.BUCHUNGSART && children != null
            && children.size() == 1)
        {
          WirtschaftsplanNode child = children.next();
          child.setWirtschaftsplanItem(item);
          child.setSoll(item.getSoll());
          reloadSoll(node);
        }
        else
        {
          WirtschaftsplanNode parent = (WirtschaftsplanNode) node.getParent();
          reloadSoll(parent);
        }
        autoExpand(einnahmen);
        autoExpand(ausgaben);

        tableChanged = true;
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException("Fehler!");
      }
    }));

    treePart.addEditListener(((object, attribute) -> {
      WirtschaftsplanNode node = (WirtschaftsplanNode) object;

      switch (node.getType())
      {
        case POSTEN:
          return true;
        case BUCHUNGSART:
          try
          {
            // Wenn nur ein Posten existiert, kann direkt bei der Buchungsart
            // der Betrag eingegeben werden.
            return (node.getChildren() == null
                || node.getChildren().size() == 1) && attribute.equals("soll");
          }
          catch (RemoteException e)
          {
            return false;
          }
        default:
          return false;
      }

    }));

    return treePart;
  }

  @SuppressWarnings("unchecked")
  private void autoExpand(TreePart tree)
  {
    try
    {
      for (WirtschaftsplanNode klasseNode : (List<WirtschaftsplanNode>) tree
          .getItems())
      {
        GenericIterator<WirtschaftsplanNode> children = klasseNode
            .getChildren();
        if (children == null)
        {
          continue;
        }
        while (children.hasNext())
        {
          WirtschaftsplanNode node = children.next();
          if (node.getChildren() != null && node.getChildren().size() == 1)
          {
            tree.setExpanded(node, false);
          }
        }
      }
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler beim AutoExpand", e1);
    }
  }

  public WirtschaftsplanUebersichtPart getUebersicht()
  {
    return uebersicht;
  }

  public void setUebersicht(WirtschaftsplanUebersichtPart uebersicht)
  {
    this.uebersicht = uebersicht;
  }

  public void reloadSoll(WirtschaftsplanNode parent)
      throws RemoteException, ApplicationException
  {
    if (parent.getType() == WirtschaftsplanNode.Type.BUCHUNGSKLASSE)
    {
      @SuppressWarnings("rawtypes")
      GenericIterator outerIterator = parent.getChildren();
      double klasseSoll = 0;
      while (outerIterator.hasNext())
      {
        WirtschaftsplanNode child = (WirtschaftsplanNode) outerIterator.next();
        double artSoll = 0;
        @SuppressWarnings("rawtypes")
        GenericIterator innerIterator = child.getChildren();
        while (innerIterator.hasNext())
        {
          WirtschaftsplanNode leaf = (WirtschaftsplanNode) innerIterator.next();
          artSoll += leaf.getSoll();
        }
        child.setSoll(artSoll);
        klasseSoll += artSoll;
      }
      parent.setSoll(klasseSoll);
    }
    else
    {
      while (parent != null)
      {
        @SuppressWarnings("rawtypes")
        GenericIterator iterator = parent.getChildren();
        double soll = 0;
        while (iterator.hasNext())
        {
          WirtschaftsplanNode child = (WirtschaftsplanNode) iterator.next();
          soll += child.getSoll();
        }
        parent.setSoll(soll);

        parent = (WirtschaftsplanNode) parent.getParent();
      }
    }

    uebersicht.updateSoll();
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Wirtschaftsplan wirtschaftsplan = getWirtschaftsplan();

    wirtschaftsplan
        .setBezeichnung((String) uebersicht.getBezeichnung().getValue());
    Date von = (Date) uebersicht.getVon().getValue();
    Date bis = (Date) uebersicht.getBis().getValue();
    wirtschaftsplan.setDatumBis(bis);
    wirtschaftsplan.setDatumVon(von);

    return wirtschaftsplan;
  }

  public void handleStore()
  {
    try
    {
      @SuppressWarnings("unchecked")
      List<WirtschaftsplanNode> rootNodesEinnahmen = (List<WirtschaftsplanNode>) einnahmen
          .getItems();
      @SuppressWarnings("unchecked")
      List<WirtschaftsplanNode> rootNodesAusgaben = (List<WirtschaftsplanNode>) ausgaben
          .getItems();

      DBService service = Einstellungen.getDBService();
      Wirtschaftsplan wirtschaftsplan = (Wirtschaftsplan) prepareStore();

      DBTransaction.starten();

      checkDate();

      wirtschaftsplan.store();

      if (!wirtschaftsplan.isNewObject())
      {
        DBIterator<WirtschaftsplanItem> iterator = service
            .createList(WirtschaftsplanItem.class);
        iterator.addFilter("wirtschaftsplan = ?", wirtschaftsplan.getID());
        while (iterator.hasNext())
        {
          iterator.next().delete(); // Löschen alter Einträge, wird später neu
                                    // angelegt
        }
      }

      for (WirtschaftsplanNode rootNode : rootNodesEinnahmen)
      {
        storeNodes(rootNode.getChildren(), wirtschaftsplan.getID());
      }
      for (WirtschaftsplanNode rootNode : rootNodesAusgaben)
      {
        storeNodes(rootNode.getChildren(), wirtschaftsplan.getID());
      }

      DBTransaction.commit();

      tableChanged = false;

      view.reload();

      GUI.getStatusBar().setSuccessText("Wirtschaftsplan gespeichert");
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();

      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      DBTransaction.rollback();

      String fehler = "Fehler beim Speichern des Wirtschaftsplans";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  @SuppressWarnings("rawtypes")
  private void storeNodes(GenericIterator iterator, String id)
      throws RemoteException, ApplicationException
  {
    while (iterator.hasNext())
    {
      WirtschaftsplanNode currentNode = (WirtschaftsplanNode) iterator.next();
      if (currentNode.getType().equals(WirtschaftsplanNode.Type.POSTEN))
      {
        WirtschaftsplanItem item = Einstellungen.getDBService()
            .createObject(WirtschaftsplanItem.class, null);
        WirtschaftsplanItem oldItem = currentNode.getWirtschaftsplanItem();
        item.setPosten(oldItem.getPosten());
        item.setSoll(oldItem.getSoll());
        item.setWirtschaftsplanId(id);
        WirtschaftsplanNode parent = (WirtschaftsplanNode) currentNode
            .getParent();
        item.setBuchungsartId(parent.getBuchungsart().getID());
        WirtschaftsplanNode root = (WirtschaftsplanNode) parent.getParent();
        item.setBuchungsklasseId(root.getBuchungsklasse().getID());
        item.store();
      }
      else
      {
        storeNodes(currentNode.getChildren(), id);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void starteAuswertung(String type) throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    //
    Settings settings = new Settings(this.getClass());
    //
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && !path.isEmpty())
    {
      fd.setFilterPath(path);
    }

    fd.setFileName("wirtschaftsplan." + type);

    final String s = fd.open();

    if (s == null || s.isEmpty())
    {
      return;
    }

    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());

    List<WirtschaftsplanNode> einnahmenList;
    List<WirtschaftsplanNode> ausgabenList;

    try
    {
      einnahmenList = (List<WirtschaftsplanNode>) einnahmen.getItems();
      ausgabenList = (List<WirtschaftsplanNode>) ausgaben.getItems();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(String
          .format("Fehler beim Erstellen der Reports: %s", e.getMessage()));
    }

    BackgroundTask task = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        switch (type)
        {
          case AUSWERTUNG_CSV:
            new WirtschaftsplanCSV(einnahmenList, ausgabenList, file);
            break;
          case AUSWERTUNG_PDF:
            try
            {
              new WirtschaftsplanPDF(einnahmenList, ausgabenList, file,
                  getWirtschaftsplan());
            }
            catch (RemoteException e)
            {
              throw new ApplicationException(
                  "Fehler beim Zugriff auf den Wirtschaftsplan");
            }
            break;
          default:
            GUI.getStatusBar().setErrorText(
                "Report konnte nicht erzeugt werden! Das Format ist unbekannt!");
        }
      }

      @Override
      public void interrupt()
      {

      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(task);
  }

  public void checkDate() throws ApplicationException
  {
    Date von = (Date) uebersicht.getVon().getValue();
    Date bis = (Date) uebersicht.getBis().getValue();
    if (von == null)
    {
      throw new ApplicationException("Von-Datum darf nicht leer sein!");
    }
    if (bis == null)
    {
      throw new ApplicationException("Bis-Datum darf nicht leer sein!");
    }
    if (bis.before(von))
    {
      throw new ApplicationException("Bis-Datum muss nach Von-Datum liegen");
    }
  }

  @Override
  public boolean hasChanged() throws RemoteException
  {
    if (!(getWirtschaftsplan() instanceof Wirtschaftsplan))
    {
      return false;
    }
    Wirtschaftsplan wirtschaftsplan = getWirtschaftsplan();

    if (wirtschaftsplan.isNewObject())
    {
      return true;
    }

    if (wirtschaftsplan.isChanged())
    {
      return true;
    }

    return tableChanged;
  }

  /**
   * Setzt den Status des Controls auf "geändert". Wird z.B. von den Editoren
   * aufgerufen, wenn sich etwas geändert hat.
   */
  public void setToChanged()
  {
    tableChanged = true;
  }
}
