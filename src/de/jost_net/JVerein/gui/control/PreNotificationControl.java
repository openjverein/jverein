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
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.io.Ct1Ueberweisung;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.keys.Ct1Ausgabe;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.AbrechnungslaufImpl;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class PreNotificationControl extends DruckMailControl
{

  private TabFolder folder = null;

  private FormularAufbereitung fa;

  private DateInput ausfuehrungsdatum;

  private SelectInput ct1ausgabe;

  private TextInput verwendungszweck;

  public enum TYP
  {
    DRUCKMAIL,
    CENT1
  }

  public PreNotificationControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public TabFolder getFolder(Composite parent)
  {
    if (folder != null)
    {
      return folder;
    }
    folder = new TabFolder(parent, SWT.NONE);
    folder.setSelection(settings.getInt(settingsprefix + "tab.selection", 0));
    return folder;
  }

  public DateInput getAusfuehrungsdatum()
  {
    if (ausfuehrungsdatum != null)
    {
      return ausfuehrungsdatum;
    }
    ausfuehrungsdatum = new DateInput();
    ausfuehrungsdatum.setName("Ausführungsdatum");
    return ausfuehrungsdatum;
  }

  public SelectInput getct1Ausgabe()
  {
    if (ct1ausgabe != null)
    {
      return ct1ausgabe;
    }
    Ct1Ausgabe aus = Ct1Ausgabe.getByKey(settings
        .getInt(settingsprefix + "ct1ausgabe", Ct1Ausgabe.SEPA_DATEI.getKey()));
    if (aus != Ct1Ausgabe.SEPA_DATEI && aus != Ct1Ausgabe.HIBISCUS)
    {
      aus = Ct1Ausgabe.HIBISCUS;
    }
    ct1ausgabe = new SelectInput(Ct1Ausgabe.values(), aus);
    ct1ausgabe.setName("Ausgabe");
    return ct1ausgabe;
  }

  public TextInput getVerwendungszweck()
  {
    if (verwendungszweck != null)
    {
      return verwendungszweck;
    }
    verwendungszweck = new TextInput(
        settings.getString("verwendungszweck", ""));
    verwendungszweck.setName("Verwendungszweck");
    return verwendungszweck;
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
          saveDruckMailSettings();

          String val = (String) getOutput().getValue();
          String pdfMode = (String) getPdfModus().getValue();

          settings.setAttribute(settingsprefix + "tab.selection",
              folder.getSelectionIndex());

          boolean mitMail = true;
          if (val.equals(PDF1))
          {
            mitMail = false;
          }

          if (val.equals(PDF1) || val.equals(PDF2))
          {
            generierePDF(getLastschriften(currentObject, mitMail), pdfMode);
          }
          if (val.equals(EMAIL))
          {
            generiereEMail(getLastschriften(currentObject, true));
          }
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
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

  public Button getStart1ctUeberweisungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();

          Ct1Ausgabe aa = (Ct1Ausgabe) ct1ausgabe.getValue();
          settings.setAttribute(settingsprefix + "ct1ausgabe", aa.getKey());
          if (ausfuehrungsdatum.getValue() == null)
          {
            GUI.getStatusBar().setErrorText("Ausführungsdatum fehlt");
            return;
          }
          Date d = (Date) ausfuehrungsdatum.getValue();
          settings.setAttribute(settingsprefix + "faelligkeitsdatum",
              new JVDateFormatDATETIME().format(d));
          settings.setAttribute(settingsprefix + "verwendungszweck",
              (String) getVerwendungszweck().getValue());
          settings.setAttribute(settingsprefix + "tab.selection",
              folder.getSelectionIndex());
          generiere1ct(getLastschriften(currentObject, true));
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
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

  private void generierePDF(List<Lastschrift> lastschriften, String pdfMode)
      throws IOException, ApplicationException
  {
    boolean einzelnePdfs = false;
    if (pdfMode.equals(EINZELN))
    {
      einzelnePdfs = true;
    }

    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(
        VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_DATEINAME) + ".pdf");
    fd.setFilterExtensions(new String[] { "*.pdf" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".pdf"))
    {
      s = s + ".pdf";
    }
    final File file = new File(s);
    String lastdir = file.getParent();
    settings.setAttribute("lastdir", lastdir);
    Formular form = (Formular) getFormular(FormularArt.SEPA_PRENOTIFICATION)
        .getValue();
    if (form == null)
    {
      throw new IOException("kein SEPA Pre-Notification-Formular ausgewählt");
    }
    Formular fo = (Formular) Einstellungen.getDBService()
        .createObject(Formular.class, form.getID());
    if (!einzelnePdfs)
    {
      fa = new FormularAufbereitung(file, false, false);
    }

    for (Lastschrift ls : lastschriften)
    {
      if (einzelnePdfs)
      {
        final File fx = new File(lastdir + File.separator
            + VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_MITGLIED_DATEINAME,
                ls, ls.getMitglied())
            + ".pdf");
        fa = new FormularAufbereitung(fx, false, false);
      }

      aufbereitenFormular(ls, fo);

      if (einzelnePdfs)
      {
        fa.closeFormular();
      }
    }

    fa.showFormular();

  }

  private void generiere1ct(ArrayList<Lastschrift> lastschriften)
      throws Exception
  {

    File file = null;
    Ct1Ausgabe aa = Ct1Ausgabe.getByKey(settings
        .getInt(settingsprefix + "ct1ausgabe", Ct1Ausgabe.SEPA_DATEI.getKey()));
    if (aa == Ct1Ausgabe.SEPA_DATEI)
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("SEPA-Ausgabedatei wählen.");
      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(
          VorlageUtil.getName(VorlageTyp.CT1_AUSGABE_DATEINAME) + ".xml");
      fd.setFilterExtensions(new String[] { "*.xml" });

      String s = fd.open();
      if (s == null || s.length() == 0)
      {
        return;
      }
      settings.setAttribute(settingsprefix + "ausgabedateiname", s);
      if (!s.toLowerCase().endsWith(".xml"))
      {
        s = s + ".xml";
      }
      file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
    }
    String faelligkeitsdatum = settings
        .getString(settingsprefix + "faelligkeitsdatum", null);
    Date faell = Datum.toDate(faelligkeitsdatum);
    Ct1Ausgabe ct1ausgabe = Ct1Ausgabe.getByKey(settings
        .getInt(settingsprefix + "ct1ausgabe", Ct1Ausgabe.SEPA_DATEI.getKey()));
    String verwendungszweck = settings
        .getString(settingsprefix + "verwendungszweck", "");
    Ct1Ueberweisung ct1ueberweisung = new Ct1Ueberweisung();
    int anzahl = ct1ueberweisung.write(lastschriften, file, faell, ct1ausgabe,
        verwendungszweck);
    GUI.getStatusBar().setSuccessText("Anzahl Überweisungen: " + anzahl);
  }

  private void generiereEMail(ArrayList<Lastschrift> lastschriften)
      throws IOException
  {
    String betr = (String) getBetreff().getValue();
    String text = (String) getTxt().getValue();
    sendeMail(lastschriften, betr, text);
  }

  private void aufbereitenFormular(Lastschrift ls, Formular fo)
      throws RemoteException, ApplicationException
  {
    Map<String, Object> map = new LastschriftMap().getMap(ls, null);
    map = new AllgemeineMap().getMap(map);
    fa.writeForm(fo, map);
    fo.store();
  }

  private void sendeMail(final ArrayList<Lastschrift> lastschriften,
      final String betr, String text) throws RemoteException
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

          Velocity.init();
          Logger.debug("preparing velocity context");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          int sentCount = 0;
          int zae = 0;
          int size = lastschriften.size();
          for (Lastschrift ls : lastschriften)
          {
            if (ls.getEmail() == null || ls.getEmail().isEmpty())
            {
              continue;
            }
            if (isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Mailversand abgebrochen");
              monitor.setPercentComplete(100);
              return;
            }

            VelocityContext context = new VelocityContext();
            context.put("dateformat", new JVDateFormatTTMMJJJJ());
            context.put("decimalformat", Einstellungen.DECIMALFORMAT);
            context.put("email", ls.getEmail());

            Map<String, Object> map = new AllgemeineMap().getMap(null);
            map = new LastschriftMap().getMap(ls, map);
            boolean ohneLesefelder = !(betr + txt)
                .contains(Einstellungen.LESEFELD_PRE);
            map = new MitgliedMap().getMap(ls.getMitglied(), map,
                ohneLesefelder);
            VarTools.add(context, map);

            StringWriter wtext1 = new StringWriter();
            Velocity.evaluate(context, wtext1, "LOG", betr);

            StringWriter wtext2 = new StringWriter();
            Velocity.evaluate(context, wtext2, "LOG", txt);

            try
            {
              try
              {
                sender.sendMail(ls.getEmail(), wtext1.getBuffer().toString(),
                    wtext2.getBuffer().toString(), new TreeSet<MailAnhang>());
              }
              // Wenn eine ApplicationException geworfen wurde, wurde die
              // Mails erfolgreich versendet, erst danach trat ein Fehler auf.
              catch (ApplicationException ae)
              {
                Logger.error("Fehler: ", ae);
                monitor.log(ls.getEmail() + " - " + ae.getMessage());
              }

              // Mail in die Datenbank schreiben
              if (ls.getMitglied() != null)
              {
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
                empf.setMitglied(ls.getMitglied());
                empf.setVersand(ts);
                empf.store();
              }

              // Als versendet markieren
              ls.setVersanddatum(new Date());
              ls.store();

              sentCount++;
              monitor.log(ls.getEmail() + " - versendet");
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Mailversand", e);
              monitor.log(ls.getEmail() + " - " + e.getMessage());
            }
            zae++;
            double proz = (double) zae / (double) size * 100d;
            monitor.setPercentComplete((int) proz);
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") + " verschickt");
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
  protected void TabRefresh()
  {
    // Nichts tun, hier ist keine Tabelle implementiert
  }

  ArrayList<Lastschrift> getLastschriften(Object currentObject, boolean mitMail)
      throws RemoteException, ApplicationException
  {
    if (currentObject == null)
    {
      if (abrechnungslaufausw != null && abrechnungslaufausw.getValue() != null)
      {
        currentObject = abrechnungslaufausw.getValue();
      }
      else
      {
        throw new ApplicationException(
            "Kein Abrechnungslauf oder keine Lastschrift ausgewählt!");
      }
    }

    ArrayList<Lastschrift> lastschriften = new ArrayList<>();
    if (currentObject instanceof Abrechnungslauf)
    {
      Abrechnungslauf abrl = (Abrechnungslauf) currentObject;
      if (abrl.getAbgeschlossen())
      {
        throw new ApplicationException(
            "Die ausgewählte Abrechnungslauf ist bereits abgeschlossen!");
      }
      DBIterator<Lastschrift> it = Einstellungen.getDBService()
          .createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      if (!mitMail)
      {
        it.addFilter("(email is null or length(email)=0)");
      }
      if (suchversand != null && suchversand.getValue() != null)
      {
        switch ((SuchVersand) suchversand.getValue())
        {
          case VERSAND:
            it.addFilter("versanddatum IS NOT NULL");
            break;
          case NICHT_VERSAND:
            it.addFilter("versanddatum IS NULL");
            break;
        }
      }

      it.setOrder("order by name, vorname");
      while (it.hasNext())
      {
        lastschriften.add((Lastschrift) it.next());
      }

      if (lastschriften.size() == 0 && !mitMail)
      {
        throw new ApplicationException(
            "Der Abrechnungslauf hat keine Lastschriften ohne Mailadresse.");
      }
      if (lastschriften.size() == 0)
      {
        throw new ApplicationException(
            "Der Abrechnungslauf hat keine Lastschriften.");
      }
    }
    else if (currentObject instanceof Lastschrift)
    {
      Lastschrift lastschrift = (Lastschrift) currentObject;
      Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
      if (abrl.getAbgeschlossen())
      {
        throw new ApplicationException(
            "Die ausgewählte Lastschrift ist bereits abgeschlossen!");
      }
      if (!mitMail && lastschrift.getEmail() != null
          && !lastschrift.getEmail().isEmpty())
      {
        throw new ApplicationException(
            "Die ausgewählte Lastschrift hat eine Mail Adresse.");
      }
      else
      {
        lastschriften.add(lastschrift);
      }
    }
    else if (currentObject instanceof Lastschrift[])
    {
      Lastschrift[] lastschriftarray = (Lastschrift[]) currentObject;
      for (Lastschrift lastschrift : lastschriftarray)
      {
        Abrechnungslauf abrl = (Abrechnungslauf) lastschrift
            .getAbrechnungslauf();
        if (abrl.getAbgeschlossen())
        {
          throw new ApplicationException(
              "Die ausgewählte Lastschrift mit der Nr " + lastschrift.getID()
                  + " ist bereits abgeschlossen!");
        }
        if (!(!mitMail && lastschrift.getEmail() != null
            && !lastschrift.getEmail().isEmpty()))
        {
          lastschriften.add(lastschrift);
        }
      }
      if (lastschriften.size() == 0)
      {
        throw new ApplicationException(
            "Alle ausgewählten Lastschriften haben eine Mail Adresse.");
      }
    }
    else
    {
      throw new ApplicationException(
          "Kein Abrechnungslauf oder keine Lastschrift ausgewählt.");
    }
    return lastschriften;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object currentObject,
      String option) throws RemoteException, ApplicationException
  {
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = null;
    int ohneMail = 0;
    String val = (String) getOutput().getValue();
    List<Lastschrift> lastschriften;
    if (option.equals(TYP.CENT1.toString()) || val.equals(EMAIL))
    {
      lastschriften = getLastschriften(currentObject, true);
    }
    else
    {
      boolean mitMail = true;
      if (val.equals(PDF1))
      {
        mitMail = false;
      }
      lastschriften = getLastschriften(currentObject, mitMail);
    }
    for (Lastschrift l : lastschriften)
    {
      String mail = l.getEmail();
      if (val.equals(EMAIL) && !option.equals(TYP.CENT1.toString()))
      {
        if (mail == null || mail.isEmpty())
        {
          ohneMail++;
        }
      }
      Mitglied m = l.getMitglied();
      String dokument = "Lastschrift über "
          + Einstellungen.DECIMALFORMAT.format(l.getBetrag()) + "€";
      if (m != null)
      {
        liste.add(new DruckMailEmpfaengerEntry(dokument, mail, m.getName(),
            m.getVorname(), m.getMitgliedstyp()));
      }
      else
      {
        liste.add(new DruckMailEmpfaengerEntry(dokument, mail, l.getName(),
            l.getVorname(), "Kursteilnehmer"));
      }
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

  public TextInput getAbrechnungslauf(AbrechnungslaufImpl lauf)
      throws RemoteException
  {
    TextInput text = new TextInput(lauf.getIDText());
    text.setName("Abrechnungslauf");
    text.disable();
    return text;
  }

}
