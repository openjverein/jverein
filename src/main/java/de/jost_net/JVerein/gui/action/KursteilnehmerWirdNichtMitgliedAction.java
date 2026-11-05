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
package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.dialogs.MitgliedstypDialog;
import de.jost_net.JVerein.keys.Geschlecht;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;
import de.willuhn.jameica.system.OperationCanceledException;

public class KursteilnehmerWirdNichtMitgliedAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Kursteilnehmer)
        && !(context instanceof Kursteilnehmer[]))
    {
      throw new ApplicationException("Keinen Kursteilnehmer ausgewählt");
    }

    Kursteilnehmer[] kursteilnehmer = null;
    if (context instanceof Kursteilnehmer)
    {
      kursteilnehmer = new Kursteilnehmer[1];
      kursteilnehmer[0] = (Kursteilnehmer) context;
    }
    if (context instanceof Kursteilnehmer[])
    {
      kursteilnehmer = (Kursteilnehmer[]) context;
    }

    // Mitgliedstyp abfragen
    MitgliedstypDialog mtd = new MitgliedstypDialog(
        MitgliedstypDialog.POSITION_CENTER);
    Long mtgliedstyp = null;
    try
    {
      mtgliedstyp = mtd.open();
      if (mtgliedstyp == null)
      {
        // Dialog wurde mit Schließen Icon beendet
        return;
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception ex)
    {
      throw new ApplicationException("Fehler bei der Abfrage des Mitgliedstyp!",
          ex);
    }

    DBTransaction.starten();
    try
    {
      Mitglied m = null;
      for (Kursteilnehmer k : kursteilnehmer)
      {
        m = (Mitglied) Einstellungen.getDBService().createObject(Mitglied.class,
            null);
        m.setMitgliedstyp(mtgliedstyp);
        m.setAnrede(k.getAnrede());
        m.setBic(k.getBic());
        m.setEmail(k.getEmail());
        m.setGeburtsdatum(k.getGeburtsdatum());
        if (k.getGeschlecht() != null)
        {
          m.setGeschlecht(k.getGeschlecht());
        }
        else
        {
          m.setGeschlecht(Geschlecht.OHNEANGABE.getStringKey());
        }
        m.setIban(k.getIban());
        m.setName(k.getName());
        m.setOrt(k.getOrt());
        m.setPersonenart(k.getPersonenart());
        m.setPlz(k.getPlz());
        m.setStaat(k.getStaatCode());
        m.setStrasse(k.getStrasse());
        m.setAdressierungszusatz(k.getAdressierungszusatz());
        m.setTitel(k.getTitel());
        m.setVorname(k.getVorname());
        m.setKontoinhaber(k.getKontoinhaber());
        m.setMandatDatum(k.getMandatDatum());
        m.setZahlungsweg(Zahlungsweg.BASISLASTSCHRIFT);
        m.setMandatID("<Mandat-ID Bitte anpassen!>");
        m.store();
      }

      DBTransaction.commit();
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      throw new ApplicationException(
          "Fehler bei der Umwandlung eines Kursteilnehmers zum Nicht-Mitglied",
          e);
    }
  }
}
