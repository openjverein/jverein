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

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.WirtschaftsplanEditAction;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungPostenDialog;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.io.WirtschaftsplanungCSV;
import de.jost_net.JVerein.io.WirtschaftsplanungPDF;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WirtschaftsplanungControl extends AbstractControl
{
  private TreePart einnahmen;

  private TreePart ausgaben;

  private WirtschaftsplanUebersichtPart uebersicht;

  public final static String AUSWERTUNG_PDF = "PDF";

  public final static String AUSWERTUNG_CSV = "CSV";

  /**
   * Erzeugt einen neuen AbstractControl der fuer die angegebene View.
   *
   * @param view
   *     die View, fuer die dieser WirtschaftsplanungControl zustaendig ist.
   */
  public WirtschaftsplanungControl(AbstractView view)
  {
    super(view);
    de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(
        this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Part getWirtschaftsplanungList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();

    String sql = "SELECT wirtschaftsplan.id, SUM(wirtschaftsplanitem.soll) " + "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart " + "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " + "AND wirtschaftsplanitem.buchungsart = buchungsart.id " + "AND buchungsart.art = ? " + "GROUP BY wirtschaftsplan.id";

    Map<Long, WirtschaftsplanungZeile> zeileMap = new HashMap<>();

    service.execute(sql, new Object[] { 0 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getLong(1)))
        {
          zeileMap.get(resultSet.getLong(1))
              .setPlanEinnahme(resultSet.getDouble(2));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              service.createObject(Wirtschaftsplan.class,
                  resultSet.getString(1)));
          zeile.setPlanEinnahme(resultSet.getDouble(2));
          zeileMap.put(resultSet.getLong(1), zeile);
        }
      }
      return resultSet;
    });

    service.execute(sql, new Object[] { 1 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getLong(1)))
        {
          zeileMap.get(resultSet.getLong(1))
              .setPlanAusgabe(resultSet.getDouble(2));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              service.createObject(Wirtschaftsplan.class,
                  resultSet.getString(1)));
          zeile.setPlanAusgabe(resultSet.getDouble(2));
          zeileMap.put(resultSet.getLong(1), zeile);
        }
      }
      return resultSet;
    });

    sql = "SELECT wirtschaftsplan.id, SUM(buchung.betrag) AS ist " + "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart, buchung, konto " + "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " + "AND buchung.buchungsart = buchungsart.id " + "AND buchung.konto = konto.id " + "AND buchung.datum >= wirtschaftsplan.datum_von " + "AND buchung.datum <= wirtschaftsplan.datum_bis " + "AND buchungsart.art = ? " + "AND konto.kontoart < ? " + "GROUP BY wirtschaftsplan.id";

    service.execute(sql, new Object[] { 0, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getLong(1)))
            {
              zeileMap.get(resultSet.getLong(1))
                  .setIstEinnahme(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    service.execute(sql, new Object[] { 1, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getLong(1)))
            {
              zeileMap.get(resultSet.getLong(1))
                  .setIstAusgabe(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    TablePart wirtschaftsplaene = new TablePart(
        new ArrayList<>(zeileMap.values()), new WirtschaftsplanEditAction());

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);
    DateFormatter dateFormatter = new DateFormatter(new JVDateFormatTTMMJJJJ());

    wirtschaftsplaene.addColumn("ID", "id");
    wirtschaftsplaene.addColumn("Von", "datum_von", dateFormatter);
    wirtschaftsplaene.addColumn("Bis", "datum_bis", dateFormatter);
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Soll", "planSaldo", formatter);
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Ist", "istSaldo", formatter);
    wirtschaftsplaene.addColumn("Saldo Differenz", "differenz", formatter);

    return wirtschaftsplaene;
  }

  public WirtschaftsplanungZeile getWirtschaftsplanungZeile()
  {
    if (getCurrentObject() instanceof WirtschaftsplanungZeile)
    {
      return (WirtschaftsplanungZeile) getCurrentObject();
    }
    return null;
  }

  public TreePart getEinnahmen() throws RemoteException
  {
    if (einnahmen == null)
    {
      einnahmen = generateTree(0);
    }
    else
    {
      @SuppressWarnings("rawtypes") List items = einnahmen.getItems();
      einnahmen.removeAll();
      einnahmen.setList(items);
    }
    return einnahmen;
  }

  public TreePart getAusgaben() throws RemoteException
  {
    if (ausgaben == null)
    {
      ausgaben = generateTree(1);
    }
    else
    {
      @SuppressWarnings("rawtypes") List items = ausgaben.getItems();
      ausgaben.removeAll();
      ausgaben.setList(items);
    }
    return ausgaben;
  }

  private TreePart generateTree(int art) throws RemoteException
  {
    WirtschaftsplanungZeile zeile = getWirtschaftsplanungZeile();

    if (zeile == null)
    {
      return null;
    }

    Map<Long, WirtschaftsplanungNode> nodes = new HashMap<>();

    DBService service = Einstellungen.getDBService();
    String sql = "SELECT wirtschaftsplanitem.buchungsklasse, sum(soll) " + "FROM wirtschaftsplanitem, buchungsart " + "WHERE wirtschaftsplan = ? AND wirtschaftsplanitem.buchungsart = buchungsart.id AND buchungsart.art = ? " + "GROUP BY wirtschaftsplanitem.buchungsklasse";

    service.execute(sql, new Object[] { zeile.getID(), art }, resultSet -> {
      while (resultSet.next())
      {
        DBIterator<Buchungsklasse> iterator = service.createList(
            Buchungsklasse.class);
        iterator.addFilter("id = ?", resultSet.getLong(1));
        if (!iterator.hasNext())
        {
          continue;
        }

        Buchungsklasse buchungsklasse = iterator.next();
        double soll = resultSet.getDouble(2);
        nodes.put(resultSet.getLong(1),
            new WirtschaftsplanungNode(buchungsklasse, art, zeile));
        nodes.get(resultSet.getLong(1)).setSoll(soll);
      }

      return nodes;
    });

    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sql = "SELECT buchung.buchungsklasse, sum(buchung.betrag) " + "FROM buchung, buchungsart " + "WHERE buchung.buchungsart = buchungsart.id " + "AND buchung.datum >= ? AND buchung.datum <= ? " + "AND buchungsart.art = ? " + "GROUP BY buchung.buchungsklasse";

    }
    else
    {
      sql = "SELECT buchungsart.buchungsklasse, sum(buchung.betrag) " + "FROM buchung, buchungsart " + "WHERE buchung.buchungsart = buchungsart.id " + "AND buchung.datum >= ? AND buchung.datum <= ? " + "AND buchungsart.art = ? " + "GROUP BY buchungsart.buchungsklasse";

    }
    service.execute(sql,
        new Object[] { zeile.getWirtschaftsplan().getDatumVon(),
            zeile.getWirtschaftsplan().getDatumBis(), art }, resultSet -> {
          while (resultSet.next())
          {
            DBIterator<Buchungsklasse> iterator = service.createList(
                Buchungsklasse.class);
            Long key = resultSet.getLong(1);
            iterator.addFilter("id = ?", key);
            if (!iterator.hasNext())
            {
              continue;
            }

            Buchungsklasse buchungsklasse = iterator.next();
            double ist = resultSet.getDouble(2);

            if (nodes.containsKey(key))
            {
              nodes.get(key).setIst(ist);
            }
            else if (ist != 0)
            {
              nodes.put(key,
                  new WirtschaftsplanungNode(buchungsklasse, art, zeile));
              nodes.get(key).setIst(ist);
            }
          }

          return nodes;
        });

    TreePart treePart = new TreePart(new ArrayList<>(nodes.values()),
        context -> {
          if (!(context instanceof WirtschaftsplanungNode))
          {
            return;
          }

          WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

          if (node.getType() != WirtschaftsplanungNode.Type.POSTEN)
          {
            return;
          }

          try
          {
            WirtschaftsplanungPostenDialog dialog = new WirtschaftsplanungPostenDialog(
                node.getWirtschaftsplanItem());
            WirtschaftsplanItem item = dialog.open();
            node.setWirtschaftsplanItem(item);
            node.setSoll(item.getSoll());

            WirtschaftsplanungNode parent = (WirtschaftsplanungNode) node.getParent();
            reloadSoll(parent, art);
          }
          catch (Exception e)
          {
            throw new ApplicationException(e);
          }
        });

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);
    treePart.addColumn("Buchungsklasse", "buchungsklassebezeichnung");
    treePart.addColumn("Buchungsart / Posten", "buchungsartbezeichnung_posten");
    treePart.addColumn("Soll", "soll", formatter);
    treePart.addColumn("Ist", "ist", formatter);

    return treePart;
  }

  public void setUebersicht(WirtschaftsplanUebersichtPart uebersicht)
  {
    this.uebersicht = uebersicht;
  }

  public void reloadSoll(WirtschaftsplanungNode parent, int art)
      throws RemoteException, ApplicationException
  {
    while (parent != null)
    {
      @SuppressWarnings("rawtypes") GenericIterator iterator = parent.getChildren();
      double soll = 0;
      while (iterator.hasNext())
      {
        WirtschaftsplanungNode child = (WirtschaftsplanungNode) iterator.next();
        soll += child.getSoll();
      }
      parent.setSoll(soll);

      parent = (WirtschaftsplanungNode) parent.getParent();
    }

    if (art == 0)
    {
      getEinnahmen();
    }
    else
    {
      getAusgaben();
    }

    uebersicht.updateSoll();
  }

  public void handleStore()
  {
    try
    {
      @SuppressWarnings("unchecked") List<WirtschaftsplanungNode> rootNodesEinnahmen = (List<WirtschaftsplanungNode>) einnahmen.getItems();
      @SuppressWarnings("unchecked") List<WirtschaftsplanungNode> rootNodesAusgaben = (List<WirtschaftsplanungNode>) ausgaben.getItems();

      DBService service = Einstellungen.getDBService();
      Wirtschaftsplan wirtschaftsplan = getWirtschaftsplanungZeile().getWirtschaftsplan();

      if (wirtschaftsplan.isNewObject() && rootNodesEinnahmen.stream()
          .noneMatch(
              WirtschaftsplanungNode::hasLeaf) && rootNodesAusgaben.stream()
          .noneMatch(WirtschaftsplanungNode::hasLeaf))
      {
        throw new ApplicationException(
            "Neuer Wirtschaftsplan enthält keine Planung!");
      }

      DBTransaction.starten();

      wirtschaftsplan.store();

      if (!wirtschaftsplan.isNewObject())
      {
        DBIterator<WirtschaftsplanItem> iterator = service.createList(
            WirtschaftsplanItem.class);
        iterator.addFilter("wirtschaftsplan = ?", wirtschaftsplan.getID());
        while (iterator.hasNext())
        {
          iterator.next().delete();
        }
      }

      for (WirtschaftsplanungNode rootNode : rootNodesEinnahmen)
      {
        iterateOverNodes(rootNode.getChildren(), wirtschaftsplan.getID());
      }
      for (WirtschaftsplanungNode rootNode : rootNodesAusgaben)
      {
        iterateOverNodes(rootNode.getChildren(), wirtschaftsplan.getID());
      }

      //Lösche Wirtschaftsplan, falls keine Planung hinterlegt ist
      DBIterator<WirtschaftsplanItem> iterator = service.createList(
          WirtschaftsplanItem.class);
      iterator.addFilter("wirtschaftsplan = ?", wirtschaftsplan.getID());
      if (!iterator.hasNext())
      {
        wirtschaftsplan.delete();
      }

      DBTransaction.commit();

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
  private void iterateOverNodes(GenericIterator iterator, String id)
      throws RemoteException, ApplicationException
  {
    while (iterator.hasNext())
    {
      WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator.next();
      if (currentNode.getType().equals(WirtschaftsplanungNode.Type.POSTEN))
      {
        WirtschaftsplanItem item = Einstellungen.getDBService()
            .createObject(WirtschaftsplanItem.class, null);
        WirtschaftsplanItem oldItem = currentNode.getWirtschaftsplanItem();
        item.setPosten(oldItem.getPosten());
        item.setSoll(oldItem.getSoll());
        item.setWirtschaftsplanId(id);
        WirtschaftsplanungNode parent = (WirtschaftsplanungNode) currentNode.getParent();
        item.setBuchungsartId(parent.getBuchungsart().getID());
        WirtschaftsplanungNode root = (WirtschaftsplanungNode) parent.getParent();
        item.setBuchungsklasseId(root.getBuchungsklasse().getID());
        item.store();
      }
      else
      {
        iterateOverNodes(currentNode.getChildren(), id);
      }
    }
  }

  public void starteAuswertung(String type) throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    //
    Settings settings = new Settings(this.getClass());
    //
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }

    try
    {
      fd.setFileName(new Dateiname("wirtschaftsplan", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), type).get());
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Erstellen der Datei: %s", e.getMessage()));
    }

    final String s = fd.open();

    if (s == null || s.length() == 0)
    {
      return;
    }

    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());

    List<WirtschaftsplanungNode> einnahmenList;
    List<WirtschaftsplanungNode> ausgabenList;

    try
    {
      //noinspection unchecked
      einnahmenList = (List<WirtschaftsplanungNode>) einnahmen.getItems();
      //noinspection unchecked
      ausgabenList = (List<WirtschaftsplanungNode>) ausgaben.getItems();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Erstellen der Reports: %s",
              e.getMessage()));
    }

    BackgroundTask task = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        switch (type)
        {
          case AUSWERTUNG_CSV:
            new WirtschaftsplanungCSV(einnahmenList, ausgabenList, file);
            break;
          case AUSWERTUNG_PDF:
            new WirtschaftsplanungPDF(einnahmenList, ausgabenList, file,
                getWirtschaftsplanungZeile().getWirtschaftsplan());
            break;
          default:
            GUI.getStatusBar()
                .setErrorText("Unable to create Report. Unknown format!");
            return;
        }
        GUI.getCurrentView().reload();
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
}
