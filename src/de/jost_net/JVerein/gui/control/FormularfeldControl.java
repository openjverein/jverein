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
import java.util.Collections;

import com.itextpdf.text.pdf.BaseFont;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineVar;
import de.jost_net.JVerein.Variable.LastschriftVar;
import de.jost_net.JVerein.Variable.MitgliedVar;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularfeldControl extends FormularPartControl
    implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private SelectInput name;

  private IntegerInput seite;

  private DecimalInput x;

  private DecimalInput y;

  private SelectInput font;

  private IntegerInput fontsize;

  private Formularfeld formularfeld;

  private TextInput formularTyp;

  private TextInput formularName;

  public static final String EMPFAENGER = "Empfänger";

  public static final String TAGESDATUM = "Tagesdatum";

  public static final String TAGESDATUMTT = "Tagesdatum TT";

  public static final String TAGESDATUMMM = "Tagesdatum MM";

  public static final String TAGESDATUMJJJJ = "Tagesdatum JJJJ";

  public static final String ZAHLUNGSWEG = "Zahlungsweg";

  public static final String ID = "ID";

  public static final String EXTERNEMITGLIEDSNUMMER = "externe Mitgliedsnummer";

  public static final String ANREDE = "Anrede";

  public static final String TITEL = "Titel";

  public static final String NAME = "Name";

  public static final String VORNAME = "Vorname";

  public static final String ADRESSIERUNGSZUSATZ = "Adressierungszusatz";

  public static final String STRASSE = "Strasse";

  public static final String PLZ = "PLZ";

  public static final String ORT = "Ort";

  public static final String STAAT = "Staat";

  @Deprecated
  public static final String ZAHLUNGSRHYTMUS = "Zahlungsrhytmus";

  public static final String ZAHLUNGSRHYTHMUS = "Zahlungsrhythmus";

  public static final String KONTOINHABER = "Kontoinhaber";

  public static final String GEBURTSDATUM = "Geburtsdatum";

  public static final String GESCHLECHT = "Geschlecht";

  public static final String TELEFONPRIVAT = "Telefon privat";

  public static final String TELEFONDIENSTLICH = "Telefon dienstlich";

  public static final String HANDY = "Handy";

  public static final String EMAIL = "Email";

  public static final String EINTRITT = "Eintritt";

  public static final String BEITRAGSGRUPPE = "Beitragsgruppe";

  public static final String AUSTRITT = "Austritt";

  public static final String KUENDIGUNG = "Kündigung";
  
  public static final String ZAEHLER = "Fortlaufende Nummer des Formulars";

  public FormularfeldControl(AbstractView view, Formular formular)
  {
    super(view, formular);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Input getFormularTyp() throws RemoteException
  {
    if (null == formularTyp)
    {
      formularTyp = new TextInput(getFormularArtName());
      formularTyp.disable();
    }
    return formularTyp;
  }

  public Input getFormularName() throws RemoteException
  {
    if (null == formularName)
    {
      formularName = new TextInput(formular.getBezeichnung());
      formularName.disable();
    }
    return formularName;
  }

  public Formularfeld getFormularfeld()
  {
    if (formularfeld != null)
    {
      return formularfeld;
    }
    formularfeld = (Formularfeld) getCurrentObject();
    return formularfeld;
  }

  public Formular getFormular()
  {
    return formular;
  }

  public SelectInput getName()
      throws RemoteException, NoSuchFieldException, SecurityException
  {
    if (name != null)
    {
      return name;
    }
    ArrayList<String> namen = new ArrayList<>();
    if (formular.getArt() == FormularArt.SPENDENBESCHEINIGUNG)
    {
      for (AllgemeineVar av : AllgemeineVar.values())
      {
        namen.add(av.getName());
      }
      for (SpendenbescheinigungVar spv : SpendenbescheinigungVar.values())
      {
        namen.add(spv.getName());
      }
      for (MitgliedVar mv : MitgliedVar.values())
      {
        namen.add(mv.getName());
      }
    }
    if (formular.getArt() == FormularArt.SAMMELSPENDENBESCHEINIGUNG)
    {
      for (AllgemeineVar av : AllgemeineVar.values())
      {
        namen.add(av.getName());
      }
      for (SpendenbescheinigungVar spv : SpendenbescheinigungVar.values())
      {
        namen.add(spv.getName());
      }
      for (MitgliedVar mv : MitgliedVar.values())
      {
        namen.add(mv.getName());
      }
    }
    if (formular.getArt() == FormularArt.FREIESFORMULAR)
    {
      for (AllgemeineVar av : AllgemeineVar.values())
      {
        namen.add(av.getName());
      }
      for (MitgliedVar mv : MitgliedVar.values())
      {
        namen.add(mv.getName());
      }
    }
    if (formular.getArt() == FormularArt.SEPA_PRENOTIFICATION)
    {
      for (AllgemeineVar av : AllgemeineVar.values())
      {
        namen.add(av.getName());
      }
      for (LastschriftVar lsv : LastschriftVar.values())
      {
        namen.add(lsv.getName());
      }
    }
    if (formular.getArt() == FormularArt.RECHNUNG
        || formular.getArt() == FormularArt.MAHNUNG)
    {
      for (AllgemeineVar av : AllgemeineVar.values())
      {
        namen.add(av.getName());
      }
      for (MitgliedVar mv : MitgliedVar.values())
      {
        namen.add(mv.getName());
      }
      for (RechnungVar mkv : RechnungVar.values())
      {
        if (!RechnungVar.class.getField(mkv.name())
            .isAnnotationPresent(Deprecated.class))
        {
          namen.add(mkv.getName());
        }
      }

    }
    if (formular.getArt() == FormularArt.FREIESFORMULAR
        || formular.getArt() == FormularArt.RECHNUNG
        || formular.getArt() == FormularArt.MAHNUNG
        || formular.getArt() == FormularArt.SPENDENBESCHEINIGUNG
        || formular.getArt() == FormularArt.SAMMELSPENDENBESCHEINIGUNG)
    {
      DBIterator<Lesefeld> itlesefelder = Einstellungen.getDBService()
          .createList(Lesefeld.class);
      while (itlesefelder.hasNext())
      {
        Lesefeld lesefeld = itlesefelder.next();
        namen.add(Einstellungen.LESEFELD_PRE + lesefeld.getBezeichnung());
      }

      DBIterator<Felddefinition> zusatzfelder = Einstellungen.getDBService()
          .createList(Felddefinition.class);
      while (zusatzfelder.hasNext())
      {
        Felddefinition zusatzfeld = (Felddefinition) zusatzfelder.next();
        namen.add(Einstellungen.ZUSATZFELD_PRE + zusatzfeld.getName());
      }
    }
    Collections.sort(namen);
    name = new SelectInput(namen, getFormularfeld().getName());
    return name;
  }

  public IntegerInput getSeite() throws RemoteException
  {
    if (seite != null)
    {
      return seite;
    }
    seite = new IntegerInput(getFormularfeld().getSeite());
    seite.setComment("Seite");
    return seite;
  }

  public DecimalInput getX() throws RemoteException
  {
    if (x != null)
    {
      return x;
    }
    x = new DecimalInput(getFormularfeld().getX(), Einstellungen.DECIMALFORMAT);
    x.setComment("Millimeter");
    return x;
  }

  public DecimalInput getY() throws RemoteException
  {
    if (y != null)
    {
      return y;
    }
    y = new DecimalInput(getFormularfeld().getY(), Einstellungen.DECIMALFORMAT);
    y.setComment("Millimeter");
    return y;
  }

  public SelectInput getFont() throws RemoteException
  {
    if (font != null)
    {
      return font;
    }
    ArrayList<String> fonts = new ArrayList<>();
    fonts.add("PTSans-Regular");
    fonts.add("PTSans-Bold");
    fonts.add("PTSans-Italic");
    fonts.add("PTSans-BoldItalic");
    fonts.add("FreeSans");
    fonts.add("FreeSans-Bold");
    fonts.add("FreeSans-BoldOblique");
    fonts.add("FreeSans-Oblique");
    fonts.add(BaseFont.HELVETICA);
    fonts.add(BaseFont.HELVETICA_BOLD);
    fonts.add(BaseFont.HELVETICA_BOLDOBLIQUE);
    fonts.add(BaseFont.HELVETICA_OBLIQUE);
    fonts.add(BaseFont.TIMES_ROMAN);
    fonts.add(BaseFont.TIMES_BOLD);
    fonts.add(BaseFont.TIMES_ITALIC);
    fonts.add(BaseFont.TIMES_BOLDITALIC);
    fonts.add(BaseFont.COURIER);
    fonts.add(BaseFont.COURIER_BOLD);
    fonts.add(BaseFont.COURIER_OBLIQUE);
    fonts.add(BaseFont.COURIER_BOLDOBLIQUE);
    font = new SelectInput(fonts, getFormularfeld().getFont());
    return font;
  }

  public IntegerInput getFontsize() throws RemoteException
  {
    if (fontsize != null)
    {
      return fontsize;
    }
    fontsize = new IntegerInput(getFormularfeld().getFontsize());
    return fontsize;
  }

  @Override
  public void prepareStore() throws RemoteException, ApplicationException
  {
    try
    {
      Formularfeld f = getFormularfeld();
      f.setFormular(getFormular());
      f.setName((String) getName().getValue());
      f.setSeite((Integer) getSeite().getValue());
      f.setX((Double) getX().getValue());
      f.setY((Double) getY().getValue());
      f.setFont((String) getFont().getValue());
      f.setFontsize((Integer) getFontsize().getValue());
    }
    catch (RemoteException e)
    {
      throw new RemoteException(e.getMessage());
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore();
      Formularfeld f = getFormularfeld();
      f.store();
      GUI.getStatusBar().setSuccessText("Formularfeld gespeichert");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern des Formularfeldes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }

  private String getFormularArtName() throws RemoteException
  {
    return formular.getArt().getText();
  }

}
