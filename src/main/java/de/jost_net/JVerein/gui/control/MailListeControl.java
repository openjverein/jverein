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
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.MailMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.MailDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MailListeControl extends FilterControl
{

  private JVereinTablePart mailsList;

  public MailListeControl(AbstractView view)
  {
    super(view, MailControl.class);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (mailsList != null)
    {
      return mailsList;
    }
    mailsList = new JVereinTablePart(getMails(), null);
    mailsList.addColumn("Nr", "id-int");
    mailsList.addColumn(getGesperrtColumn(), false);
    mailsList.addColumn("Betreff", "betreff");
    mailsList.addColumn("Bearbeitung", "bearbeitung",
        new DateFormatter(new JVDateFormatDATETIME()));
    mailsList.addColumn("Versand", "versand",
        new DateFormatter(new JVDateFormatDATETIME()));
    mailsList.addColumn("Anhänge", "anhaenge");
    mailsList.setContextMenu(new MailMenu(mailsList));
    mailsList.setMulti(true);
    mailsList.setAction(new EditAction(MailDetailView.class, mailsList));
    VorZurueckControl.setObjektListe(null, null);
    return mailsList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (mailsList == null)
      {
        return;
      }
      mailsList.removeAll();
      DBIterator<Mail> mails = getMails();
      while (mails.hasNext())
      {
        mailsList.addItem(mails.next());
      }
      mailsList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Mail> getMails()
      throws RemoteException, ApplicationException
  {
    DBIterator<Mail> mails = Einstellungen.getDBService()
        .createList(Mail.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case MAIL_EMPFAENGER:
          mails.join("mailempfaenger");
          mails.addFilter("mailempfaenger.mail = mail.id");
          mails.join("mitglied");
          mails.addFilter("mitglied.id = mailempfaenger.mitglied");
          mails.addFilter("(lower(name) like ? or lower(vorname) like ?) ",
              "%" + value.toString().toLowerCase() + "%",
              "%" + value.toString().toLowerCase() + "%");

          break;
        case BETREFF:
          mails.addFilter("(lower(betreff) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_BEARBEITUNG_VON:
          mails.addFilter("bearbeitung >= ?", value);
          break;
        case DATUM_BEARBEITUNG_BIS:
          Calendar cal = Calendar.getInstance();
          cal.setTime((Date) value);
          cal.add(Calendar.DAY_OF_MONTH, 1);
          mails.addFilter("bearbeitung <= ?",
              new java.sql.Date(cal.getTimeInMillis()));
          break;
        case DATUM_VERSAND_VON:
          mails.addFilter("mail.versand >= ?", value);
          break;
        case DATUM_VERSAND_BIS:
          Calendar calendar = Calendar.getInstance();
          calendar.setTime((Date) value);
          calendar.add(Calendar.DAY_OF_MONTH, 1);
          mails.addFilter("mail.versand <= ?", calendar.getTime());
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    mails.setOrder("ORDER BY betreff");

    return mails;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.MAILS_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.MAILS_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.MAILS_DATEINAME, this);
  }
}
