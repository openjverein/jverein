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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class AbrechnungSEPAView extends AbstractView
{

  final AbrechnungSEPAControl control = new AbrechnungSEPAControl(this);

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Abrechnung");
    LabelGroup group = new LabelGroup(getParent(), "Parameter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    SimpleContainer rigth = new SimpleContainer(cl.getComposite());

    left.addLabelPair("Modus", control.getAbbuchungsmodus());
    left.addLabelPair("Fälligkeit", control.getFaelligkeit());
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      left.addLabelPair("Abrechnungsmonat", control.getAbrechnungsmonat());
    }
    left.addLabelPair("Stichtag¹", control.getStichtag());
    left.addLabelPair("Von Eintrittsdatum", control.getVondatum());
    left.addLabelPair("Von Eingabedatum", control.getVonEingabedatum());
    left.addLabelPair("Bis Austrittsdatum", control.getBisdatum());
    left.addLabelPair("Zahlungsgrund für Beiträge", control.getZahlungsgrund());
    if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
    {
      left.addLabelPair("Zusatzbeträge", control.getZusatzbetrag());
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
    {
      left.addLabelPair("Kursteilnehmer", control.getKursteilnehmer());
    }
    left.addLabelPair("Sollbuchungen zusammenfassen",
        control.getSollbuchungenZusammenfassen());

    rigth.addHeadline("Lastschriften");
    rigth.addLabelPair("Kompakte Abbuchung", control.getKompakteAbbuchung());
    rigth.addLabelPair("SEPA-Check temporär deaktivieren",
        control.getSEPACheck());
    rigth.addLabelPair("Lastschrift-PDF erstellen", control.getSEPAPrint());
    rigth.addLabelPair("Abbuchungsausgabe", control.getAbbuchungsausgabe());

    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      rigth.addHeadline("Rechnungen");
      rigth.addLabelPair("Rechnung erstellen²", control.getRechnung());
      if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG)
          && JVereinPlugin.isArchiveServiceActive())
      {
        rigth.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getRechnungsdokumentSpeichern());
      }
      rigth.addLabelPair("Rechnungsformular", control.getRechnungFormular());
      rigth.addLabelPair("Rechnungstext", control.getRechnungstext());
      rigth.addLabelPair("Rechnungsdatum", control.getRechnungsdatum());
    }

    group.addSeparator();
    group.addText(
        "¹) Für die Berechnung, ob ein Mitglied bereits eingetreten oder ausgetreten ist. "
            + "Und für Berechnung ob Zusatzbeträge fällig sind.\n"
            + "²) Es wird für jede (zusammengefasste) Sollbuchung eine separate Rechnung erstellt.",
        true);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ABRECHNUNG, false, "question-circle.png");
    buttons.addButton("Fehler/Warnungen/Hinweise", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        GUI.startView(SEPABugsView.class.getName(), null);
      }
    }, null, false, "bug.png");

    buttons.addButton("Zahlungsgrund Variablen anzeigen",
        new RechnungVariableDialogAction(), null, false, "bookmark.png");
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      buttons.addButton("Rechnungstext Variablen anzeigen",
          new RechnungVariableDialogAction(), null, false, "bookmark.png");
    }
    buttons.addButton(control.getStartButton());
    buttons.paint(this.getParent());
  }

  private class RechnungVariableDialogAction implements Action
  {

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        new InsertVariableDialogAction(getRmap()).handleAction(null);
      }
      catch (RemoteException re)
      {
        //
      }
    }
  }

  private Map<String, Object> getRmap()
      throws RemoteException, ApplicationException
  {
    Map<String, Object> rmap = new AllgemeineMap().getMap(null);
    rmap = new AbrechnungsParameterMap()
        .getMap(new AbrechnungSEPAParam(control, null, null, null), rmap);
    rmap = MitgliedMap.getDummyMap(rmap);
    return RechnungMap.getDummyMap(rmap);
  }
}
