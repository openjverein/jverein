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
package de.jost_net.JVerein.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.mail.MessagingException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Versand von Mails mit Anhang aus einer Zip-Datei an die Mitglieder. Wird z.
 * B. für den Rechnungsversand gebraucht<br>
 */
public class ZipMailer
{
  /**
   * Sendet die Mail mit den Dateien aus dem Zip an alle Empfnger
   * 
   * @param zipfile
   *          das Archiv mit allen PDFs (o.ä.) die an die Mitglieder verschickt
   *          werden sollen. Die Dateien darin müssen den Dateinamen in der Form
   *          MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf haben.
   * @param betreff
   *          Betreff der Mail
   * @param text
   *          Text der Mail
   * @throws RemoteException
   */
  public ZipMailer(final File zipfile, final String betreff, String text)
      throws RemoteException
  {
    // ggf. Signatur anhängen
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

    BackgroundTask t = new BackgroundTask()
    {
      private boolean cancel = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try (ZipFile zip = new ZipFile(zipfile))
        {
          MailSender sender = new MailSender(
              (String) Einstellungen.getEinstellung(Property.SMTPSERVER),
              (String) Einstellungen.getEinstellung(Property.SMTPPORT),
              (String) Einstellungen.getEinstellung(Property.SMTPAUTHUSER),
              Einstellungen.getSmtpAuthPwd(),
              (String) Einstellungen.getEinstellung(Property.SMTPFROMADDRESS),
              (String) Einstellungen.getEinstellung(Property.SMTPFROMANZEIGENAME),
              (String) Einstellungen.getEinstellung(Property.MAILALWAYSBCC),
              (String) Einstellungen.getEinstellung(Property.MAILALWAYSCC),
              (Boolean) Einstellungen.getEinstellung(Property.SMTPSSL),
              (Boolean) Einstellungen.getEinstellung(Property.SMTPSTARTTLS),
              (Integer) Einstellungen.getEinstellung(Property.MAILVERZOEGERUNG),
              Einstellungen.getImapCopyData());

          Velocity.init();
          Logger.debug("preparing velocity context");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          int sentCount = 0;

          int zae = 0;
          int size = zip.size();
          for (@SuppressWarnings("rawtypes")
          Enumeration e = zip.entries(); e.hasMoreElements();)
          {
            if(isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Mailversand abgebrochen");
              monitor.setPercentComplete(100);
              return;
            }
            ZipEntry entry = (ZipEntry) e.nextElement();
            String currentEntry = entry.getName();
            if (currentEntry.indexOf("@") > 0)
            {
              VelocityContext context = new VelocityContext();
              context.put("dateformat", new JVDateFormatTTMMJJJJ());
              context.put("decimalformat", Einstellungen.DECIMALFORMAT);
              Map<String, Object> map = new AllgemeineMap().getMap(null);

              // Dateiname muss das Format
              // MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf haben
              String[] teile = currentEntry.split("#", 5);

              if (teile.length != 5)
              {
                throw new ApplicationException(
                    "Ungültiger Dateiname: " + currentEntry);
              }

              String id = teile[0];
              String art = teile[1];
              String artId = teile[2];
              String mail = teile[3];
              String dateiname = teile[4];
              
              Rechnung re = null;
              Spendenbescheinigung spb = null;
              
              // Mitglied Map hinzufügen
              Mitglied m = (Mitglied) Einstellungen.getDBService()
                  .createObject(Mitglied.class, id);
              map = new MitgliedMap().getMap(m, map);

              switch (art.toLowerCase().trim())
              {
                case "rechnung":
                case "mahnung":
                  re = (Rechnung) Einstellungen.getDBService()
                      .createObject(Rechnung.class, artId);
                  map = new RechnungMap().getMap(re, map);
                  break;
                case "spendenbescheinigung":
                  spb = (Spendenbescheinigung) Einstellungen.getDBService()
                      .createObject(Spendenbescheinigung.class, artId);
                  map = new SpendenbescheinigungMap().getMap(spb, map);
                  break;
                case "lastschrift":
                  Lastschrift ls = (Lastschrift) Einstellungen.getDBService()
                      .createObject(Lastschrift.class, artId);
                  map = new LastschriftMap().getMap(ls, map);
                  break;
                case "":
                case "freiesformular":
                case "kontoauszug":
                  // Keine eigene Map verwendet
                  break;
                default:
                  Logger.error("Zipmailer Map nicht implementiert: " + art);
                  break;
              }
              VarTools.add(context, map);

              MailAnhang ma = (MailAnhang) Einstellungen.getDBService()
                  .createObject(MailAnhang.class, null);
              InputStream in = zip.getInputStream(entry);
              ByteArrayOutputStream bos = new ByteArrayOutputStream();
              // buffer size
              byte[] b = new byte[1024];
              int count;
              while ((count = in.read(b)) > 0)
              {
                bos.write(b, 0, count);
              }
              in.close();
              ma.setAnhang(bos.toByteArray());

              String finaldateiname = "";
              switch (art.toLowerCase().trim())
              {
                case "rechnung":
                  finaldateiname = VorlageUtil.getName(
                      VorlageTyp.RECHNUNG_MITGLIED_DATEINAME, re, m) + ".pdf";
                  break;
                case "mahnung":
                  finaldateiname = VorlageUtil.getName(
                      VorlageTyp.MAHNUNG_MITGLIED, re, m) + ".pdf";
                  break;
                case "spendenbescheinigung":
                  finaldateiname = VorlageUtil.getName(
                      VorlageTyp.SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME, spb, m)
                      + ".pdf";
                  break;
                case "freiesformular":
                  finaldateiname = VorlageUtil.getName(
                      VorlageTyp.FREIES_FORMULAR_MITGLIED_DATEINAME,
                      dateiname.substring(0, dateiname.lastIndexOf('.')), m)
                      + ".pdf";
                  break;
                case "kontoauszug":
                  finaldateiname = VorlageUtil.getName(
                      VorlageTyp.KONTOAUSZUG_MITGLIED_DATEINAME, null, m) + ".pdf";
                  break;
                default:
                  StringWriter wdateiname = new StringWriter();
                  Velocity.evaluate(context, wdateiname, "LOG", dateiname);
                  finaldateiname = wdateiname.toString();
                  break;
              }
              ma.setDateiname(finaldateiname);
              TreeSet<MailAnhang> anhang = new TreeSet<>();
              anhang.add(ma);

              StringWriter wtext1 = new StringWriter();
              Velocity.evaluate(context, wtext1, "LOG", betreff);

              StringWriter wtext2 = new StringWriter();
              Velocity.evaluate(context, wtext2, "LOG", txt);

              monitor.log("Versende an " + mail);
              try
              {
                try
                {
                  sender.sendMail(mail, wtext1.toString(), wtext2.toString(),
                      anhang);
                }
                // Wenn eine ApplicationException geworfen wurde, wurde die
                // Mails erfolgreich versendet, erst danach trat ein Fehler auf.
                catch (ApplicationException ae)
                {
                  Logger.error("Fehler: ", ae);
                  monitor.log(mail + " - " + ae.getMessage());
                }
                sentCount++;
                      
                Mail ml = (Mail) Einstellungen.getDBService()
                        .createObject(Mail.class, null);
                ml.setBetreff(wtext1.toString());
                ml.setTxt(wtext2.toString());
                ml.setBearbeitung(new Timestamp(new Date().getTime()));
                ml.setVersand(new Timestamp(new Date().getTime()));
                ml.store();
                
                MailEmpfaenger me = (MailEmpfaenger) Einstellungen.getDBService()
                        .createObject(MailEmpfaenger.class, null);
                me.setMitglied(m);
                me.setMail(ml);
                me.setVersand(new Timestamp(new Date().getTime()));
                me.store();
                if ((Boolean) Einstellungen.getEinstellung(Property.ANHANGSPEICHERN))
                {
                  ma.setMail(ml);
                  ma.store();
                }
              }
              catch (MessagingException me)
              {
                monitor.log("Versand fehlgeschlagen: " + mail);
                Logger.error("Fehler beim Mailversand: " + me);
              }
            } // Ende von if
            zae++;
            double proz = (double) zae / (double) size * 100d;
            monitor.setPercentComplete((int) proz);
          } // Ende von for
          zip.close();
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") + " verschickt");
          GUI.getCurrentView().reload();
        }
        catch (ZipException e)
        {
          e.printStackTrace();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        catch (Exception e)
        {
          e.printStackTrace();
          throw new ApplicationException(e);
        }

        monitor.setPercentComplete(100);
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
        GUI.getCurrentView().reload();
      } // Ende von run()

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
}