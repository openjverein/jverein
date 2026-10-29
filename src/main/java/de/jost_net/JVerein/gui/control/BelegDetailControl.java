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

import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.DokumentShowAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.BuchungAbrechnugslaufMenu;
import de.jost_net.JVerein.gui.menu.MitgliedMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegDetailControl extends VorZurueckControl implements Savable
{
  private TextInput bemerkung;

  private FileInput datei;

  private Settings settings;

  private AutoUpdateTablePart buchungList;

  public enum Tab
  {
    BUCHUNG(0),
    MITGLIEDER(1);

    private int index;

    Tab(int i)
    {
      index = i;
    }

    public int getInt()
    {
      return index;
    }

    public static Tab getByInt(int i)
    {
      for (Tab v : Tab.values())
      {
        if (v.getInt() == i)
          return v;
      }
      return BUCHUNG;
    }
  }

  private Tab selectedTab = Tab.BUCHUNG;

  private AbstractBelegDBObject belegObject;

  private Beleg beleg;

  private DateInput datum;

  private TextInput belegnummer;

  private SelectInput belegArt;

  private JVereinTablePart mitgliederList;

  public BelegDetailControl(AbstractView view,
      AbstractBelegDBObject belegObject)
  {
    super(view);
    this.belegObject = belegObject;
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
  }

  private Beleg getBeleg()
  {
    if (beleg != null)
    {
      return beleg;
    }
    beleg = (Beleg) getCurrentObject();
    return beleg;
  }

  @Override
  public Beleg prepareStore() throws RemoteException, ApplicationException
  {
    Beleg beleg = getBeleg();
    beleg.setBemerkung((String) getBemerkung().getValue());
    if (beleg.isNewObject())
    {
      beleg.setDatum((Date) getDatum().getValue());
    }

    if (datei != null)
    {
      String filename = (String) datei.getValue();
      File file = null;
      if (filename != null && !filename.isBlank())
      {
        file = new File(filename);
        settings.setAttribute("lastdir", file.getParent());
      }
      beleg.setFile(file);
    }

    beleg.setBelegArt((BelegArt) getBelegArt().getValue());

    return beleg;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    DBTransaction.starten();
    try
    {
      Beleg beleg = prepareStore();
      beleg.store();
      if (belegObject != null)
      {
        belegObject.addBeleg(beleg);
        // belegObject wird nur hier für das hinzufügen gebraucht, damit es
        // nicht nochmal hinzugefügt wird, auf null setzen
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

  public TextInput getBemerkung() throws RemoteException
  {
    if (bemerkung != null)
    {
      return bemerkung;
    }
    bemerkung = new TextInput(getBeleg().getBemerkung(), 50);
    bemerkung.setName("Bemerkung");
    bemerkung.setMandatory(true);
    return bemerkung;
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
    String text;
    if (getBeleg().getPfad() == null || getBeleg().getPfad().isBlank())
    {
      text = "-- Das Dokument ist mit Jameica Messaging gespeichert --";
    }
    else
    {
      text = getBeleg().getRootDir() + getBeleg().getPfad();
    }
    Input pfad = new TextInput(text);
    pfad.setName("Pfad");
    pfad.disable();
    return pfad;
  }

  public Input getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    datum = new DateInput(getBeleg().getDatum());
    datum.setName("Datum");
    if (getBeleg().isNewObject())
    {
      datum.setValue(new Date());
    }
    else
    {
      datum.disable();
    }
    return datum;
  }

  public Input getBelegnummer() throws RemoteException
  {
    if (belegnummer != null)
    {
      return belegnummer;
    }
    belegnummer = new TextInput(getBeleg().getBelegnummer());
    belegnummer.setName("Belegnummer");
    belegnummer.disable();
    return belegnummer;
  }

  public Input getBelegArt() throws RemoteException
  {
    if (belegArt != null)
    {
      return belegArt;
    }

    BelegArt def = getBeleg().getBelegArt();
    if (def == null)
    {
      def = belegObject != null ? belegObject.getBelegArt()
          : BelegArt.SONSTIGES;
    }

    belegArt = new SelectInput(BelegArt.values(), def);
    belegArt.setName("Belegart");
    if (!getBeleg().isNewObject())
    {
      belegArt.disable();
    }
    return belegArt;
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
    it.join("belegbuchung");
    it.addFilter("belegbuchung.referenz = buchung.id");
    it.addFilter("belegbuchung.beleg = ?", getBeleg().getID());

    buchungList = new BuchungListTablePart(PseudoIterator.asList(it),
        new BuchungAction(false, null));
    buchungList.setTableName("Buchungen");
    buchungList.addColumn("Nr", "id-int");
    buchungList.addColumn(getAbgeschlossenColumn(), false);
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
        new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      buchungList.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
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

  public JVereinTablePart getMitgliederList() throws RemoteException
  {
    if (mitgliederList != null)
    {
      return mitgliederList;
    }
    DBIterator<Mitglied> it = Einstellungen.getDBService()
        .createList(Mitglied.class);
    it.join("belegmitglied");
    it.addFilter("belegmitglied.referenz = mitglied.id");
    it.addFilter("belegmitglied.beleg = ?", getBeleg().getID());

    mitgliederList = new JVereinTablePart(PseudoIterator.asList(it),
        new MitgliedDetailAction());
    mitgliederList.setTableName("Mitglieder");
    mitgliederList.addColumn("Nr", "id-int");
    mitgliederList.addColumn("Name", "name");
    mitgliederList.addColumn("Vorname", "vorname");
    mitgliederList.addColumn("Straße", "strasse");
    mitgliederList.addColumn("Ort", "ort");

    mitgliederList.setMulti(true);

    mitgliederList.setContextMenu(
        new MitgliedMenu(new MitgliedDetailAction(), mitgliederList));
    return mitgliederList;
  }

  public void setFolderSelection(Tab selection)
  {
    selectedTab = selection;
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    switch (selectedTab)
    {
      case BUCHUNG:
        return getBuchungList();
      case MITGLIEDER:
        return getMitgliederList();
    }
    return null;
  }

  @Override
  protected String getTableTitle()
  {
    switch (selectedTab)
    {
      case BUCHUNG:
        return VorlageUtil.getName(VorlageTyp.BELEG_BUCHUNGEN_TITEL,
            getBeleg());
      case MITGLIEDER:
        return VorlageUtil.getName(VorlageTyp.BELEG_MITGLIEDER_TITEL,
            getBeleg());
    }
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    switch (selectedTab)
    {
      case BUCHUNG:
        return VorlageUtil.getName(VorlageTyp.BELEG_BUCHUNGEN_SUBTITEL,
            getBeleg());
      case MITGLIEDER:
        return VorlageUtil.getName(VorlageTyp.BELEG_MITGLIEDER_SUBTITEL,
            getBeleg());
    }
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    switch (selectedTab)
    {
      case BUCHUNG:
        return VorlageUtil.getName(VorlageTyp.BELEG_BUCHUNGEN_DATEINAME,
            getBeleg());
      case MITGLIEDER:
        return VorlageUtil.getName(VorlageTyp.BELEG_MITGLIEDER_DATEINAME,
            getBeleg());
    }
    return null;

  }

  public PanelButton getDetailSpaltenPanelButton()
  {
    return new PanelButton("document-properties.png", context -> {
      try
      {
        new TabelleSpaltenAuswahlDialog(getBuchungList()).open();
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Spalten-Auswahl-Dialog");
      }
    }, "Spalten auswählen");
  }

  public ButtonRtoL getAnzeigenButton()
  {
    return new ButtonRtoL("Anzeigen", new DokumentShowAction(),
        getCurrentObject(), false, "eye.png");
  }
}
