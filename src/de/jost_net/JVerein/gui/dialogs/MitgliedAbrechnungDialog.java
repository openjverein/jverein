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
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.input.AbbuchungsmodusInput.AbbuchungsmodusObject;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class MitgliedAbrechnungDialog extends AbstractDialog<Boolean>
{
  private boolean fortfahren = false;

  private LabelInput status = null;

  private CheckboxInput vorlageSpeichernInput;

  private Mitglied[] mitglieder;

  private boolean EinstellungRechnungAnzeigen = false;

  private boolean EinstellungBuchungsklasseInBuchung = false;

  private boolean EinstellungSteuerInBuchung = false;

  private boolean EinstellungSpeicherungAnzeigen = false;

  private Settings settings = null;

  final AbrechnungSEPAControl control = new AbrechnungSEPAControl(null);

  /**
   * @param position
   */
  public MitgliedAbrechnungDialog(int position, Mitglied[] m)
  {
    super(position);
    super.setSize(950, SWT.DEFAULT);
    setTitle("Forderung erstellen");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.mitglieder = m;
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    EinstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    EinstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    EinstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    EinstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);

    Zusatzbetrag zusatzb = getZusatzbetrag();
    ZusatzbetragPart part = new ZusatzbetragPart(zusatzb, false);

    left.addHeadline("Forderung");
    left.addLabelPair("Fälligkeit ", control.getFaelligkeit());
    left.addLabelPair("Zahlungsgrund", part.getBuchungstext());
    left.addLabelPair("Betrag", part.getBetrag());
    left.addLabelPair("Buchungsart", part.getBuchungsart());
    if (EinstellungBuchungsklasseInBuchung)
    {
      left.addLabelPair("Buchungsklasse", part.getBuchungsklasse());
    }
    if (EinstellungSteuerInBuchung)
    {
      left.addLabelPair("Steuer", part.getSteuer());
    }
    left.addLabelPair("Zahlungsweg", part.getZahlungsweg());
    left.addLabelPair("Mitglied zahlt selbst", part.getMitgliedzahltSelbst());
    left.addHeadline("Vorlagen");
    left.addLabelPair("Als Vorlage speichern", getVorlageSpeichern());

    right.addHeadline("Sollbuchungen");
    right.addLabelPair("Sollbuchung(en) zusammenfassen",
        control.getSollbuchungenZusammenfassen());

    right.addHeadline("Lastschriften");
    right.addLabelPair("Kompakte Abbuchung(en)",
        control.getKompakteAbbuchung());
    right.addLabelPair("SEPA-Check temporär deaktivieren",
        control.getSEPACheck());
    right.addLabelPair("Lastschrift-PDF erstellen", control.getSEPAPrint());
    right.addLabelPair("Abbuchungsausgabe", control.getAbbuchungsausgabe());

    if (EinstellungRechnungAnzeigen)
    {
      right.addHeadline("Rechnungen");
      right.addLabelPair("Rechnung(en) erstellen", control.getRechnung());
      if (EinstellungSpeicherungAnzeigen)
      {
        right.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getRechnungsdokumentSpeichern());
      }
      right.addLabelPair("Rechnungsformular", control.getRechnungFormular());
      right.addLabelPair("Rechnungstext", control.getRechnungstext());
      right.addLabelPair("Rechnungsdatum", control.getRechnungsdatum());
    }

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = MitgliedMap.getDummyMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINMAL_ABRECHNUNG, false, "question-circle.png");

    buttons.addButton("Zahlungsgrund Variablen anzeigen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    buttons.addButton("Rechnungstext Variablen anzeigen",
        new RechnungVariableDialogAction(part), null, false, "bookmark.png");

    buttons.addButton("Vorlagen", new ZusatzbetragVorlageAuswahlAction(part),
        null, false, "view-refresh.png");
    buttons.addButton("Abrechnen", context -> {
      try
      {
        if (!checkInput(part))
        {
          return;
        }
        saveSettings(part);

        control.getZahlungsgrund()
            .setValue((String) part.getBuchungstext().getValue());
        control.getAbbuchungsmodus()
            .setValue(new AbbuchungsmodusObject(Abrechnungsmodi.FORDERUNG));
        control.startZusatzbetragAbrechnung(getZusatzbetraegeList(part));
        fortfahren = true;
        close();
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
      catch (RemoteException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
        Logger.error("Fehler", e);
      }
    }, null, false, "ok.png");

    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return fortfahren;
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  private class RechnungVariableDialogAction implements Action
  {
    private ZusatzbetragPart part;

    public RechnungVariableDialogAction(ZusatzbetragPart part)
    {
      this.part = part;
    }

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        control.getZahlungsgrund()
            .setValue((String) part.getBuchungstext().getValue());

        Map<String, Object> rmap = new AllgemeineMap().getMap(null);
        rmap = new AbrechnungsParameterMap()
            .getMap(new AbrechnungSEPAParam(control, null, null, null), rmap);
        rmap = MitgliedMap.getDummyMap(rmap);
        rmap = RechnungMap.getDummyMap(rmap);
        new InsertVariableDialogAction(rmap).handleAction(null);
      }
      catch (RemoteException re)
      {
        //
      }
    }
  }

  public CheckboxInput getVorlageSpeichern()
  {
    if (vorlageSpeichernInput != null)
    {
      return vorlageSpeichernInput;
    }
    vorlageSpeichernInput = new CheckboxInput(false);
    return vorlageSpeichernInput;
  }

  private boolean checkInput(ZusatzbetragPart part)
  {
    try
    {
      if (control.getFaelligkeit().getValue() == null)
      {
        status.setValue("Bitte Fälligkeit eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (part.getBuchungstext().getValue() == null
          || ((String) part.getBuchungstext().getValue()).isEmpty())
      {
        status.setValue("Bitte Zahlungsgrund eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (part.getBetrag().getValue() == null
          || ((Double) part.getBetrag().getValue()) < 0.005d)
      {
        status.setValue("Bitte positiven Betrag eingeben");
        status.setColor(Color.ERROR);
        return false;
      }

      if (EinstellungSteuerInBuchung)
      {
        Buchungsart buchungsart = (Buchungsart) part.getBuchungsart()
            .getValue();
        Steuer steuer = (Steuer) part.getSteuer().getValue();
        if (steuer != null && buchungsart != null)
        {
          if (buchungsart.getSpende() || buchungsart.getAbschreibung())
          {
            status.setValue(
                "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
            status.setColor(Color.ERROR);
            return false;
          }
          if (steuer.getBuchungsart().getArt() != buchungsart.getArt())
          {
            switch (buchungsart.getArt())
            {
              case ArtBuchungsart.AUSGABE:
                status.setValue("Umsatzsteuer statt Vorsteuer gewählt!");
                status.setColor(Color.ERROR);
                return false;
              case ArtBuchungsart.EINNAHME:
                status.setValue("Vorsteuer statt Umsatzsteuer gewählt!");
                status.setColor(Color.ERROR);
                return false;
              // Umbuchung ist bei Anlagebuchungen möglich,
              // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf)
              // möglich
              case ArtBuchungsart.UMBUCHUNG:
                break;
            }
          }
        }
      }

      if (EinstellungRechnungAnzeigen
          && (boolean) control.getRechnung().getValue())
      {
        if (control.getRechnungFormular().getValue() == null)
        {
          status.setValue("Bitte Rechnungsformular auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
        if (control.getRechnungsdatum().getValue() == null)
        {
          status.setValue("Bitte Rechnungsdatum auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
      }

    }
    catch (RemoteException re)
    {
      status.setValue("Fehler beim Auswerten der Eingabe!");
      status.setColor(Color.ERROR);
      return false;
    }
    return true;
  }

  private Zusatzbetrag getZusatzbetrag() throws RemoteException
  {
    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    zusatzb.setStartdatum(new Date());
    zusatzb.setBuchungstext(settings.getString("buchungstext", ""));
    zusatzb.setBetrag(settings.getDouble("betrag", 0.0));

    String buchungsart = settings.getString("buchungsart", "");
    if (buchungsart.length() > 0)
    {
      try
      {
        Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
            .createObject(Buchungsart.class, buchungsart);
        zusatzb.setBuchungsart(ba);
      }
      catch (ObjectNotFoundException e)
      {
        //
      }
    }

    if (EinstellungBuchungsklasseInBuchung)
    {
      String buchungsklasse = settings.getString("buchungsklasse", "");
      if (buchungsklasse.length() > 0)
      {
        try
        {
          Buchungsklasse bk = (Buchungsklasse) Einstellungen.getDBService()
              .createObject(Buchungsklasse.class, buchungsklasse);
          zusatzb.setBuchungsklasseId(Long.valueOf(bk.getID()));
        }
        catch (ObjectNotFoundException e)
        {
          //
        }
      }
    }

    if (EinstellungSteuerInBuchung)
    {
      String steuer = settings.getString("steuer", "");
      if (steuer.length() > 0)
      {
        try
        {
          Steuer st = (Steuer) Einstellungen.getDBService()
              .createObject(Steuer.class, steuer);
          zusatzb.setSteuer(st);
        }
        catch (ObjectNotFoundException e)
        {
          //
        }
      }
    }

    String zahlungsweg = settings.getString("zahlungsweg", "");
    if (zahlungsweg.length() > 0)
    {
      try
      {
        Zahlungsweg weg = new Zahlungsweg(Integer.valueOf(zahlungsweg));
        zusatzb.setZahlungsweg(weg);
      }
      catch (Exception e)
      {
        //
      }
    }

    zusatzb.setMitgliedzahltSelbst(
        settings.getBoolean("mitgliedzahltselbst", false));
    return zusatzb;
  }

  private List<Zusatzbetrag> getZusatzbetraegeList(ZusatzbetragPart part)
      throws RemoteException, ApplicationException
  {
    List<Zusatzbetrag> list = new ArrayList<>();
    for (Mitglied mit : mitglieder)
    {
      Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
          .createObject(Zusatzbetrag.class, null);
      zb.setBetrag((Double) part.getBetrag().getValue());
      zb.setBuchungstext((String) part.getBuchungstext().getValue());
      zb.setFaelligkeit((Date) control.getFaelligkeit().getValue());
      zb.setIntervall(IntervallZusatzzahlung.KEIN);
      zb.setMitglied(Integer.parseInt(mit.getID()));
      zb.setStartdatum((Date) control.getFaelligkeit().getValue());
      zb.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
      zb.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
      if (part.isSteuerActive())
      {
        zb.setSteuer((Steuer) part.getSteuer().getValue());
      }
      zb.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zb.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      list.add(zb);
    }
    if ((Boolean) getVorlageSpeichern().getValue())
    {
      ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
          .getDBService().createObject(ZusatzbetragVorlage.class, null);
      zv.setIntervall(IntervallZusatzzahlung.KEIN);
      zv.setBuchungstext((String) part.getBuchungstext().getValue());
      zv.setBetrag((Double) part.getBetrag().getValue());
      zv.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
      zv.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
      if (part.isSteuerActive())
      {
        zv.setSteuer((Steuer) part.getSteuer().getValue());
      }
      zv.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zv.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      zv.store();
    }
    return list;
  }

  private void saveSettings(ZusatzbetragPart part)
  {
    try
    {
      settings.setAttribute("buchungstext",
          (String) part.getBuchungstext().getValue());
      settings.setAttribute("betrag", (Double) part.getBetrag().getValue());
      Buchungsart tmpba = (Buchungsart) part.getBuchungsart().getValue();
      if (tmpba != null)
      {
        settings.setAttribute("buchungsart", tmpba.getID());
      }
      else
      {
        settings.setAttribute("buchungsart", "");
      }
      if (EinstellungBuchungsklasseInBuchung)
      {
        Buchungsklasse tmpbk = (Buchungsklasse) part.getBuchungsklasse()
            .getValue();
        if (tmpbk != null)
        {
          settings.setAttribute("buchungsklasse", tmpbk.getID());
        }
        else
        {
          settings.setAttribute("buchungsklasse", "");
        }
      }
      if (EinstellungSteuerInBuchung)
      {
        Steuer tmpst = (Steuer) part.getSteuer().getValue();
        if (tmpst != null)
        {
          settings.setAttribute("steuer", tmpst.getID());
        }
        else
        {
          settings.setAttribute("steuer", "");
        }
      }
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if (weg != null)
      {
        settings.setAttribute("zahlungsweg", weg.getKey());
      }
      else
      {
        settings.setAttribute("zahlungsweg", "");
      }
      Boolean tmp = (Boolean) part.getMitgliedzahltSelbst().getValue();
      if (tmp != null)
      {
        settings.setAttribute("mitgliedzahltselbst", tmp);
      }
      else
      {
        settings.setAttribute("mitgliedzahltselbst", "false");
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler", re);
    }
  }
}
