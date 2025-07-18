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

package de.jost_net.JVerein.gui.dialogs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, ueber den Daten importiert werden koennen.
 */
public class BuchungenSollbuchungZuordnungDialog extends AbstractDialog<Object>
{
  private static final String SETTINGS_PREFIX = "BUCHUNGSZUORDNUNG.";
  private static final String SETTINGS_NAME_IBAN = SETTINGS_PREFIX + "IBAN";
  private static final String SETTINGS_NAME_MITGLIEDSNUMMER = SETTINGS_PREFIX + "MITGLIEDSNUMMER";
  private static final String SETTINGS_NAME_VORNAME_NAME = SETTINGS_PREFIX + "VORNAME_NAME";

  private static final String SETTINGS_ZWECK = SETTINGS_PREFIX + "ZWECK";

  private static final int WINDOW_WIDTH = 620;

  private DateInput     dateFrom = null;
  private DateInput     dateUntil = null;
  private CheckboxInput useIban = null;
  private CheckboxInput useMemberNumber = null;
  private CheckboxInput useName = null;

  private CheckboxInput useZweck;

  private Settings settings = null;

  /**
   * ct.
   * @param bisdatum
   * @param vondatum
   * 
   * @throws RemoteException
   */
  public BuchungenSollbuchungZuordnungDialog(Date vondatum, Date bisdatum)
  {
    super(POSITION_CENTER);

    setTitle("Buchungen zu Sollbuchung zuordnen");
    setSize(WINDOW_WIDTH, SWT.DEFAULT);

    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);

    //Inputs
    dateFrom = createDateInput(vondatum, true);
    dateUntil = createDateInput(bisdatum, false);
    useIban = new CheckboxInput(settings.getBoolean(SETTINGS_NAME_IBAN, true));
    useMemberNumber = new CheckboxInput(settings.getBoolean(SETTINGS_NAME_MITGLIEDSNUMMER, false));
    useName = new CheckboxInput(settings.getBoolean(SETTINGS_NAME_VORNAME_NAME, false));
    useZweck = new CheckboxInput(settings.getBoolean(SETTINGS_ZWECK, false));
  }

  private DateInput createDateInput(Date date, boolean isStart)
  {
    DateInput returnValue = new DateInput(date, new JVDateFormatTTMMJJJJ());
    String typeOfInput = isStart ? "Be�ginn" : "Ende";
    returnValue.setTitle(typeOfInput +  " des Suchbereichs");
    returnValue.setText("Bitte "+ typeOfInput +" des Suchbereichs w�hlen");
    returnValue.setComment("*)");
    return returnValue;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText("Bitte w�hlen Sie den Suchzeitraum und die gew�nschte Zuordnungsart aus", true);

    group.addLabelPair("Startdatum", dateFrom);
    group.addLabelPair("Enddatum", dateUntil);
    group.addLabelPair("Nach eindeutiger IBAN", useIban);
    group.addLabelPair("Nach " + ((Boolean) Einstellungen
        .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER)
            ? "Ext. Mitgliedsnummer"
            : "Mitgliedsnummer"),
        useMemberNumber);
    group.addLabelPair("Nach eindeutigen Vorname und Nachname", useName);
    group.addLabelPair("Nach eindeutigem Verwendungszweck", useZweck);
    ButtonArea buttons = new ButtonArea();

    Button button = new Button("Zuordnungen suchen", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        doSearchAssignment();
      }
    }, null, true, "user-friends.png");
    buttons.addButton(button);
    buttons.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");
    group.addButtonArea(buttons);
    getShell()
        .setMinimumSize(getShell().computeSize(WINDOW_WIDTH, SWT.DEFAULT));
  }

  /**
   * Ermittelt eine Liste von Buchungen mit Mitgliedskonten und gibt
   * diese an den BuchungenMitgliedskontenZuordnungVorschauDialog weiter
   * 
   */
  private void doSearchAssignment()
  {
    final Date dateFromInput = (Date)dateFrom.getValue();
    final Date dateUntilInput = (Date)dateUntil.getValue();

    final boolean useIbanInput = Boolean.TRUE.equals(this.useIban.getValue());
    final boolean useMemberNumberInput = Boolean.TRUE.equals(this.useMemberNumber.getValue());
    final boolean useNameInput = Boolean.TRUE.equals(this.useName.getValue());
    final boolean useZweckInput = Boolean.TRUE.equals(this.useZweck.getValue());

    settings.setAttribute(SETTINGS_NAME_IBAN, useIbanInput);
    settings.setAttribute(SETTINGS_NAME_MITGLIEDSNUMMER, useMemberNumberInput);
    settings.setAttribute(SETTINGS_NAME_VORNAME_NAME, useNameInput);
    settings.setAttribute(SETTINGS_ZWECK, useZweckInput);

    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try 
        {
          boolean externeMitgliedsnummer = Boolean.TRUE.equals((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER));

          if (!useIbanInput && !useMemberNumberInput && !useNameInput
              && !useZweckInput)
          {
            GUI.getStatusBar().setErrorText("Es wurde keine Zuordnungsart angegeben.");
            return;
          }

          if(dateFromInput == null || dateUntilInput == null)
          {
            GUI.getStatusBar().setErrorText("Bitte geben Sie ein Start- und Enddatum ein.");
            return;
          }

          if(dateUntilInput.before(dateFromInput))
          {
            GUI.getStatusBar().setErrorText("Das Enddatum liegt vor dem Startdatum.");
            return;
          }

          Map<String, String> uniqueIbans = new HashMap<>();
          Set<String> duplicateIbans = new HashSet<>();

          Map<String, String> uniqueIds = new HashMap<>();
          Set<String> duplicateIds = new HashSet<>();

          Map<String, String> uniqueNames = new HashMap<>();
          Set<String> duplicateNames = new HashSet<>();

          Map<String, String> uniqueZweck = new HashMap<>();

          if(useIbanInput || useMemberNumberInput || useNameInput)
          {
            DBIterator<Mitglied> mitglieder = Einstellungen.getDBService().createList(Mitglied.class);

            while(mitglieder.hasNext())
            {
              Mitglied mitglied = mitglieder.next();

              if(useIbanInput)
              {
                processUniqueEntry(mitglied.getIban(), mitglied.getID(), uniqueIbans, duplicateIbans);
              }

              if(useMemberNumberInput)
              {
                if(externeMitgliedsnummer)
                {
                  processUniqueEntry(mitglied.getExterneMitgliedsnummer(), mitglied.getID(), uniqueIds, duplicateIds);
                }
                else
                {
                  uniqueIds.put(mitglied.getID(), mitglied.getID());
                }
              }

              if(useNameInput)
              {
                processUniqueEntry(concatName(mitglied), mitglied.getID(), uniqueNames, duplicateNames);
              }
            }
          }
          if (useZweckInput)
          {
            ResultSetExtractor rs = new ResultSetExtractor()
            {
              @Override
              public Object extract(ResultSet rs) throws SQLException, RemoteException
              {
                while (rs.next())
                {
                  // 1 = MitgliedID, 2 = Zweck1
                  uniqueZweck.put(rs.getString(2), rs.getString(1));
                }
                return new Object();
              }
            };

            Einstellungen.getDBService().execute(
                "SELECT " + Sollbuchung.MITGLIED + ", " + Sollbuchung.ZWECK1
                    + " FROM " + Sollbuchung.TABLE_NAME
                    + " WHERE " + Sollbuchung.MITGLIED + " IS NOT NULL"
                    + " GROUP BY " + Sollbuchung.ZWECK1 + " HAVING COUNT("
                    + Sollbuchung.ZWECK1 + ") = 1",
                new Object[] {}, rs);
          }
          duplicateIbans.clear();
          duplicateIds.clear();
          duplicateNames.clear();

          if (uniqueIbans.isEmpty() && uniqueIds.isEmpty()
              && uniqueNames.isEmpty() && uniqueZweck.isEmpty())
          {
            GUI.getStatusBar().setErrorText("Es wurden keine eindeutigen Mitglieder zum Zuordnen in den gew�hlten Zeitraum gefunden.");
            return;
          }

          DBIterator<Buchung> buchungen = Einstellungen.getDBService().createList(Buchung.class);
          buchungen.addFilter("datum >= ?", dateFromInput);
          buchungen.addFilter("datum <= ?", dateUntilInput);
          buchungen.addFilter("(splittyp != ? or splittyp is null)",
              SplitbuchungTyp.HAUPT);
          buchungen.addFilter("(splittyp != ? or splittyp is null)",
              SplitbuchungTyp.GEGEN);
          buchungen.addFilter(Buchung.SOLLBUCHUNG + " is null");
          buchungen.setOrder("ORDER BY datum");

          List<BookingMemberAccountEntry> assignedBooking = new ArrayList<>();
          Set<String> usedMemberAccount = new HashSet<>();

          while(buchungen.hasNext()) 
          {
            Buchung buchung = buchungen.next();

            String bookingPurpose = getBookingPurpose(buchung);
            if(assginMemberAccountToBooking(assignedBooking,  usedMemberAccount, dateFromInput, dateUntilInput, buchung, uniqueIbans.get(buchung.getIban()), "IBAN") ||
                assginMemberAccountToBooking(assignedBooking, usedMemberAccount, dateFromInput, dateUntilInput, buchung, uniqueIds.get(bookingPurpose), externeMitgliedsnummer ? "Ext. Mitgliedsnummer" : "Mitgliedsnummer") ||
                assginMemberAccountToBooking(assignedBooking, usedMemberAccount,
                    dateFromInput, dateUntilInput, buchung,
                    uniqueNames.get(bookingPurpose), "Vorname und Nachname")
                || assginMemberAccountToBooking(assignedBooking,
                    usedMemberAccount, dateFromInput, dateUntilInput, buchung,
                    uniqueNames.get(buchung.getName()), "Vorname und Nachname")
                || assginMemberAccountToBooking(assignedBooking,
                    usedMemberAccount, dateFromInput, dateUntilInput, buchung,
                    uniqueZweck.get(bookingPurpose), "Verwendungszweck"))
            {
              continue;
            }
          }

          if(assignedBooking.isEmpty())
          {
            GUI.getStatusBar().setErrorText("Es wurden keine passenden Buchungen oder Sollbuchungen zum Zuordnen gefunden.");
          }
          else
          {
            BuchungenSollbuchungZuordnungVorschauDialog userValidationDialog = new BuchungenSollbuchungZuordnungVorschauDialog(assignedBooking);
            userValidationDialog.open();
          }
        } 
        catch (RemoteException e)
        {
          Logger.error("error while saving import file", e);
          throw new ApplicationException("Fehler bei der Zuordnungssuche", e);
        } 
        catch (Exception e)
        {
          Logger.error("error while opening a dialog", e);
          throw new ApplicationException("Fehler bei der Durchf�hrung der Zuordnung (Best�tigungsdialog)", e);
        }
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };

    Application.getController().start(t);

    close();
  }

  private String getBookingPurpose(Buchung buchung) throws RemoteException {
    String zweck = buchung.getZweck();
    if(zweck == null) return null;
    zweck = zweck.replaceAll("\r\n", " ").replaceAll("\r", " ").replaceAll("\n", " ").toUpperCase();
    // manche Banken haengen hier noch zusaetzliche Felder ran: diese versuchen wir Abzuschneiden
    if(zweck.contains(" IBAN:")) {
      zweck = zweck.substring(0, zweck.indexOf(" IBAN:"));
    }
    return zweck;
  }

  private boolean assginMemberAccountToBooking(final List<BookingMemberAccountEntry> assignedBooking, final Set<String> usedMemberAccount, Date dateFromInput, Date dateUntilInput, final Buchung buchung, String mitgliedsId, final String zuordnungsart)
      throws RemoteException 
  {
    boolean processed = false;

    if(mitgliedsId != null)
    {
      //wir wollen das nicht nochmal mit der Buchung probieren, da dies das gleiche Ergebnis w�re
      processed = true;

      ResultSetExtractor rs = new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs) throws SQLException, RemoteException
        {
          while (rs.next())
          {
            long sollbuchungId = rs.getLong(1);

            DBIterator<Sollbuchung> sollbIt = Einstellungen.getDBService().createList(Sollbuchung.class);
            sollbIt.addFilter("id = ?", sollbuchungId);

            while(sollbIt.hasNext()) 
            {
              Sollbuchung sollb = sollbIt.next();

              if(!usedMemberAccount.contains(sollb.getID())) 
              {
                BigDecimal ist = convertDoubleToBigDecimal(sollb.getIstSumme());
                BigDecimal soll = convertDoubleToBigDecimal(sollb.getBetrag());

                if(soll.subtract(ist).equals(convertDoubleToBigDecimal(buchung.getBetrag()))) {
                  assignedBooking.add(new BookingMemberAccountEntry(buchung, sollb, zuordnungsart));
                  usedMemberAccount.add(sollb.getID());
                  return new Object();
                }
              }
            }
          }
          return new Object();
        }
      };

      String sql = "SELECT " + Sollbuchung.TABLE_NAME_ID + " FROM "
          + Sollbuchung.TABLE_NAME
          + " inner join abrechnungslauf as a on a.id = " + Sollbuchung.T_ABRECHNUNGSLAUF
          + " WHERE a.stichtag >= ?"
          + " AND a.stichtag <= ?"
          + " AND " + Sollbuchung.T_MITGLIED + " = ?" + " AND "
          + Sollbuchung.T_ZAHLUNGSWEG + " = ?"
          + " GROUP BY " + Sollbuchung.TABLE_NAME_ID
          + " ORDER BY a.stichtag, " + Sollbuchung.TABLE_NAME_ID;
      Einstellungen.getDBService().execute(sql, new Object[] { dateFromInput, dateUntilInput, mitgliedsId, Zahlungsweg.�BERWEISUNG}, rs);
    }

    return processed;
  }

  private BigDecimal convertDoubleToBigDecimal(Double doubleValue)
  {
    BigDecimal value = BigDecimal.valueOf(doubleValue);
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  private String concatName(Mitglied mitglied) throws RemoteException
  {
    if(mitglied.getVorname() == null || mitglied.getVorname().length() == 0) 
    {
      return mitglied.getName() == null ? "" : mitglied.getName().toUpperCase();
    }
    else
    {
      if(mitglied.getName() == null || mitglied.getName().length() == 0) 
      {
        return mitglied.getVorname() == null ? "" : mitglied.getVorname().toUpperCase();
      }
      else
      {
        return mitglied.getVorname().toUpperCase() + " " + mitglied.getName().toUpperCase();
      }
    }
  }

  private void processUniqueEntry(String key, String value, Map<String, String> uniqueKeys, Set<String> duplicateKeys)
  {
    if(key != null && key.length() > 0) 
    {
      if(uniqueKeys.containsKey(key))
      {
        uniqueKeys.remove(key);
        duplicateKeys.add(key);
      }
      else
      {
        if(!duplicateKeys.contains(key))
        {
          uniqueKeys.put(key, value);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

  protected class BookingMemberAccountEntry implements GenericObject
  {
    public static final String PREFIX_BUCHUNG = "buchung-";
    public static final String PREFIX_MITGLIEDSKONTO = "mitgliedskonto-";

    private Buchung buchung;
    private Sollbuchung sollbuchung;
    private String zuordnungsart;
    
    BookingMemberAccountEntry(Buchung buchung, Sollbuchung sollbuchung, String zuordnungsart) 
    {
      this.buchung = buchung;
      this.sollbuchung = sollbuchung;
      this.zuordnungsart = zuordnungsart;
    }

    public Buchung getBuchung()
    {
      return buchung;
    }
    
    public Sollbuchung getSollbuchung()
    {
      return sollbuchung;
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException 
    {
      if(arg0 instanceof BookingMemberAccountEntry)
      {
        BookingMemberAccountEntry otherValue = (BookingMemberAccountEntry) arg0;
        return otherValue.getBuchung().equals(buchung) && otherValue.getSollbuchung().equals(sollbuchung);
      }
      return false; 
    }

    @Override
    public Object getAttribute(String arg0) throws RemoteException
    {
      if(arg0 == null) return null;

      if(arg0.startsWith(PREFIX_BUCHUNG))
      {
        return buchung.getAttribute(arg0.substring(PREFIX_BUCHUNG.length()));
      }

      if(arg0.startsWith(PREFIX_MITGLIEDSKONTO))
      {
        return sollbuchung.getAttribute(arg0.substring(PREFIX_MITGLIEDSKONTO.length()));
      }

      if(arg0.equals("id"))
      {
        return getID();
      }

      if(arg0.equals("zuordnungsart"))
      {
        return zuordnungsart;
      }
      return null;
    }

    @Override
    public String[] getAttributeNames() throws RemoteException {
      List<String> attributeNames = new ArrayList<>();
      attributeNames.add("id");
      attributeNames.add("zuordnungsart");

      for(String dao : buchung.getAttributeNames()) {
        attributeNames.add(PREFIX_BUCHUNG + dao);
      }

      for(String dao : sollbuchung.getAttributeNames()) {
        attributeNames.add(PREFIX_MITGLIEDSKONTO + dao);
      }

      return attributeNames.toArray(new String[0]);
    }

    @Override
    public String getID() throws RemoteException {
      return buchung.getID() +"."+ sollbuchung.getID();
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException {
      return "id";
    }
  }
}
