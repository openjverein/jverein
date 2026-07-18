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
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Messaging.MailDeleteMessage;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.MailAnhangAnzeigeAction;
import de.jost_net.JVerein.gui.dialogs.MailEmpfaengerAuswahlDialog;
import de.jost_net.JVerein.gui.menu.MailAnhangMenu;
import de.jost_net.JVerein.gui.menu.MailEmpfaengerMenu;
import de.jost_net.JVerein.gui.menu.MailMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.MailDetailView;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MailControl extends FilterControl implements IMailControl, Savable
{

  private AutoUpdateTablePart empfaenger;

  private TextInput betreff;

  private TextAreaInput txt;

  private JVereinTablePart anhang;

  private JVereinTablePart mitgliedmitmail;

  private Mail mail;

  private JVereinTablePart mailsList;

  private MailDeleteMessageConsumer mailDeleteConsumer = null;

  private ButtonRtoL anhangButton;

  public MailControl(AbstractView view)
  {
    super(view);
  }

  public Mail getMail()
  {
    if (mail != null)
    {
      return mail;
    }
    mail = (Mail) getCurrentObject();
    return mail;
  }

  public AutoUpdateTablePart getEmpfaenger() throws RemoteException
  {
    if (empfaenger != null)
    {
      return empfaenger;
    }
    empfaenger = new AutoUpdateTablePart(getMail().getEmpfaenger(), null);
    empfaenger.addColumn("Id", "id");
    empfaenger.addColumn("Mail-Adresse", "mailadresse");
    empfaenger.addColumn("Name", "name");
    empfaenger.addColumn("Versand", "versand",
        new DateFormatter(new JVDateFormatDATETIME()));
    empfaenger.setContextMenu(new MailEmpfaengerMenu(this));
    empfaenger.setMulti(true);
    return empfaenger;
  }

  public void addEmpfaenger(MailEmpfaenger me) throws RemoteException
  {
    // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht werden
    for (MailEmpfaenger e : getMail().getEmpfaenger())
    {
      if (BeanUtil.equals(e, me))
      {
        return;
      }
    }
    getEmpfaenger().addItem(me);
    getMail().getEmpfaenger().add(me);
  }

  public void removeEmpfaenger(MailEmpfaenger me)
      throws RemoteException, ApplicationException
  {
    getEmpfaenger().removeItem(me);
    getMail().getEmpfaenger().remove(me);
  }

  public void addAnhang(MailAnhang ma) throws RemoteException
  {
    // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht werden
    for (MailAnhang a : getMail().getAnhang())
    {
      if (BeanUtil.equals(a, ma))
      {
        return;
      }
    }
    getAnhang().addItem(ma);
    getMail().getAnhang().add(ma);
  }

  public void removeAnhang(MailAnhang ma) throws RemoteException
  {
    getAnhang().removeItem(ma);
    getMail().getAnhang().remove(ma);
  }

  public JVereinTablePart getMitgliedMitMail() throws RemoteException
  {
    if (mitgliedmitmail != null && mitgliedmitmail.size() > 0)
    {
      return mitgliedmitmail;
    }
    DBIterator<Mitglied> it = Einstellungen.getDBService()
        .createList(Mitglied.class);
    it.addFilter("email is not null and length(email) > 0");
    mitgliedmitmail = new JVereinTablePart(it, null);
    mitgliedmitmail.addColumn("EMail", "email");
    mitgliedmitmail.addColumn("Name", "name");
    mitgliedmitmail.addColumn("Vorname", "vorname");
    mitgliedmitmail.addColumn("Mitgliedstyp", Mitglied.MITGLIEDSTYP);
    mitgliedmitmail.setCheckable(true);
    mitgliedmitmail.removeFeature(FeatureSummary.class);
    return mitgliedmitmail;
  }

  public TextInput getBetreff() throws RemoteException
  {
    if (betreff != null)
    {
      return betreff;
    }
    betreff = new TextInput(getMail().getBetreff(), 100);
    betreff.setName("Betreff");
    if (getMail().getVersand() != null)
    {
      betreff.disable();
    }
    return betreff;
  }

  public TextAreaInput getTxt() throws RemoteException
  {
    if (txt != null)
    {
      return txt;
    }
    txt = new TextAreaInput(getMail().getTxt(), 10000);
    txt.setName("Text");
    if (getMail().getVersand() != null)
    {
      txt.disable();
    }
    return txt;
  }

  public JVereinTablePart getAnhang() throws RemoteException
  {
    if (anhang != null)
    {
      return anhang;
    }

    // Umwandeln in ArrayList
    ArrayList<MailAnhang> anhang2 = new ArrayList<>();
    for (MailAnhang ma : getMail().getAnhang())
    {
      anhang2.add(ma);
    }
    this.mailDeleteConsumer = new MailDeleteMessageConsumer();
    Application.getMessagingFactory()
        .registerMessageConsumer(this.mailDeleteConsumer);
    anhang = new JVereinTablePart(anhang2, new MailAnhangAnzeigeAction());
    anhang.addColumn("Dateiname", "dateiname");
    anhang.setContextMenu(new MailAnhangMenu(this));
    anhang.setMulti(true);
    return anhang;
  }

  public ButtonRtoL getAddEmpfaengerButton()
  {
    ButtonRtoL b = new ButtonRtoL("Empfänger hinzufügen", context -> {
      MailEmpfaengerAuswahlDialog mead = new MailEmpfaengerAuswahlDialog(
          (MailControl) context, MailEmpfaengerAuswahlDialog.POSITION_CENTER);
      try
      {
        mead.open();
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        throw new ApplicationException(e.getMessage());
      }
    }, this, false, "list-add.png");
    return b;
  }

  public ButtonRtoL getAddAnhangButton() throws RemoteException
  {
    if (anhangButton != null)
    {
      return anhangButton;
    }
    anhangButton = new ButtonRtoL("Anhang hinzufügen", context -> {
      Settings settings = new Settings(this.getClass());
      settings.setStoreWhenRead(true);
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN | SWT.MULTI);
      fd.setFilterPath(
          settings.getString("lastdir", System.getProperty("user.home")));
      fd.setText("Bitte wählen Sie einen Anhang aus.");
      if (fd.open() != null)
      {
        try
        {
          for (String f : fd.getFileNames())
          {
            MailAnhang anh = (MailAnhang) Einstellungen.getDBService()
                .createObject(MailAnhang.class, null);
            anh.setDateiname(f);
            File file = new File(
                fd.getFilterPath() + System.getProperty("file.separator") + f);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            anh.setAnhang(buffer);
            ((MailControl) context).addAnhang(anh);
            fis.close();
            settings.setAttribute("lastdir", file.getParent());
          }
        }
        catch (Exception e)
        {
          Logger.error("", e);
          throw new ApplicationException(e);
        }
      }
    }, this, false, "list-add.png");
    if (getMail().getVersand() != null)
    {
      anhangButton.setEnabled(false);
    }
    return anhangButton;
  }

  public ButtonRtoL getMailSendButton()
  {
    ButtonRtoL b = new ButtonRtoL("Senden", context -> {
      try
      {
        // Insert Check
        checkInputs();

        int toBeSentCount = 0;
        for (final MailEmpfaenger empf : getMail().getEmpfaenger())
        {
          if (empf.getVersand() == null)
          {
            toBeSentCount++;
          }
        }
        if (toBeSentCount == 0)
        {
          SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
          d.setTitle("Mail bereits versendet");
          d.setText("Mail wurde bereits an alle Empfänger versendet!");
          try
          {
            d.open();
          }
          catch (Exception e)
          {
            Logger.error("Fehler beim Nicht-Senden der Mail", e);
          }
          return;
        }
        if (toBeSentCount != getMail().getEmpfaenger().size())
        {
          YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
          d.setTitle("Mail senden?");
          d.setText("Diese Mail wurde bereits an "
              + (getMail().getEmpfaenger().size() - toBeSentCount)
              + " der gewählten Empfänger versendet. Wollen Sie diese Mail an alle weiteren "
              + toBeSentCount + " Empfänger senden?");
          try
          {
            Boolean choice = (Boolean) d.open();
            if (!choice.booleanValue())
              return;
          }
          catch (Exception e)
          {
            Logger.error("Fehler beim Senden der Mail", e);
            return;
          }
        }
        sendeMail();
        handleStore(true);
      }
      catch (RemoteException e)
      {
        Logger.error(e.getMessage());
        throw new ApplicationException("Fehler beim Senden der Mail");
      }
    }, null, true, "envelope-open.png");
    return b;
  }

  @Override
  public String getBetreffString() throws RemoteException
  {
    return (String) getBetreff().getValue();
  }

  @Override
  public String getTxtString() throws RemoteException
  {
    return (String) getTxt().getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Mitglied> getEmpfaengerList() throws RemoteException
  {
    if (empfaenger != null)
    {
      List<Mitglied> mitglieder = new ArrayList<>();
      for (MailEmpfaenger e : (List<MailEmpfaenger>) empfaenger.getItems())
      {
        mitglieder.add(e.getMitglied());
      }
      return mitglieder;
    }
    return null;
  }

  /**
   * Versende Mail an Empfänger. Es wird nur an Empfänger gesendet für die die
   * Mail noch nicht versendet wurde.
   */
  private void sendeMail() throws RemoteException
  {
    String text = getTxtString();
    if (text.toLowerCase().contains("<html")
        && text.toLowerCase().contains("</body"))
    {
      // MailSignatur ohne Separator mit vorangestellten hr in den body einbauen
      text = text.substring(0, text.toLowerCase().indexOf("</body") - 1);
      text = text + "<hr />"
          + (String) Einstellungen.getEinstellung(Property.MAILSIGNATUR);
      text = text + "</body></html>";
    }
    else
    {
      // MailSignatur mit Separator einfach anhängen
      text = text + Einstellungen.getMailSignatur(true);
    }
    final String txt = text;
    final String betr = getBetreffString();
    BackgroundTask t = new BackgroundTask()
    {

      private boolean cancel = false;

      @Override
      public void run(ProgressMonitor monitor)
      {
        try
        {
          MailSender sender = new MailSender(
              (String) Einstellungen.getEinstellung(Property.SMTPSERVER),
              (String) Einstellungen.getEinstellung(Property.SMTPPORT),
              (String) Einstellungen.getEinstellung(Property.SMTPAUTHUSER),
              Einstellungen.getSmtpAuthPwd(),
              (String) Einstellungen.getEinstellung(Property.SMTPFROMADDRESS),
              (String) Einstellungen
                  .getEinstellung(Property.SMTPFROMANZEIGENAME),
              (String) Einstellungen.getEinstellung(Property.MAILALWAYSBCC),
              (String) Einstellungen.getEinstellung(Property.MAILALWAYSCC),
              (Boolean) Einstellungen.getEinstellung(Property.SMTPSSL),
              (Boolean) Einstellungen.getEinstellung(Property.SMTPSTARTTLS),
              (Integer) Einstellungen.getEinstellung(Property.MAILVERZOEGERUNG),
              Einstellungen.getImapCopyData());

          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          int zae = 0;
          int sentCount = 0;
          for (final MailEmpfaenger empf : getMail().getEmpfaenger())
          {
            if (isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Mailversand abgebrochen");
              monitor.setPercentComplete(100);
              return;
            }
            try
            {
              if (empf.getVersand() == null)
              {
                Map<String, Object> map = new MitgliedMap().getMap(
                    empf.getMitglied(), new AllgemeineMap().getMap(null));
                map.put("email", empf.getMitglied().getEmail());
                map.put("empf", empf.getMitglied());

                String betreffParsed = VelocityTool.eval(map, betr);
                String textParsed = VelocityTool.eval(map, txt);
                try
                {
                  sender.sendMail(empf.getMailAdresse(), betreffParsed,
                      textParsed, getMail().getAnhang());
                }
                // Wenn eine ApplicationException geworfen wurde, wurde die
                // Mails erfolgreich versendet, erst danach trat ein Fehler auf.
                catch (ApplicationException ae)
                {
                  Logger.error("Fehler: ", ae);
                  monitor.log(empf.getMailAdresse() + " - "
                      + ae.getMessage().split("\n")[0]);
                }
                sentCount++;
                monitor.log(empf.getMailAdresse() + " - versendet");
                // Nachricht wurde erfolgreich versendet; speicher Versand-Datum
                // persistent.
                empf.setVersand(new Timestamp(new Date().getTime()));
                // Fix null value in colum mail for mailempfaenger
                empf.setMail(getMail());
                empf.store();
              }
              else
              {
                monitor.log(empf.getMailAdresse() + " - übersprungen");
              }
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Mailversand", e);
              monitor.log(empf.getMailAdresse() + " - " + e.getMessage());
            }
            zae++;
            double proz = (double) zae
                / (double) getMail().getEmpfaenger().size() * 100d;
            monitor.setPercentComplete((int) proz);
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") + " verschickt");
          getMail().store();
          if (sentCount > 0)
          {
            updateInputs();
          }
        }
        catch (ApplicationException ae)
        {
          Logger.error("", ae);
          monitor.log(ae.getMessage());
        }
        catch (Exception re)
        {
          Logger.error("", re);
          monitor.log(re.getMessage());
        }
      }

      @Override
      public void interrupt()
      {
        this.cancel = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return this.cancel;
      }
    };
    Application.getController().start(t);
  }

  @Override
  public boolean hasChanged() throws RemoteException
  {
    if (!getMail().getBetreff().equals(getBetreffString())
        || !getMail().getTxt().equals(getTxtString()) || empfaengerChanged()
        || anhangChanged())
    {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean empfaengerChanged() throws RemoteException
  {
    if (getMail().getEmpfaenger().size() != getEmpfaenger().size())
    {
      return true;
    }

    // Auf gleiche Einträge prüfen
    for (MailEmpfaenger me : getMail().getEmpfaenger())
    {
      boolean found = false;
      // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
      // werden
      for (MailEmpfaenger e : (List<MailEmpfaenger>) getEmpfaenger().getItems())
      {
        if (BeanUtil.equals(e, me))
        {
          found = true;
        }
      }
      if (!found)
      {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean anhangChanged() throws RemoteException
  {
    if (getMail().getAnhang().size() != getAnhang().size())
    {
      return true;
    }

    // Auf gleiche Einträge prüfen
    for (MailAnhang me : getMail().getAnhang())
    {
      boolean found = false;
      // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
      // werden
      for (MailAnhang e : (List<MailAnhang>) getAnhang().getItems())
      {
        if (BeanUtil.equals(e, me))
        {
          found = true;
        }
      }
      if (!found)
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Mail m = getMail();
    m.setBetreff(getBetreffString());
    m.setTxt(getTxtString());
    return m;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    handleStore(false);
  }

  /**
   * Speichert die Mail in der DB.
   *
   * @param mitversand
   *          wenn true, wird Spalte Versand auf aktuelles Datum gesetzt.
   * @throws ApplicationException
   */
  public void handleStore(boolean mitversand) throws ApplicationException
  {
    try
    {
      Mail m = (Mail) prepareStore();
      m.setBearbeitung(new Timestamp(new Date().getTime()));
      if (mitversand)
      {
        m.setVersand(new Timestamp(new Date().getTime()));
      }
      m.store();
      for (MailEmpfaenger me : getMail().getEmpfaenger())
      {
        me.setMail(m);
        me.store();
      }
      DBIterator<MailEmpfaenger> it = Einstellungen.getDBService()
          .createList(MailEmpfaenger.class);
      it.addFilter("mail = ?", new Object[] { m.getID() });
      while (it.hasNext())
      {
        MailEmpfaenger me = it.next();

        // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
        // werden
        boolean found = false;
        for (MailEmpfaenger e : m.getEmpfaenger())
        {
          if (BeanUtil.equals(e, me))
          {
            found = true;
            break;
          }
        }
        if (!found)
        {
          me.delete();
        }
      }
      for (MailAnhang ma : getMail().getAnhang())
      {
        ma.setMail(m);
        ma.store();
      }
      it = Einstellungen.getDBService().createList(MailAnhang.class);
      it.addFilter("mail = ?", new Object[] { m.getID() });
      while (it.hasNext())
      {
        MailAnhang ma = (MailAnhang) it.next();
        // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
        // werden
        boolean found = false;
        for (MailAnhang a : m.getAnhang())
        {
          if (BeanUtil.equals(a, ma))
          {
            found = true;
            break;
          }
        }
        if (!found)
        {
          ma.delete();
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Mail";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }

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

  private void checkInputs() throws ApplicationException
  {
    try
    {
      String betreff = (String) getBetreffString();
      if (betreff == null || betreff.isBlank())
      {
        throw new ApplicationException("Bitte Betreff eingeben");
      }
      String text = (String) getTxtString();
      if (text == null || text.isBlank())
      {
        throw new ApplicationException("Bitte Text eingeben");
      }
      if (text.length() > 10000)
      {
        throw new ApplicationException(
            "Maximale Länge des Textes 10.000 Zeichen");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Insert check of mail failed", e);
      throw new ApplicationException(
          "Mail kann nicht gespeichert werden. Siehe system log");
    }
  }

  private void updateInputs() throws RemoteException
  {
    boolean unversand = getMail().getVersand() == null;
    getBetreff().setEnabled(unversand);
    getTxt().setEnabled(unversand);
    getAddAnhangButton().setEnabled(unversand);
  }

  public void setDragDrop(Composite composit)
  {
    DropTarget target = new DropTarget(composit,
        DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener()
    {

      @Override
      public void dragEnter(DropTargetEvent event)
      {
        if (event.detail == DND.DROP_DEFAULT)
        {
          if ((event.operations & DND.DROP_COPY) != 0)
            event.detail = DND.DROP_COPY;
          else
            event.detail = DND.DROP_NONE;
        }
        for (int i = 0; i < event.dataTypes.length; i++)
        {
          if (fileTransfer.isSupportedType(event.dataTypes[i]))
          {
            event.currentDataType = event.dataTypes[i];
            // files should only be copied
            if (event.detail != DND.DROP_COPY)
              event.detail = DND.DROP_NONE;
            break;
          }
        }
      }

      @Override
      public void drop(DropTargetEvent event)
      {
        if (event.data == null)
        {
          event.detail = DND.DROP_NONE;
          GUI.getStatusBar()
              .setErrorText("Fehler bem Hinzufügen der Datei(en)");
          return;
        }
        try
        {
          for (String filename : (String[]) event.data)
          {
            MailAnhang anh = (MailAnhang) Einstellungen.getDBService()
                .createObject(MailAnhang.class, null);
            File file = new File(filename);
            anh.setDateiname(file.getName());
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            anh.setAnhang(buffer);
            addAnhang(anh);
            fis.close();
          }
        }
        catch (IOException e)
        {
          GUI.getStatusBar()
              .setErrorText("Fehler bem Hinzufügen der Datei(en)");
        }
      }

      @Override
      public void dragLeave(DropTargetEvent event)
      {
      }

      @Override
      public void dragOperationChanged(DropTargetEvent event)
      {
      }

      @Override
      public void dragOver(DropTargetEvent event)
      {
      }

      @Override
      public void dropAccept(DropTargetEvent event)
      {
      }
    });
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class MailDeleteMessageConsumer implements MessageConsumer
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
      return new Class[] { MailDeleteMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            if (((MailDeleteMessage) message).getObject() instanceof MailAnhang)
            {
              removeAnhang(
                  (MailAnhang) ((MailDeleteMessage) message).getObject());
            }
            else if (((MailDeleteMessage) message)
                .getObject() instanceof MailEmpfaenger)
            {
              removeEmpfaenger(
                  (MailEmpfaenger) ((MailDeleteMessage) message).getObject());
            }
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("Fehler beim Mail Anhang löschen", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(MailDeleteMessageConsumer.this);
          }
        }
      });
    }
  }

  public void deregisterMailDeleteConsumer()
  {
    Application.getMessagingFactory()
        .unRegisterMessageConsumer(mailDeleteConsumer);
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
