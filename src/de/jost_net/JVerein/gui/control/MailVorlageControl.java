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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.menu.MailVorlageMenu;
import de.jost_net.JVerein.rmi.MailVorlage;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MailVorlageControl extends AbstractControl
    implements Savable, IMailControl
{

  private TablePart mailvorlageList;

  private TextInput betreff;

  private TextAreaInput txt;

  private MailVorlage mailvorlage;

  public MailVorlageControl(AbstractView view)
  {
    super(view);
  }

  private MailVorlage getMailVorlage()
  {
    if (mailvorlage != null)
    {
      return mailvorlage;
    }
    mailvorlage = (MailVorlage) getCurrentObject();
    return mailvorlage;
  }

  public TextInput getBetreff(boolean withFocus) throws RemoteException
  {
    if (betreff != null)
    {
      return betreff;
    }
    betreff = new TextInput(getMailVorlage().getBetreff(), 100);
    betreff.setName("Betreff");
    betreff.setMandatory(true);
    if (withFocus)
    {
      betreff.focus();
    }
    return betreff;
  }

  @Override
  public String getBetreffString()
  {
    return (String) betreff.getValue();
  }

  public TextAreaInput getTxt() throws RemoteException
  {
    if (txt != null)
    {
      return txt;
    }
    txt = new TextAreaInput(getMailVorlage().getTxt(), 10000);
    txt.setName("Text");
    txt.setMandatory(true);
    return txt;
  }

  @Override
  public String getTxtString()
  {
    return (String) txt.getValue();
  }

  @Override
  public DBObject prepareStore() throws RemoteException, ApplicationException
  {
    MailVorlage mv = getMailVorlage();
    String betreff = (String) getBetreff(false).getValue();
    if (betreff == null || betreff.isEmpty())
    {
      throw new ApplicationException("Bitte Betreff eingeben!");
    }
    DBIterator<MailVorlage> vorlagen = Einstellungen.getDBService()
        .createList(MailVorlage.class);
    vorlagen.addFilter("betreff = ?", betreff);
    if (vorlagen.hasNext() && mv.isNewObject())
    {
      throw new ApplicationException(
          "Es existiert bereits eine Vorlage mit diesem Betreff!");
    }

    mv.setBetreff(betreff);
    mv.setTxt((String) getTxt().getValue());
    return mv;
  }

  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore();
      MailVorlage mv = getMailVorlage();
      mv.store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der MailVorlage";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public TablePart getMailVorlageTable(Action action) throws RemoteException
  {
    if (mailvorlageList != null)
    {
      return mailvorlageList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<MailVorlage> fdef = service.createList(MailVorlage.class);
    mailvorlageList = new TablePart(fdef, action);
    mailvorlageList.addColumn("Betreff", "betreff");
    mailvorlageList.setContextMenu(new MailVorlageMenu());
    return mailvorlageList;
  }
}
