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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.io.PreNotificationAusgabe;
import de.jost_net.JVerein.io.Ueberweisung;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.AbrechnungslaufImpl;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class PreNotificationControl extends DruckMailControl
{

  private TabFolder folder = null;

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
    UeberweisungAusgabe aus = UeberweisungAusgabe
        .getByKey(settings.getInt(settingsprefix + "ct1ausgabe",
            UeberweisungAusgabe.SEPA_DATEI.getKey()));
    if (aus != UeberweisungAusgabe.SEPA_DATEI
        && aus != UeberweisungAusgabe.HIBISCUS)
    {
      aus = UeberweisungAusgabe.HIBISCUS;
    }
    ct1ausgabe = new SelectInput(UeberweisungAusgabe.values(), aus);
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
          saveFilterSettings();

          new PreNotificationAusgabe((Formular) PreNotificationControl.this
              .getFormular(null).getValue()).aufbereiten(
                  getLastschriften(currentObject),
                  (Ausgabeart) getAusgabeart().getValue(), getBetreffString(),
                  getTxtString(), false, false);
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

  @Override
  public void saveFilterSettings() throws RemoteException
  {
    settings.setAttribute(settingsprefix + "tab.selection",
        folder.getSelectionIndex());

    settings.setAttribute(settingsprefix + "ct1ausgabe",
        ((UeberweisungAusgabe) ct1ausgabe.getValue()).getKey());

    if (ausfuehrungsdatum != null && ausfuehrungsdatum.getValue() != null)
    {
      settings.setAttribute(settingsprefix + "faelligkeitsdatum",
          new JVDateFormatDATETIME()
              .format((Date) ausfuehrungsdatum.getValue()));
    }

    settings.setAttribute(settingsprefix + "verwendungszweck",
        (String) getVerwendungszweck().getValue());

    super.saveFilterSettings();
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
          saveFilterSettings();

          generiere1ct(getLastschriften(currentObject));
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

  private void generiere1ct(ArrayList<Lastschrift> lastschriften)
      throws Exception
  {
    if (ausfuehrungsdatum.getValue() == null)
    {
      GUI.getStatusBar().setErrorText("Ausführungsdatum fehlt");
      return;
    }

    File file = null;
    UeberweisungAusgabe aa = UeberweisungAusgabe
        .getByKey(settings.getInt(settingsprefix + "ct1ausgabe",
            UeberweisungAusgabe.SEPA_DATEI.getKey()));
    if (aa == UeberweisungAusgabe.SEPA_DATEI)
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
    UeberweisungAusgabe ct1ausgabe = UeberweisungAusgabe
        .getByKey(settings.getInt(settingsprefix + "ct1ausgabe",
            UeberweisungAusgabe.SEPA_DATEI.getKey()));
    String verwendungszweck = settings
        .getString(settingsprefix + "verwendungszweck", "");
    Ueberweisung ct1ueberweisung = new Ueberweisung(0.01d);
    int anzahl = ct1ueberweisung.write(lastschriften, file, faell, ct1ausgabe,
        verwendungszweck);
    GUI.getStatusBar().setSuccessText("Anzahl Überweisungen: " + anzahl);
  }

  @Override
  protected void TabRefresh()
  {
    // Nichts tun, hier ist keine Tabelle implementiert
  }

  ArrayList<Lastschrift> getLastschriften(Object currentObject)
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
      if (isMailauswahlAktiv())
      {
        int mailauswahl = (Integer) getMailauswahl().getValue();
        if (mailauswahl == MailAuswertungInput.OHNE)
        {
          it.addFilter("(email is null or length(email) = 0)");
        }
        if (mailauswahl == MailAuswertungInput.MIT)
        {
          it.addFilter("(email is  not null and length(email) > 0)");
        }
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

      if (lastschriften.size() == 0)
      {
        throw new ApplicationException(
            "Für die gewählten Filterkriterien wurde keine Lastschrift gefunden.");
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
      lastschriften.add(lastschrift);
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
        lastschriften.add(lastschrift);
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
  public String getInfoText(Object selection) throws RemoteException
  {
    Lastschrift[] lastschrift = null;
    String text = "";

    if (selection instanceof Lastschrift)
    {
      lastschrift = new Lastschrift[] { (Lastschrift) selection };
    }
    else if (selection instanceof Lastschrift[])
    {
      lastschrift = (Lastschrift[]) selection;
    }
    else
    {
      return "";
    }

    if (lastschrift != null)
    {
      text = "Es wurden " + lastschrift.length + " Lastschriften ausgewählt";
      String fehlen = "";
      for (Lastschrift l : lastschrift)
      {
        if (l.getEmail() == null || l.getEmail().isEmpty())
        {
          fehlen = fehlen + "\n - " + l.getName() + ", " + l.getVorname();
        }
      }
      if (fehlen.length() > 0)
      {
        text += "\nFolgende Lastschriften haben keine Mailadresse:" + fehlen;
      }
    }
    return text;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object currentObject,
      String option) throws RemoteException, ApplicationException
  {
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = null;
    int ohneMail = 0;
    List<Lastschrift> lastschriften = getLastschriften(currentObject);
    for (Lastschrift l : lastschriften)
    {
      String mail = l.getEmail();
      if (ausgabeart.getValue() == Ausgabeart.MAIL
          && !option.equals(TYP.CENT1.toString())
          && (mail == null || mail.isEmpty()))
      {
        ohneMail++;
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
      text = ohneMail + " Lastschrift hat keine Mail Adresse.";
    }
    else if (ohneMail > 1)
    {
      text = ohneMail + " Lastschriften haben keine Mail Adresse.";
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
