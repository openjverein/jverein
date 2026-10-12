/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
import java.util.List;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.io.Kontoauszug;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KontoauszugMailControl extends DruckMailControl
{

  public KontoauszugMailControl(AbstractView view)
  {
    super(view, SollbuchungControl.class);
  }

  public Button getStartKontoauszugButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveFilterSettings();
          new Kontoauszug(KontoauszugMailControl.this).aufbereiten(
              getMitglieder(currentObject),
              (Ausgabeart) getAusgabeart().getValue(), getBetreffString(),
              getTxtString(), false, false, false);
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (OperationCanceledException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Kontoauszug Ausgabe.", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private ArrayList<Mitglied> getMitglieder(Object object)
      throws RemoteException, ApplicationException
  {
    ArrayList<Mitglied> mitglieder = new ArrayList<>();
    if (object == null)
    {
      mitglieder = new MitgliedQuery(this).get(MitgliedAuswahl.ALLE, null);
      if (mitglieder.size() == 0)
      {
        throw new ApplicationException(
            "Für die gewählten Filterkriterien wurden keine Mitglieder gefunden.");
      }
    }
    else if (object != null && object instanceof Mitglied)
    {
      mitglieder.add((Mitglied) object);
    }
    else if (object != null && object instanceof Mitglied[])
    {
      mitglieder = new ArrayList<>(Arrays.asList((Mitglied[]) object));
    }
    else
    {
      throw new ApplicationException("Kein Mitglied ausgewählt!");
    }
    return mitglieder;
  }

  @Override
  public String getInfoText(Object selection) throws RemoteException
  {
    Mitglied[] mitglieder = null;
    String text = "";

    if (selection instanceof Mitglied)
    {
      mitglieder = new Mitglied[] { (Mitglied) selection };
    }
    else if (selection instanceof Mitglied[])
    {
      mitglieder = (Mitglied[]) selection;
    }
    else
    {
      return "";
    }

    // Aufruf aus Mitglieder View
    if (mitglieder != null)
    {
      text = "Es wurden " + mitglieder.length + " Mitglieder ausgewählt";
      String fehlen = "";
      for (Mitglied m : mitglieder)
      {
        if (m.getEmail() == null || m.getEmail().isEmpty())
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

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object object, String option)
      throws RemoteException, ApplicationException
  {
    List<Mitglied> mitglieder = getMitglieder(object);
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = null;
    int ohneMail = 0;

    for (Mitglied m : mitglieder)
    {
      String mail = m.getEmail();
      if ((mail == null || mail.isEmpty())
          && getAusgabeart().getValue() == Ausgabeart.MAIL)
      {
        ohneMail++;
      }
      liste.add(new DruckMailEmpfaengerEntry("Kontoauszug", mail, m.getName(),
          m.getVorname(), m.getMitgliedstyp()));
    }

    if (ohneMail == 1)
    {
      text = ohneMail + " Mitglied hat keine Mail Adresse.";
    }
    else if (ohneMail > 1)
    {
      text = ohneMail + " Mitglieder haben keine Mail Adresse.";
    }
    return new DruckMailEmpfaenger(liste, text);
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

}
