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
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.BuchungAbrechnugslaufMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.IBeleg;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegControl extends VorZurueckControl implements Savable
{
  private TextInput bezeichnung;

  private FileInput datei;

  private Settings settings;

  private AutoUpdateTablePart buchungList;

  public final static int TAB_BUCHUNGEN = 0;

  private int selectedTab = TAB_BUCHUNGEN;

  private IBeleg belegObject;

  private BuchungDokument beleg;

  public BelegControl(AbstractView view, IBeleg belegObject)
  {
    super(view);
    this.belegObject = belegObject;
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
  }

  private BuchungDokument getBeleg() throws RemoteException
  {
    if (beleg != null)
    {
      return beleg;
    }
    beleg = (BuchungDokument) getCurrentObject();
    return beleg;
  }

  @Override
  public BuchungDokument prepareStore()
      throws RemoteException, ApplicationException
  {
    BuchungDokument beleg = getBeleg();
    beleg.setBemerkung((String) getBezeichnung().getValue());

    if (datei != null)
    {
      File file = new File((String) datei.getValue());
      settings.setAttribute("lastdir", file.getParent());
      beleg.setFile(file);
    }
    return beleg;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    DBTransaction.starten();
    try
    {
      BuchungDokument beleg = prepareStore();
      beleg.store();
      if (belegObject != null)
      {
        belegObject.addBeleg(beleg);
        // belegObject wird nur hier für das hinzufügen gebraucht, damit das
        // nich nochmal passiert, auf null setzen
        belegObject = null;
      }
      DBTransaction.commit();
    }
    catch (RemoteException e)
    {
      DBTransaction.rollback();
      String fehler = "Fehler bei speichern des Belegs";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getBeleg().getBemerkung(), 50);
    bezeichnung.setName("Bezeichnung");
    return bezeichnung;
  }

  public Input getDatei()
  {
    if (datei != null)
    {
      return datei;
    }
    datei = new FileInput("", false)
    {
      @Override
      protected void customize(FileDialog fd)
      {
        fd.setFilterPath(settings.getString("lastdir", ""));
      }
    };
    datei.setMandatory(true);
    datei.setName("Datei");
    return datei;
  }

  public Input getPfad() throws RemoteException
  {
    Input pfad = new TextInput(getBeleg().getRootDir() + getBeleg().getPfad());
    pfad.disable();
    return pfad;
  }

  @SuppressWarnings("unchecked")
  public JVereinTablePart getBuchungList()
      throws RemoteException, ApplicationException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.join("buchungsdokumentbuchung");
    it.addFilter("buchungsdokumentbuchung.buchung = buchung.id");
    it.addFilter("buchungsdokumentbuchung.dokument = ?", getBeleg().getID());

    buchungList = new BuchungListTablePart(PseudoIterator.asList(it),
        new BuchungAction(false, null));
    buchungList.setTableName("Buchungen");
    buchungList.addColumn("Nr", "id-int");
    buchungList.addColumn("Geprüft", "geprueft",
        o -> (Boolean) o ? "\u2705" : "");
    if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG))
    {
      buchungList.addColumn("D", "document");
    }
    buchungList.addColumn("S", "splittyp",
        o -> SplitbuchungTyp.get((Integer) o).substring(0, 1));

    buchungList.addColumn("Konto", "konto");
    buchungList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    buchungList.addColumn("Name", "name");
    buchungList.addColumn("IBAN oder Kontonummer", "iban", new IBANFormatter());
    buchungList.addColumn("Verwendungszweck", "zweck", o -> {
      if (o == null)
      {
        return null;
      }
      String s = o.toString();
      s = s.replaceAll("\r\n", " ");
      s = s.replaceAll("\r", " ");
      s = s.replaceAll("\n", " ");
      return s;
    });
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      buchungList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }

    buchungList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    buchungList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      buchungList.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        buchungList.addColumn("Steuer", "steuer.name", null, false,
            Column.ALIGN_RIGHT);
      }
    }
    buchungList.addColumn(new Column(Buchung.SOLLBUCHUNG,
        "Mitglied - Sollbuchung", new SollbuchungFormatter(), false,
        Column.ALIGN_AUTO, Column.SORT_BY_DISPLAY));
    buchungList.setMulti(true);

    buchungList.setContextMenu(new BuchungAbrechnugslaufMenu());
    return buchungList;
  }

  public void setFolderSelection(int selection)
  {
    selectedTab = selection;
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return getBuchungList();
      default:
        return null;
    }
  }

  @Override
  protected String getTableTitle()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_TITEL, this);
      default:
        return null;
    }
  }

  @Override
  protected String getTableSubtitle()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_SUBTITEL, this);
      default:
        return null;
    }
  }

  @Override
  protected String getTableDateiname()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_DATEINAME, this);
      default:
        return null;
    }
  }
}
