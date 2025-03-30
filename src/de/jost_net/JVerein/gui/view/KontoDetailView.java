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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.KontoControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class KontoDetailView extends AbstractDetailView
{
  private KontoControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Konto");

    control = new KontoControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Konto");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Kontoart", control.getKontoArt());
    left.addLabelPair("Nummer", control.getNummer());
    left.addLabelPair("Bezeichnung", control.getBezeichnung());
    left.addLabelPair("Eröffnungsdatum", control.getEroeffnung());
    left.addLabelPair("Auflösungsdatum", control.getAufloesung());
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Hibiscus-Konto", control.getHibiscusId());
    right.addLabelPair("GB-Buchungsart", control.getBuchungsart());
    right.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    right.addLabelPair("Kommentar", control.getKommentar());
    
    LabelGroup group1 = new LabelGroup(getParent(), "Anlagenkonto Daten");
    ColumnLayout cl1 = new ColumnLayout(group1.getComposite(), 2);
    
    SimpleContainer left1 = new SimpleContainer(cl1.getComposite());
    left1.addLabelPair("Anlagen Buchungsart", control.getAnlagenart());
    left1.addLabelPair("AfA Buchungsart", control.getAfaart());
    left1.addLabelPair("Anlagenwert", control.getBetrag());
    left1.addLabelPair("Anschaffungsdatum", control.getAnschaffung());
    if (Einstellungen.getEinstellung().getMittelverwendung())
    {
      left1.addLabelPair("Anlagenzweck", control.getAnlagenzweck());
    }
    ButtonArea anlagenbuttons = new ButtonArea();
    anlagenbuttons.addButton(control.getAutobutton());
    left1.addButtonArea(anlagenbuttons);
    
    SimpleContainer right1 = new SimpleContainer(cl1.getComposite());
    right1.addLabelPair("Nutzungsdauer", control.getNutzungsdauer());
    right1.addLabelPair("Anlagen Restwert", control.getAfaRestwert());
    right1.addLabelPair("Afa Mode", control.getAfaMode());
    right1.addLabelPair("AfA Erstes Jahr", control.getAfaStart());
    right1.addLabelPair("AfA Folgejahre", control.getAfaDauer());
    ButtonArea afabuttons = new ButtonArea();
    afabuttons.addButton(control.getAfabutton());
    right1.addButtonArea(afabuttons);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.KONTEN, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
