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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.server.SpendenbescheinigungNode;
import de.jost_net.JVerein.util.SpbAdressaufbereitung;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungAutoNeuControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private SelectInput jahr;

  private TreePart spbTree;

  public SpendenbescheinigungAutoNeuControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public SelectInput getJahr()
  {
    if (jahr != null)
    {
      return jahr;
    }
    Calendar cal = Calendar.getInstance();
    jahr = new SelectInput(
        new Object[] { cal.get(Calendar.YEAR), cal.get(Calendar.YEAR) - 1,
            cal.get(Calendar.YEAR) - 2, cal.get(Calendar.YEAR) - 3 },
        cal.get(Calendar.YEAR));
    jahr.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          spbTree.setRootObject(
              new SpendenbescheinigungNode((Integer) getJahr().getValue()));
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }

    });
    return jahr;
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    //
  }

  public Button getSpendenbescheinigungErstellenButton()
  {
    Button b = new Button("Erstellen", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          @SuppressWarnings("rawtypes")
          List items = spbTree.getItems();

          // Baum Spendenbescheinigungen enthält keine Einträge
          if (items == null)
            return;

          SpendenbescheinigungNode spn = (SpendenbescheinigungNode) items
              .get(0);
          // Loop über die Mitglieder
          @SuppressWarnings("rawtypes")
          GenericIterator it1 = spn.getChildren();
          while (it1.hasNext())
          {
            ArrayList<Buchung> sachspenden = new ArrayList<>();
            SpendenbescheinigungNode sp1 = (SpendenbescheinigungNode) it1
                .next();
            Spendenbescheinigung spbescheinigung = (Spendenbescheinigung) Einstellungen
                .getDBService().createObject(Spendenbescheinigung.class, null);
            spbescheinigung.setSpendenart(Spendenart.GELDSPENDE);
            SpbAdressaufbereitung.adressaufbereitung(sp1.getMitglied(),
                spbescheinigung);
            spbescheinigung.setBescheinigungsdatum(new Date());
            spbescheinigung.setSpendedatum(new Date());
            spbescheinigung.setBetrag(0.01);
            spbescheinigung.setBezeichnungSachzuwendung("");
            spbescheinigung.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
            spbescheinigung.setUnterlagenWertermittlung(false);
            // Loop über die Buchungen eines Mitglieds
            @SuppressWarnings("rawtypes")
            GenericIterator it2 = sp1.getChildren();
            while (it2.hasNext())
            {
              Buchung bu = ((SpendenbescheinigungNode) it2.next()).getBuchung();
              // Bei Sachspende
              if (bu.getBezeichnungSachzuwendung() != null
                  && !bu.getBezeichnungSachzuwendung().isEmpty())
              {
                sachspenden.add(bu);
              }
              else
              {
                spbescheinigung.addBuchung(bu);
              }
            }
            // Nun noch das korrekte Formular setzen
            if (spbescheinigung.getBuchungen().size() > 1)
            {
              spbescheinigung.setFormular(
                  (Formular) FormularInput.initdefault((String) Einstellungen
                      .getEinstellung(Property.FORMULARSAMMELSPENDE)));
            }
            else
            {
              spbescheinigung.setFormular(
                  (Formular) FormularInput.initdefault((String) Einstellungen
                      .getEinstellung(Property.FORMULARGELDSPENDE)));
            }
            if (spbescheinigung.getBuchungen().size() > 0)
            {
              // Spendenbescheinigungen erfolgreich erstellt
              spbescheinigung.store();
            }
            // Jetzt noch Sachspenden erzeugen falls vorhanden
            for (Buchung bu : sachspenden)
            {
              Spendenbescheinigung spb = (Spendenbescheinigung) Einstellungen
                  .getDBService()
                  .createObject(Spendenbescheinigung.class, null);
              spb.setSpendenart(Spendenart.SACHSPENDE);
              SpbAdressaufbereitung.adressaufbereitung(sp1.getMitglied(), spb);
              spb.setBuchung(bu);
              spb.setBescheinigungsdatum(new Date());
              spb.setSpendedatum(bu.getDatum());
              spb.setBezeichnungSachzuwendung(bu.getBezeichnungSachzuwendung());
              spb.setHerkunftSpende(bu.getHerkunftSpende());
              spb.setUnterlagenWertermittlung(bu.getUnterlagenWertermittlung());
              spb.setFormular(
                  (Formular) FormularInput.initdefault((String) Einstellungen
                      .getEinstellung(Property.FORMULARSACHSPENDE)));
              spb.store();
            }
          }
          spbTree.removeAll();
          GUI.getStatusBar()
              .setSuccessText("Spendenbescheinigung(en) erstellt");
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler bei der Aufbereitung der Spendenbescheinigung");
        }
      }
    }, null, false, "document-save.png");
    return b;
  }

  public Part getSpendenbescheinigungTree() throws RemoteException
  {
    spbTree = new TreePart(
        new SpendenbescheinigungNode((Integer) getJahr().getValue()), null);
    spbTree.setFormatter(new TreeFormatter()
    {
      @Override
      public void format(TreeItem item)
      {
        SpendenbescheinigungNode spbn = (SpendenbescheinigungNode) item
            .getData();
        try
        {
          if (spbn.getNodeType() == SpendenbescheinigungNode.ROOT)
            item.setImage(SWTUtil.getImage("file-invoice.png"));
          if (spbn.getNodeType() == SpendenbescheinigungNode.MITGLIED)
            item.setImage(SWTUtil.getImage("user.png"));
          if (spbn.getNodeType() == SpendenbescheinigungNode.BUCHUNG)
            item.setImage(SWTUtil.getImage("euro-sign.png"));
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim TreeFormatter", e);
        }
      }
    });
    return spbTree;
  }

}
