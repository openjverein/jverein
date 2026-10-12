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
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragControl extends VorZurueckControl implements Savable
{
  private Zusatzbetrag zuab;

  private ZusatzbetragPart part;

  private SelectInput vorlage = null;

  public static final String NEIN = "nein";

  public static final String MITDATUM = "ja, mit Datum";

  public static final String OHNEDATUM = "ja, ohne Datum";

  public ZusatzbetragControl(AbstractView view)
  {
    super(view);
  }

  public Zusatzbetrag getZusatzbetrag()
  {
    if (zuab != null)
    {
      return zuab;
    }
    zuab = (Zusatzbetrag) getCurrentObject();
    return zuab;
  }

  public ZusatzbetragPart getZusatzbetragPart()
  {
    if (part != null)
    {
      return part;
    }
    part = new ZusatzbetragPart(getZusatzbetrag(), true);
    return part;
  }

  public SelectInput getVorlage()
  {
    if (vorlage != null)
    {
      return vorlage;
    }
    vorlage = new SelectInput(new Object[] { NEIN, MITDATUM, OHNEDATUM }, NEIN);
    return vorlage;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Zusatzbetrag z = getZusatzbetrag();
    z.setFaelligkeit((Date) getZusatzbetragPart().getFaelligkeit().getValue());
    z.setStartdatum(
        (Date) getZusatzbetragPart().getStartdatum(false).getValue());
    IntervallZusatzzahlung iz = (IntervallZusatzzahlung) getZusatzbetragPart()
        .getIntervall().getValue();
    z.setIntervall(iz.getKey());
    z.setEndedatum((Date) getZusatzbetragPart().getEndedatum().getValue());
    z.setBuchungstext(
        (String) getZusatzbetragPart().getBuchungstext().getValue());
    z.setBuchungsart(
        (Buchungsart) getZusatzbetragPart().getBuchungsart().getValue());
    z.setBuchungsklasseId(getZusatzbetragPart().getSelectedBuchungsKlasseId());
    z.setBetrag((Double) getZusatzbetragPart().getBetrag().getValue());
    z.setZahlungsweg(
        (Zahlungsweg) getZusatzbetragPart().getZahlungsweg().getValue());
    if (getZusatzbetragPart().isSteuerActive())
    {
      z.setSteuer((Steuer) getZusatzbetragPart().getSteuer().getValue());
    }
    if (z.isNewObject())
    {
      if (getZusatzbetragPart().getMitglied().getValue() != null)
      {
        Mitglied m = (Mitglied) getZusatzbetragPart().getMitglied().getValue();
        z.setMitglied(Integer.parseInt(m.getID()));
      }
      else
      {
        z.setMitglied(null);
      }
    }
    z.setMitgliedzahltSelbst(
        (Boolean) getZusatzbetragPart().getMitgliedzahltSelbst().getValue());
    return z;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Zusatzbetrag z = (Zusatzbetrag) prepareStore();
      z.store();
      if (getVorlage().getValue().equals(MITDATUM)
          || getVorlage().getValue().equals(OHNEDATUM))
      {
        ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
            .getDBService().createObject(ZusatzbetragVorlage.class, null);
        zv.setIntervall(z.getIntervall());
        zv.setBuchungstext(z.getBuchungstext());
        zv.setBetrag(z.getBetrag());
        if (getVorlage().getValue().equals(MITDATUM))
        {
          zv.setEndedatum(z.getEndedatum());
          zv.setFaelligkeit(z.getFaelligkeit());
          zv.setStartdatum(z.getStartdatum());
        }
        zv.setBuchungsart(z.getBuchungsart());
        zv.setBuchungsklasseId(z.getBuchungsklasseId());
        zv.setZahlungsweg(z.getZahlungsweg());
        if (getZusatzbetragPart().isSteuerActive())
        {
          zv.setSteuer(z.getSteuer());
        }
        zv.setMitgliedzahltSelbst(z.getMitgliedzahltSelbst());
        zv.store();
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Zusatzbetrages";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
