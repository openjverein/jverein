/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.BelegMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.BelegDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegListControl extends FilterControl
{
  private JVereinTablePart docsList;

  public BelegListControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (docsList != null)
    {
      return docsList;
    }
    docsList = new AutoUpdateTablePart(getList(), null);
    docsList.setTableName("Dokumente");
    docsList.addColumn("Belegnummer", "belegnummer");
    docsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    docsList.addColumn("Bemerkung", "bemerkung");
    docsList.addColumn("Pfad", "vollpfad");
    docsList.setContextMenu(new BelegMenu(docsList));
    docsList.setMulti(true);
    // Nur in der View, nicht im Dialog
    if (this.view != null)
    {
      docsList.setAction(new EditAction(BelegDetailView.class, docsList));
      VorZurueckControl.setObjektListe(null, null);
    }
    return docsList;
  }

  /**
   * Erstellt einen Neuen Beleg mit der angegebenen Datei. Datum und Bemerkung
   * werden automatisch bestimmt.
   * 
   * @param filename
   */
  public void addFile(String filename)
  {
    try
    {
      DBTransaction.starten();

      BuchungDokument document = Einstellungen.getDBService()
          .createObject(BuchungDokument.class, null);
      File file = new File(filename);

      document.setBemerkung(file.getName());
      document.setDatum(new Date());

      document.setFile(file);
      document.store();

      getTablePart().addItem(document);

      DBTransaction.commit();
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar()
          .setErrorText("Fehler beim Hinzufügen der Datei:" + e.getMessage());
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  public DBIterator<BuchungDokument> getList()
      throws RemoteException, ApplicationException
  {
    DBIterator<BuchungDokument> docs = Einstellungen.getDBService()
        .createList(BuchungDokument.class);
    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NUMMER:
          docs.addFilter("(lower(belegnummer) like ?)",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        case BEZEICHNUNG:
          docs.addFilter("(lower(bemerkung) like ?)",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        case NICHT_ZUGEORDNET:
          if ((boolean) value)
          {
            docs.addFilter("NOT EXISTS "
                + "(SELECT * FROM buchungsdokumentbuchung "
                + "WHERE buchungsdokumentbuchung.dokument = buchungdokument.id)");
          }
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    docs.setOrder("ORDER BY datum desc");
    return docs;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      docsList.removeAll();
      DBIterator<BuchungDokument> it = getList();
      while (it.hasNext())
      {
        docsList.addItem(it.next());
      }
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler bei Datenbankzugriff", e1);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BELEG_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BELEG_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BELEG_DATEINAME);
  }
}
