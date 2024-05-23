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
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import de.jost_net.JVerein.rmi.Spendenbescheinigung;


public class SpendenbescheinigungMailControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private TextInput mailbetreff;

  private TextAreaInput mailtext;
  
  private TextAreaInput  info;
  
  private Spendenbescheinigung[] spbArr;

  public SpendenbescheinigungMailControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public String getInfoText(Object spbArray)
  {
    spbArr = (Spendenbescheinigung[]) spbArray;
    String text = "Es wurden " + spbArr.length + 
        " Spendenbescheinigungen ausgewählt"
        + "\nFolgende Mitglieder haben keine Mailadresse:";
    try
    {
      for (Spendenbescheinigung spb: spbArr)
      {
        Mitglied m = spb.getMitglied();
        if (m != null && ( m.getEmail() == null || m.getEmail().isEmpty()))
        {
          text = text + "\n - " + m.getName()
              + ", " + m.getVorname();
        }
      }
      text = text 
          + "\nFür folgende Spendenbescheinigungen existiert kein Mitglied und keine Mailadresse:";
      for (Spendenbescheinigung spb: spbArr)
      {
        if (spb.getMitglied() == null)
        {
          text = text  + "\n - " + spb.getZeile1()
              + ", " + spb.getZeile2() + ", " + spb.getZeile3();
        }
      }
    }
    catch (Exception ex)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Ermitteln der Mitglieder aus den Spendenbescheinigungen");
    }
    return text;
  }

  public TextAreaInput getInfo() throws RemoteException
  {
    if (info != null)
    {
      return info;
    }
    info = new TextAreaInput(getInfoText(getCurrentObject()), 10000);
    info.setHeight(100);
    info.setEnabled(false);
    return info;
  }
  
  public TextInput getBetreff() throws RemoteException
  {
    if (mailbetreff != null)
    {
      return mailbetreff;
    }
    mailbetreff = new TextInput(settings.getString("spendenbescheinigungmail.subject", ""), 100);
    mailbetreff.setName("Betreff");
    return mailbetreff;
  }

  public TextAreaInput getTxt() throws RemoteException
  {
    if (mailtext != null)
    {
      return mailtext;
    }
    mailtext = new TextAreaInput(settings.getString("spendenbescheinigungmail.body", ""), 10000);
    mailtext.setName("Text");
    return mailtext;
  }
  
  public Button getStartButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          settings.setAttribute("spendenbescheinigungmail.subject",
              (String) mailbetreff.getValue());
          settings.setAttribute("spendenbescheinigungmail.body", 
              (String) mailtext.getValue());
          String betr = (String) mailbetreff.getValue();
          String text = (String) mailtext.getValue();
         sendeMail(betr, text);
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void sendeMail(final String betr, final String txt) throws RemoteException
  {

    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor)
      {
        try
        {
          MailSender sender = new MailSender(
              Einstellungen.getEinstellung().getSmtpServer(),
              Einstellungen.getEinstellung().getSmtpPort(),
              Einstellungen.getEinstellung().getSmtpAuthUser(),
              Einstellungen.getEinstellung().getSmtpAuthPwd(),
              Einstellungen.getEinstellung().getSmtpFromAddress(),
              Einstellungen.getEinstellung().getSmtpFromAnzeigename(),
              Einstellungen.getEinstellung().getMailAlwaysBcc(),
              Einstellungen.getEinstellung().getMailAlwaysCc(),
              Einstellungen.getEinstellung().getSmtpSsl(),
              Einstellungen.getEinstellung().getSmtpStarttls(),
              Einstellungen.getEinstellung().getMailVerzoegerung(),
              Einstellungen.getImapCopyData());

          Velocity.init();
          Logger.debug("preparing velocity context");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);

          int sentCount = 0;
          int size = spbArr.length;
          for (int i=0; i < size; i++)
          {
            double proz = (double) i / (double) size * 100d;
            monitor.setPercentComplete((int) proz);
            Mitglied m = spbArr[i].getMitglied();
            if (m == null || m.getEmail() == null || m.getEmail().isEmpty())
            {
              continue;
            }
            VelocityContext context = new VelocityContext();
            context.put("dateformat", new JVDateFormatTTMMJJJJ());
            context.put("decimalformat", Einstellungen.DECIMALFORMAT);
            context.put("email", m.getEmail());

            Map<String, Object> map = new MitgliedMap().getMap(m, null);
            map = new AllgemeineMap().getMap(map);
            VarTools.add(context, map);

            StringWriter wtext1 = new StringWriter();
            Velocity.evaluate(context, wtext1, "LOG", betr);

            StringWriter wtext2 = new StringWriter();
            Velocity.evaluate(context, wtext2, "LOG", txt);

            try
            {
              String path = Einstellungen.getEinstellung()
                  .getSpendenbescheinigungverzeichnis();
              if (path == null || path.length() == 0)
              {
                path = settings.getString("lastdir", System.getProperty("user.home"));
              }

              settings.setAttribute("lastdir", path);
              path = path.endsWith(File.separator) ? path : path + File.separator;
              TreeSet<MailAnhang> anhang = new TreeSet<MailAnhang>();
              MailAnhang anh = (MailAnhang) Einstellungen.getDBService()
                  .createObject(MailAnhang.class, null);
              String fileName = new Dateiname(m,
                  spbArr[i].getBescheinigungsdatum(), "Spendenbescheinigung",
                  Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                  "pdf").get();
              anh.setDateiname(fileName);
              fileName = path + fileName;
              File file = new File(fileName);
              FileInputStream fis = new FileInputStream(file);
              byte[] buffer = new byte[(int) file.length()];
              fis.read(buffer);
              anh.setAnhang(buffer);
              anhang.add(anh);
              fis.close();

              sender.sendMail(m.getEmail(), wtext1.getBuffer().toString(),
                  wtext2.getBuffer().toString(), anhang);
              sentCount++;

              // Mail in die Datenbank schreiben
              Mail mail = (Mail) Einstellungen.getDBService()
                  .createObject(Mail.class, null);
              Timestamp ts = new Timestamp(new Date().getTime());
              mail.setBearbeitung(ts);
              mail.setBetreff(wtext1.getBuffer().toString());
              mail.setTxt(wtext2.getBuffer().toString());
              mail.setVersand(ts);
              mail.store();
              MailEmpfaenger empf = (MailEmpfaenger) Einstellungen
                  .getDBService().createObject(MailEmpfaenger.class, null);
              empf.setMail(mail);
              empf.setMitglied(m);
              empf.setVersand(ts);
              empf.store();
              anh.setMail(mail);
              if (Einstellungen.getEinstellung().getAnhangSpeichern())
              {
                anh.store();
              }

              monitor.log(m.getEmail() + " - versendet");
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Mailversand", e);
              monitor.log(m.getEmail() + " - " + e.getMessage());
            }
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") + " verschickt");
          GUI.getCurrentView().reload();
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
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }

}
