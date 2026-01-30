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
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.gui.input.AbbuchungsmodusInput.AbbuchungsmodusObject;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class MitgliedAbrechnungDialog extends AbstractDialog<Boolean>
{
  private boolean fortfahren = false;

  private Mitglied[] mitglieder;

  /**
   * @param position
   */
  public MitgliedAbrechnungDialog(int position, Mitglied[] m)
  {
    super(position);
    super.setSize(950, SWT.DEFAULT);
    setTitle("Einmalige Abrechnung");
    this.mitglieder = m;
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    final AbrechnungSEPAControl control = new AbrechnungSEPAControl(null);
    final ZusatzbetragControl zcontrol = new ZusatzbetragControl(null);

    LabelGroup group = new LabelGroup(parent, "");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);

    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    zusatzb.setStartdatum(new Date());
    ZusatzbetragPart part = new ZusatzbetragPart(zusatzb, false);

    left.addHeadline("Buchung");
    left.addLabelPair("Fälligkeit ", part.getStartdatum(true));
    left.addLabelPair("Buchungstext", part.getBuchungstext());
    left.addLabelPair("Betrag", part.getBetrag());
    left.addLabelPair("Buchungsart", part.getBuchungsart());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      left.addLabelPair("Buchungsklasse", part.getBuchungsklasse());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      left.addLabelPair("Steuer", part.getSteuer());
    }
    left.addLabelPair("Zahlungsweg", part.getZahlungsweg());
    left.addLabelPair("Mitglied zahlt selbst", part.getMitgliedzahltSelbst());
    left.addHeadline("Vorlagen");
    left.addLabelPair("Als Vorlage speichern", zcontrol.getVorlage());

    // Nicht angezeigte Parameter aus dem ZusatzbetragPart und Abrechnungslauf
    // View erzeugen
    part.getFaelligkeit();
    part.getIntervall();
    part.getEndedatum();
    control.getAbbuchungsmodus()
        .setValue(new AbbuchungsmodusObject(Abrechnungsmodi.KEINBEITRAG));
    control.getFaelligkeit();
    control.getStichtag();
    control.getZahlungsgrund();
    control.getZusatzbetrag().setValue(true);
    control.getKursteilnehmer().setValue(false);

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

    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      right.addHeadline("Rechnungen");
      right.addLabelPair("Rechnung(en) erstellen", control.getRechnung());
      if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG)
          && JVereinPlugin.isArchiveServiceActive())
      {
        right.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getRechnungsdokumentSpeichern());
      }
      right.addLabelPair("Rechnung Formular", control.getRechnungFormular());
      right.addLabelPair("Rechnung Text", control.getRechnungstext());
      right.addLabelPair("Rechnung Datum", control.getRechnungsdatum());
    }

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = MitgliedMap.getDummyMap(map);

    Map<String, Object> rmap = new AllgemeineMap().getMap(null);
    rmap = MitgliedMap.getDummyMap(rmap);
    rmap = RechnungMap.getDummyMap(rmap);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINMAL_ABRECHNUNG, false, "question-circle.png");

    buttons.addButton("Buchungstext Variablen anzeigen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    buttons.addButton("Rechnung Text Variablen anzeigen",
        new InsertVariableDialogAction(rmap), null, false, "bookmark.png");

    buttons.addButton("Vorlagen", new ZusatzbetragVorlageAuswahlAction(part),
        null, false, "view-refresh.png");
    buttons.addButton("Abrechnen", context -> {
      try
      {
        List<Zusatzbetrag> list = new ArrayList<>();
        for (Mitglied mit : mitglieder)
        {
          Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
              .createObject(Zusatzbetrag.class, null);
          zb.setBetrag((Double) part.getBetrag().getValue());
          zb.setBuchungstext((String) part.getBuchungstext().getValue());
          zb.setFaelligkeit((Date) part.getStartdatum(true).getValue());
          zb.setIntervall(IntervallZusatzzahlung.KEIN);
          zb.setMitglied(Integer.parseInt(mit.getID()));
          zb.setStartdatum((Date) part.getStartdatum(true).getValue());
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
        if (zcontrol.getVorlage().getValue()
            .equals(ZusatzbetragControl.MITDATUM)
            || zcontrol.getVorlage().getValue()
                .equals(ZusatzbetragControl.OHNEDATUM))
        {
          ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
              .getDBService().createObject(ZusatzbetragVorlage.class, null);
          zv.setIntervall(IntervallZusatzzahlung.KEIN);
          zv.setBuchungstext((String) part.getBuchungstext().getValue());
          zv.setBetrag((Double) part.getBetrag().getValue());
          if (zcontrol.getVorlage().getValue()
              .equals(ZusatzbetragControl.MITDATUM))
          {
            zv.setFaelligkeit((Date) part.getStartdatum(true).getValue());
            zv.setStartdatum((Date) part.getStartdatum(true).getValue());
          }
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

        control.getFaelligkeit()
            .setValue((Date) part.getStartdatum(true).getValue());
        control.getStichtag()
            .setValue((Date) part.getStartdatum(true).getValue());
        control.getZahlungsgrund()
            .setValue((String) part.getBuchungstext().getValue());
        control.startAbrechnung(list);
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
}
