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

import java.rmi.RemoteException;
import org.eclipse.swt.widgets.TableItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.NotizFormatter;
import de.jost_net.JVerein.gui.menu.BeitragsgruppeMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.BeitragsgruppeDetailView;
import de.jost_net.JVerein.io.AltersgruppenParser;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeitragsgruppeControl extends FilterControl
{

  private JVereinTablePart beitragsgruppeList;

  public BeitragsgruppeControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (beitragsgruppeList != null)
    {
      return beitragsgruppeList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Beitragsgruppe> beitragsgruppen = service
        .createList(Beitragsgruppe.class);
    beitragsgruppeList = new JVereinTablePart(beitragsgruppen, null);
    beitragsgruppeList.addColumn(getGesperrtColumn(), false);
    beitragsgruppeList.addColumn("Bezeichnung", "bezeichnung");
    switch (Beitragsmodel.getByKey(
        (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)))
    {
      case GLEICHERTERMINFUERALLE:
      case MONATLICH12631:
        beitragsgruppeList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
        break;
      case FLEXIBEL:
        beitragsgruppeList.addColumn("Betrag monatlich", "betragmonatlich",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag viertelj.",
            "betragvierteljaehrlich",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag halbj.", "betraghalbjaehrlich",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag jährlich", "betragjaehrlich",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
        break;
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
    {
      beitragsgruppeList.addColumn("Arbeitseinsatz-Stunden",
          "arbeitseinsatzstunden",
          new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
      beitragsgruppeList.addColumn("Arbeitseinsatz-Stundensatz",
          "arbeitseinsatzbetrag",
          new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
    }
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      beitragsgruppeList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    beitragsgruppeList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      beitragsgruppeList.addColumn("Steuer", "steuer", o -> {
        if (o == null)
        {
          return "";
        }
        try
        {
          return ((Steuer) o).getName();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return "";
      }, false, Column.ALIGN_RIGHT);
    }
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) != Beitragsmodel.FLEXIBEL.getKey()
        && (Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
    {
      beitragsgruppeList.addColumn("Altersstaffel", "altersstaffel",
          new JaNeinFormatter());
    }
    if ((Boolean) Einstellungen
        .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN))
    {
      beitragsgruppeList.addColumn("Sekundär", "sekundaer",
          new JaNeinFormatter());
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.FAMILIENBEITRAG))
    {
      beitragsgruppeList.addColumn("Beitragsart", "beitragsart", new Formatter()
      {

        @Override
        public String format(Object o)
        {
          return ArtBeitragsart.getByKey((Integer) o).getText();
        }
      });
    }
    beitragsgruppeList.addColumn("Notiz", "notiz", new NotizFormatter(40));
    beitragsgruppeList
        .setContextMenu(new BeitragsgruppeMenu(beitragsgruppeList));
    beitragsgruppeList.setFormatter(new TableFormatter()
    {
      @Override
      public void format(TableItem item)
      {
        Beitragsgruppe b = (Beitragsgruppe) item.getData();
        try
        {
          if (b.getHasAltersstaffel())
          {
            AltersgruppenParser ap = new AltersgruppenParser(
                (String) Einstellungen
                    .getEinstellung(Property.BEITRAGALTERSSTUFEN));
            String text = "";
            DBIterator<Altersstaffel> it = b.getAltersstaffelIterator();
            while (it.hasNext() && ap.hasNext())
            {
              ap.getNext();
              Altersstaffel a = it.next();
              text = text + "|"
                  + Einstellungen.BIGDECIMALFORMAT.format(a.getBetrag());
            }
            item.setText(1, text.substring(1));
          }
        }
        catch (RemoteException e)
        {
          Logger.error("unable to format line", e);
        }
      }
    });
    beitragsgruppeList.setMulti(true);
    beitragsgruppeList.setAction(
        new EditAction(BeitragsgruppeDetailView.class, beitragsgruppeList));
    VorZurueckControl.setObjektListe(null, null);
    return beitragsgruppeList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BEITRAGSGRUPPEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BEITRAGSGRUPPEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BEITRAGSGRUPPEN_DATEINAME);
  }

}
