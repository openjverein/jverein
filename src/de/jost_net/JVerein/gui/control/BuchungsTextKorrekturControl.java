/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Queries.BuchungsKorrekturQuery;
import de.jost_net.JVerein.util.BuchungsZweckKorrektur;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.formatter.KontoFormatter;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;

public class BuchungsTextKorrekturControl extends AbstractControl
{

  private TablePart buchungsList;

  private BuchungsKorrekturQuery query;

  public BuchungsTextKorrekturControl(AbstractView view)
  {
    super(view);
  }

  public Button getStartKorrekturButton()
  {
    return new Button("Korrektur", context -> starteKorrektur(), null, true,
        "walking.png");
  }

  public Part getBuchungsList() throws RemoteException
  {
    // Buchungen holen
    query = new BuchungsKorrekturQuery();
    if (buchungsList == null)
    {
      buchungsList = new TablePart(query.get(), new BuchungAction(false));
      buchungsList.addColumn("Nr", "id-int");
      buchungsList.addColumn("S", "splitid", o -> (o != null ? "S" : " "));
      buchungsList.addColumn("Konto", "konto", new KontoFormatter());
      buchungsList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));

      buchungsList.addColumn("Verwendungszweck neu", "zweck", value -> {
        if (value == null)
        {
          return null;
        }
        return BuchungsZweckKorrektur.getBuchungsZweckKorrektur(value.toString(), false);
      });
      buchungsList.addColumn("Verwendungszweck alt", "zweck", value -> {
        if (value == null)
        {
          return null;
        }
        String s = value.toString();
        s = s.replaceAll("\r\n", "|");
        s = s.replaceAll("\r", "|");
        s = s.replaceAll("\n", "|");
        return s;
      });
      buchungsList.setMulti(true);
      buchungsList.setRememberColWidths(true);
      buchungsList.setRememberOrder(true);
      buchungsList.setRememberState(true);
      BuchungMessageConsumer mc = new BuchungMessageConsumer();
      Application.getMessagingFactory().registerMessageConsumer(mc);
    }
    else
    {
      buchungsList.removeAll();

      for (Buchung buchung : query.get())
      {
        buchungsList.addItem(buchung);
      }
      buchungsList.sort();
    }

    return buchungsList;
  }

  public void refreshBuchungen() throws RemoteException
  {
    if (buchungsList == null)
    {
      return;
    }
    buchungsList.removeAll();

    for (Buchung buchung : query.get())
    {
      buchungsList.addItem(buchung);
    }
    buchungsList.sort();
  }

  private void starteKorrektur()
  {
    try
    {
      int count = 0;
      DBTransaction.starten();

      for (Object item : buchungsList.getItems())
      {
        Buchung b = (Buchung) item;
        if (b.getJahresabschluss() != null)
        {
          continue;
        }
        String zweck = b.getZweck();
        zweck = BuchungsZweckKorrektur.getBuchungsZweckKorrektur(zweck, true);
        b.setZweck(zweck);
        b.store();
        count++;
      }

      DBTransaction.commit();
      GUI.getStatusBar().setSuccessText(count + " Buchungen korrigiert");
      refreshBuchungen();
    }
    catch (ApplicationException | RemoteException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
  }

  /**
   * Wird benachrichtigt, um die Anzeige zu aktualisieren.
   */
  private class BuchungMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[] { BuchungMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(() -> {
        try
        {
          if (buchungsList == null)
          {
            // Eingabe-Feld existiert nicht. Also abmelden
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(BuchungMessageConsumer.this);
            return;
          }
          refreshBuchungen();
        }
        catch (Exception e)
        {
          // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
          Logger.error("unable to refresh Splitbuchungen", e);
          Application.getMessagingFactory()
              .unRegisterMessageConsumer(BuchungMessageConsumer.this);
        }
      });
    }

  }
}
