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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.AbbuchungsmodusInput;
import de.jost_net.JVerein.io.AbrechnungSEPA;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.util.ApplicationException;

public class AbrechnungSEPAControl extends AbstractAbrechnungControl
{

  // Inputs in Settings gespeichert
  private AbbuchungsmodusInput modus;

  private TextInput zahlungsgrund;

  private CheckboxInput zusatzbetrag;

  private CheckboxInput kursteilnehmer;

  // Inputs nicht in Settings gespeichert
  private SelectInput abrechnungsmonat;

  private DateInput vondatum;

  private DateInput bisdatum;

  private DateInput voneingabedatum;

  public AbrechnungSEPAControl() throws RemoteException
  {
    super();
  }

  // Inputs in Settings gespeichert

  public AbbuchungsmodusInput getAbbuchungsmodus() throws RemoteException
  {
    if (modus != null)
    {
      return modus;
    }
    Integer mod = settings.getInt("modus", Abrechnungsmodi.KEINBEITRAG);

    modus = new AbbuchungsmodusInput(mod);
    modus.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        Integer m = ((Integer) modus.getValue());
        if (m.intValue() != Abrechnungsmodi.EINGETRETENEMITGLIEDER)
        {
          vondatum.setValue(null);
          vondatum.setMandatory(false);
          vondatum.setEnabled(false);
          voneingabedatum.setValue(null);
          voneingabedatum.setMandatory(false);
          voneingabedatum.setEnabled(false);
        }
        else
        {
          vondatum.setValue(new Date());
          vondatum.setEnabled(true);
          vondatum.setMandatory(voneingabedatum.getValue() == null);
          voneingabedatum.setValue(new Date());
          voneingabedatum.setEnabled(true);
          voneingabedatum.setMandatory(vondatum.getValue() == null);
        }
        if (m.intValue() != Abrechnungsmodi.ABGEMELDETEMITGLIEDER)
        {
          bisdatum.setValue(null);
          bisdatum.setMandatory(false);
          bisdatum.setEnabled(false);
        }
        else
        {
          bisdatum.setValue(new Date());
          bisdatum.setEnabled(true);
          bisdatum.setMandatory(true);
        }
      }
    });
    modus.setName("Modus");
    return modus;
  }

  public TextInput getZahlungsgrund()
  {
    if (zahlungsgrund != null)
    {
      return zahlungsgrund;
    }
    String zgrund = settings.getString("zahlungsgrund", "bitte eingeben");
    zahlungsgrund = new TextInput(zgrund, 140);
    zahlungsgrund.setName("Zahlungsgrund für Beiträge");
    return zahlungsgrund;
  }

  public CheckboxInput getZusatzbetrag()
  {
    if (zusatzbetrag != null)
    {
      return zusatzbetrag;
    }
    zusatzbetrag = new CheckboxInput(
        settings.getBoolean("zusatzbetraege", false));
    return zusatzbetrag;
  }

  public boolean isZusatzbetragSupported()
  {
    return zusatzbetrag != null;
  }

  public CheckboxInput getKursteilnehmer()
  {
    if (kursteilnehmer != null)
    {
      return kursteilnehmer;
    }
    kursteilnehmer = new CheckboxInput(
        settings.getBoolean("kursteilnehmer", false));
    return kursteilnehmer;
  }

  public boolean isKursteilnehmerSupported()
  {
    return kursteilnehmer != null;
  }

  // Inputs nicht in Settings gespeichert

  public SelectInput getAbrechnungsmonat()
  {
    if (abrechnungsmonat != null)
    {
      return abrechnungsmonat;
    }
    abrechnungsmonat = new SelectInput(Monat.values(), Monat.JANUAR);
    abrechnungsmonat.setName("Abrechnungsmonat");
    return abrechnungsmonat;
  }

  public boolean isAbrechnungsmonatSupported()
  {
    return abrechnungsmonat != null;
  }

  public DateInput getVondatum()
  {
    if (vondatum != null)
    {
      return vondatum;
    }
    vondatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    vondatum.setTitle("Anfangsdatum Abrechnung");
    vondatum.setText("Bitte Anfangsdatum der Abrechnung wählen");
    boolean mode = (Integer) modus
        .getValue() == Abrechnungsmodi.EINGETRETENEMITGLIEDER;
    if (mode)
    {
      vondatum.setValue(new Date());
    }
    vondatum.setEnabled(mode);
    vondatum.addListener(event -> {
      if (vondatum.hasChanged())
      {
        vondatum.setMandatory(voneingabedatum.getValue() == null);
        voneingabedatum.setMandatory(vondatum.getValue() == null);
      }
    });
    vondatum.hasChanged();
    vondatum.setName("Von Eintrittsdatum");
    return vondatum;
  }

  public boolean isVondatumSupported()
  {
    return vondatum != null;
  }

  public DateInput getVonEingabedatum()
  {
    if (voneingabedatum != null)
    {
      return voneingabedatum;
    }
    voneingabedatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    boolean mode = (Integer) modus
        .getValue() == Abrechnungsmodi.EINGETRETENEMITGLIEDER;
    if (mode)
    {
      voneingabedatum.setValue(new Date());
    }
    voneingabedatum.setMandatory(mode && this.vondatum.getValue() == null);
    voneingabedatum.setEnabled(mode);
    // Das kann erst gemacht werden wenn voneingabedatum existiert
    vondatum.setMandatory(mode && this.voneingabedatum.getValue() == null);
    voneingabedatum.addListener(event -> {
      if (voneingabedatum.hasChanged())
      {
        vondatum.setMandatory(voneingabedatum.getValue() == null);
        voneingabedatum.setMandatory(vondatum.getValue() == null);
      }
    });
    voneingabedatum.hasChanged();
    voneingabedatum.setName("Von Eingabedatum");
    return voneingabedatum;
  }

  public boolean isVoneingabgedatumSupported()
  {
    return voneingabedatum != null;
  }

  public DateInput getBisdatum()
  {
    if (bisdatum != null)
    {
      return bisdatum;
    }
    bisdatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    bisdatum.setTitle("Enddatum Abrechnung");
    bisdatum
        .setText("Bitte maximales Austrittsdatum für die Abrechnung wählen");
    boolean mode = (Integer) modus
        .getValue() == Abrechnungsmodi.ABGEMELDETEMITGLIEDER;
    if (mode)
    {
      bisdatum.setValue(new Date());
    }
    bisdatum.setMandatory(mode);
    bisdatum.setEnabled(mode);
    bisdatum.setName("Bis Austrittsdatum");
    return bisdatum;
  }

  public boolean isBisdatumSupported()
  {
    return bisdatum != null;
  }

  @Override
  public List<Bug> getBugs()
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    try
    {
      boolean isLastschrift = false;
      AbrechnungSEPAParam param = new AbrechnungSEPAParam(this, null);

      // Stichtag ist beim Öffnen des Dialogs nicht gesetzt und es wird kein
      // checkInput gemacht
      if (param.stichtag == null)
      {
        bugs.add(new Bug(null, "Stichtag fehlt", Bug.ERROR));
        return bugs;
      }

      // Prüfen ob das Verrechnungskonto gesetzt ist. Das wird auch beim
      // Abrechnungslauf am Anfang geholt.
      checkVerrechnungskonto(bugs);

      // Bei allen Mitgliedern die abgerechnet werden das Beitragsmodell prüfen.
      // Falls der Zahler mit Basislastschrift zahlt wird dabei auch der SEPA
      // Check durchgeführt.
      if ((Integer) getAbbuchungsmodus()
          .getValue() != Abrechnungsmodi.KEINBEITRAG)
      {
        Double betrag;
        DBIterator<Mitglied> it2 = AbrechnungSEPA
            .getAbrechnenMitgliederIt(param);
        while (it2.hasNext())
        {
          boolean mitgliedChecked = false;
          betrag = null;
          Mitglied m = it2.next();
          Mitglied zahler = m.getZahler();
          if (!checkMitgliedBeitraege(m, bugs))
          {
            continue;
          }
          try
          {
            betrag = AbrechnungSEPA.getBetrag(m, true, m.getBeitragsgruppe(),
                param);
            if (betrag != null
                && zahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
            {
              checkMitgliedKontodaten(zahler, bugs);
              if (!param.sepacheckdisable)
              {
                checkSEPA(zahler, bugs);
              }
              mitgliedChecked = true;
              isLastschrift = true;
            }
            DBIterator<SekundaereBeitragsgruppe> sekundaer = Einstellungen
                .getDBService().createList(SekundaereBeitragsgruppe.class);
            sekundaer.addFilter("mitglied=?", m.getID());
            while (sekundaer.hasNext())
            {
              SekundaereBeitragsgruppe sb = sekundaer.next();
              betrag = AbrechnungSEPA.getBetrag(m, false,
                  sb.getBeitragsgruppe(), param);
              if (!mitgliedChecked && betrag != null
                  && zahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
              {
                checkMitgliedKontodaten(zahler, bugs);
                if (!param.sepacheckdisable)
                {
                  checkSEPA(zahler, bugs);
                }
                mitgliedChecked = true;
                isLastschrift = true;
              }
            }
          }
          catch (ApplicationException ex)
          {
            bugs.add(new Bug(m, ex.getMessage(), Bug.ERROR));
          }
        }
      }

      // Zusatzbeträge prüfen
      if (param.zusatzbetraege)
      {
        List<Zusatzbetrag> zusatzbetraege = AbrechnungSEPA
            .getAbrechnenZusatzbetragList(param);
        Date stichtag = param.stichtag != null ? param.stichtag : new Date();

        for (Zusatzbetrag z : zusatzbetraege)
        {
          if (z.isAktiv(stichtag))
          {
            Mitglied m = z.getMitglied();
            Mitglied mZahler;
            if (z.getMitgliedzahltSelbst())
            {
              mZahler = m;
            }
            else
            {
              mZahler = m.getZahler();
            }
            Integer zahlungsweg;
            if (z.getZahlungsweg() != null
                && z.getZahlungsweg().getKey() != Zahlungsweg.STANDARD)
            {
              zahlungsweg = z.getZahlungsweg().getKey();
            }
            else
            {
              zahlungsweg = mZahler.getZahlungsweg();
            }

            if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT)
            {
              isLastschrift = true;
              checkMitgliedKontodaten(mZahler, bugs);
              if (!param.sepacheckdisable)
              {
                checkSEPA(mZahler, bugs);
              }
            }
          }
        }
      }

      // Kursteilnehmer prüfen
      if (param.kursteilnehmer)
      {
        DBIterator<Kursteilnehmer> list = AbrechnungSEPA
            .getAbrechnenKursteilnehmerIt(param);
        if (list.size() > 0)
        {
          isLastschrift = true;
        }
      }

      if (isLastschrift)
      {
        // Bei Lastschrift auch allgemeine Daten prüfen weil z.B. dann
        // Verrechnungskonto und Vereinskontodaten gebraucht werden.
        checkGlobal(bugs);
        checkFaelligkeit((Date) getFaelligkeit().getValue(), bugs);
      }

      if (bugs.isEmpty())
      {
        bugs.add(new Bug(null, KEINFEHLER, Bug.HINT));
      }
    }
    catch (Exception ex)
    {
      bugs.add(new Bug(null, ex.getMessage(), Bug.ERROR));
    }
    return bugs;
  }

  @Override
  protected String checkInput()
  {
    try
    {
      Integer modus = null;
      try
      {
        modus = (Integer) getAbbuchungsmodus().getValue();
      }
      catch (RemoteException e)
      {
        return ("Interner Fehler - kann Abrechnungsmodus nicht auslesen");
      }
      if (getFaelligkeit().getValue() == null)
      {
        return ("Fälligkeitsdatum fehlt");
      }
      if (getStichtag().getValue() == null)
      {
        return ("Stichtag fehlt");
      }
      if (modus == Abrechnungsmodi.EINGETRETENEMITGLIEDER)
      {
        if (vondatum == null || voneingabedatum == null
            || (vondatum.getValue() == null
                && voneingabedatum.getValue() == null))
        {
          return ("Von-Datum fehlt");
        }
      }
      if (modus == Abrechnungsmodi.ABGEMELDETEMITGLIEDER)
      {
        if (bisdatum == null || bisdatum.getValue() == null)
        {
          return ("Bis-Datum fehlt");
        }
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        if ((Boolean) getRechnung().getValue())
        {
          if (getRechnungFormular().getValue() == null)
          {
            return ("Rechnungsformular fehlt");
          }
          if (getRechnungsdatum().getValue() == null)
          {
            return ("Rechnungsdatum fehlt");
          }
        }
      }
    }
    catch (RemoteException re)
    {
      return ("Fehler beim Auswerten der Eingabe!");
    }
    return null;
  }

  @Override
  protected void saveSettings()
  {
    super.saveSettings();

    settings.setAttribute("modus", (Integer) modus.getValue());
    settings.setAttribute("zahlungsgrund", (String) zahlungsgrund.getValue());
    if (zusatzbetrag != null)
    {
      settings.setAttribute("zusatzbetraege",
          (Boolean) zusatzbetrag.getValue());
    }
    if (kursteilnehmer != null)
    {
      settings.setAttribute("kursteilnehmer",
          (Boolean) kursteilnehmer.getValue());
    }
  }

  @Override
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    return new AbrechnungSEPAParam(this, sepaVersion);
  }
}
