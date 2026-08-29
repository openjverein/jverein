/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.Map.Entry;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.AbrechnungsmodusFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.AbrechnungslaufMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.AbrechnungslaufDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungslaufListeControl extends FilterControl
{

  private JVereinTablePart abrechnungslaufList;

  public AbrechnungslaufListeControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (abrechnungslaufList != null)
    {
      return abrechnungslaufList;
    }

    abrechnungslaufList = new AutoUpdateTablePart(getAbrechnungslaeufe(),
        new EditAction(AbrechnungslaufDetailView.class));
    abrechnungslaufList.addColumn("Nr", "nr");
    if ((Boolean) Einstellungen.getEinstellung(Property.ABRLABSCHLIESSEN))
    {
      abrechnungslaufList.addColumn("Abgeschlossen", "abgeschlossen",
          o -> (Boolean) o ? "\uD83D\uDD12" : "");
    }
    abrechnungslaufList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    abrechnungslaufList.addColumn("Modus", "modus",
        new AbrechnungsmodusFormatter(), false, Column.ALIGN_LEFT);
    abrechnungslaufList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    abrechnungslaufList.addColumn("Stichtag", "stichtag",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    abrechnungslaufList.addColumn("Eintrittsdatum", "eingabedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    abrechnungslaufList.addColumn("Austrittsdatum", "austrittsdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    abrechnungslaufList.addColumn("Zahlungsgrund", "zahlungsgrund");
    if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
    {
      abrechnungslaufList.addColumn("Zusatzbeträge", "zusatzbetraege",
          new JaNeinFormatter());
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
    {
      abrechnungslaufList.addColumn("Kursteilnehmer", "kursteilnehmer",
          new JaNeinFormatter());
    }
    abrechnungslaufList
        .setContextMenu(new AbrechnungslaufMenu(abrechnungslaufList));
    abrechnungslaufList.setAction(
        new EditAction(AbrechnungslaufDetailView.class, abrechnungslaufList));
    VorZurueckControl.setObjektListe(null, null);

    return abrechnungslaufList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (abrechnungslaufList == null)
    {
      return;
    }
    try
    {
      DBIterator<Abrechnungslauf> abrechnungslaeufe = getAbrechnungslaeufe();
      abrechnungslaufList.removeAll();
      while (abrechnungslaeufe.hasNext())
      {
        abrechnungslaufList.addItem(abrechnungslaeufe.next());
      }
      abrechnungslaufList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Abrechnungslauf> getAbrechnungslaeufe()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Abrechnungslauf> abrechnungslaeufe = service
        .createList(Abrechnungslauf.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case DATUM_VON:
          abrechnungslaeufe.addFilter("datum >= ?", value);
          break;
        case DATUM_BIS:
          abrechnungslaeufe.addFilter("datum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    abrechnungslaeufe.setOrder("ORDER BY datum DESC");
    return abrechnungslaeufe;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ABRECHNUNGSLAEUFE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ABRECHNUNGSLAEUFE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ABRECHNUNGSLAEUFE_DATEINAME, this);
  }

}
