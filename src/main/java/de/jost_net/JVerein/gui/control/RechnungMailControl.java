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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.io.Rechnungsausgabe;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.Differenz;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.Datum;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungMailControl extends DruckMailControl
{

  public RechnungMailControl(AbstractView view)
  {
    super(view, RechnungControl.class);
  }

  public Button getStartRechnungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveFilterSettings();
          new Rechnungsausgabe(RechnungControl.TYP.RECHNUNG, null).aufbereiten(
              getRechnungen(currentObject),
              (Ausgabeart) getAusgabeart().getValue(), getBetreffString(),
              getTxtString(), true, false, (Boolean) versand.getValue());
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Rechnung Ausgabe.", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  public Button getStartMahnungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveFilterSettings();
          new Rechnungsausgabe(RechnungControl.TYP.MAHNUNG,
              (Formular) RechnungMailControl.this.getFormular(null).getValue())
                  .aufbereiten(getRechnungen(currentObject),
                      (Ausgabeart) getAusgabeart().getValue(),
                      getBetreffString(), getTxtString(), true, false, false);
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Mahnung Ausgabe.", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  @SuppressWarnings("unchecked")
  private GenericIterator<Rechnung> getRechnungIterator()
      throws RemoteException, ApplicationException
  {
    DBIterator<Rechnung> rechnungenIt = Einstellungen.getDBService()
        .createList(Rechnung.class);

    boolean joinMitglied = false;
    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();

      switch (entry.getKey())
      {
        case VERSAND:
          switch ((SuchVersand) value)
          {
            case VERSAND:
              rechnungenIt.addFilter("rechnung.versanddatum IS NOT NULL");
              break;
            case NICHT_VERSAND:
              rechnungenIt.addFilter("rechnung.versanddatum IS NULL");
              break;
          }
          break;
        case DATUM_VON:
          rechnungenIt.addFilter("rechnung.datum >= ? ", value);
          break;
        case DATUM_BIS:
          rechnungenIt.addFilter("rechnung.datum <= ? ", value);
          break;
        case NAME:
          joinMitglied = true;
          rechnungenIt.addFilter(
              "((lower(mitglied.name) like ?) OR (lower(mitglied.vorname) like ?))",
              "%" + ((String) value).toLowerCase() + "%",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        case MAIL:
          switch ((MailAuswahl) value)
          {
            case OHNE:
              joinMitglied = true;
              rechnungenIt.addFilter(
                  "(mitglied.email is null or length(mitglied.email) = 0)");
              break;
            case MIT:
              joinMitglied = true;
              rechnungenIt.addFilter(
                  "(mitglied.email is  not null and length(mitglied.email) > 0)");
          }
          break;
        case OHNE_ABBUCHER:
          if ((boolean) value)
          {
            rechnungenIt.addFilter("rechnung.zahlungsweg <> ?",
                Zahlungsweg.BASISLASTSCHRIFT);
          }
          break;
        case DIFFERENZ:
          Double limit = (Double) getFilter().get(Filter.DIFFERENZ_LIMIT);
          if (limit == null)
          {
            limit = 0.005d;
          }
          // Es ist egal ob der Betrag positiv oder negativ eingetragen
          // wurde
          limit = Math.abs(limit);

          ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
              Sollbuchung.TABLE_NAME);
          it.addColumn(Sollbuchung.T_RECHNUNG + " as rid");
          it.addColumn("sum(cast(COALESCE(buchung.ist,0) - COALESCE("
              + Sollbuchung.T_BETRAG + ",0) AS DECIMAL(10,2))) as dif");
          it.leftJoin(
              "(SELECT sum(COALESCE((betrag),0)) AS ist,"
                  + Buchung.T_SOLLBUCHUNG + " FROM buchung GROUP BY "
                  + Buchung.T_SOLLBUCHUNG + ") AS buchung",
              Buchung.T_SOLLBUCHUNG + " = " + Sollbuchung.TABLE_NAME_ID);

          if (value == Differenz.FEHLBETRAG)
          {
            it.addHaving("dif < ?", -limit);
          }
          else
          {
            it.addHaving("dif > ?", limit);
          }
          it.addGroupBy(Sollbuchung.T_RECHNUNG);
          ArrayList<String> diffIds = new ArrayList<>();
          while (it.hasNext())
          {
            diffIds.add(String.valueOf(it.next().getAttribute("rid")));
          }
          if (diffIds.size() == 0)
          {
            return PseudoIterator.fromArray(new GenericObject[] {});
          }
          rechnungenIt
              .addFilter("rechnung.id in (" + String.join(",", diffIds) + ")");
          break;
        case DIFFERENZ_LIMIT:
          // Wird oben getestet
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    if (joinMitglied)
    {
      rechnungenIt.join("mitglied");
      rechnungenIt.addFilter("mitglied.id = rechnung.mitglied");
    }

    return rechnungenIt;
  }

  @Override
  public String getInfoText(Object selection) throws RemoteException
  {
    Rechnung[] rechnungen = null;
    String text = "";

    if (selection instanceof Rechnung)
    {
      rechnungen = new Rechnung[] { (Rechnung) selection };
    }
    else if (selection instanceof Rechnung[])
    {
      rechnungen = (Rechnung[]) selection;
    }
    else
    {
      return "";
    }

    if (rechnungen != null)
    {
      text = "Es wurden " + rechnungen.length + " Rechnungen ausgewählt";
      String fehlen = "";
      for (Rechnung re : rechnungen)
      {
        Mitglied m = re.getMitglied();
        if (m != null && (m.getEmail() == null || m.getEmail().isEmpty()))
        {
          fehlen = fehlen + "\n - " + m.getName() + ", " + m.getVorname();
        }
      }
      if (fehlen.length() > 0)
      {
        text += "\nFolgende Mitglieder haben keine Mailadresse:" + fehlen;
      }
    }
    return text;
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Rechnung> getRechnungen(Object currentObject)
      throws RemoteException, ApplicationException
  {
    if (currentObject instanceof Rechnung)
    {
      currentObject = (new Rechnung[] { (Rechnung) currentObject });
    }
    if (currentObject instanceof Rechnung[])
    {
      return new ArrayList<Rechnung>(Arrays.asList((Rechnung[]) currentObject));
    }

    ArrayList<Rechnung> rechn = (ArrayList<Rechnung>) PseudoIterator
        .asList(getRechnungIterator());

    if (rechn == null || rechn.size() == 0)
    {
      throw new ApplicationException("Keine passende Rechnung gefunden.");
    }
    return rechn;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object object, String option)
      throws RemoteException, ApplicationException
  {
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = null;
    int ohneMail = 0;
    for (Rechnung r : getRechnungen(object))
    {
      Mitglied m = r.getZahler();
      String mail = m.getEmail();
      if ((mail == null || mail.isEmpty())
          && getAusgabeart().getValue() == Ausgabeart.MAIL)
      {
        ohneMail++;
      }
      String dokument = "Rechnung " + r.getID() + " vom "
          + Datum.formatDate(r.getDatum()) + " über "
          + Einstellungen.DECIMALFORMAT.format(r.getBetrag())
          + "€ und Fehlbetrag " + Einstellungen.DECIMALFORMAT
              .format((r.getBetrag() - r.getIstSumme()))
          + "€";
      liste.add(new DruckMailEmpfaengerEntry(dokument, mail, m.getName(),
          m.getVorname(), m.getMitgliedstyp()));
    }
    if (ohneMail == 1)
    {
      text = ohneMail + " Rechnung hat keine Mail Adresse.";
    }
    else if (ohneMail > 1)
    {
      text = ohneMail + " Rechnungen haben keine Mail Adresse.";
    }
    return new DruckMailEmpfaenger(liste, text);
  }

  @Override
  public Map<Mitglied, Object> getDruckMailList()
      throws RemoteException, ApplicationException
  {
    Map<Mitglied, Object> map = new HashMap<>();
    ArrayList<Rechnung> rechnungen = getRechnungen(
        this.view.getCurrentObject());
    for (Rechnung r : rechnungen)
    {
      if (r.getZahler() != null)
      {
        map.put(r.getZahler(), r);
      }
    }
    return map;
  }

  @Override
  protected String getTableTitle()
  {
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    return null;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    return null;
  }

}
