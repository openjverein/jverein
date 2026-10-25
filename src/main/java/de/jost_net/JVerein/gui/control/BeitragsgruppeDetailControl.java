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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.io.AltersgruppenParser;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.VonBis;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeitragsgruppeDetailControl extends VorZurueckControl
    implements Savable
{
  private BigDecimalInput[] alterstaffel;

  private CheckboxInput isAltersstaffel;

  private Input bezeichnung;

  private CheckboxInput sekundaer;

  private BigDecimalInput betrag;

  private BigDecimalInput betragmonatlich;

  private BigDecimalInput betragvierteljaehrlich;

  private BigDecimalInput betraghalbjaehrlich;

  private BigDecimalInput betragjaehrlich;

  private SelectInput beitragsart;

  private SelectInput buchungsklasse;

  private Beitragsgruppe beitrag;

  private BigDecimalInput arbeitseinsatzstunden;

  private BigDecimalInput arbeitseinsatzbetrag;

  private AbstractInput buchungsart;

  private TextAreaInput notiz;

  private SteuerInput steuer = null;

  public BeitragsgruppeDetailControl(AbstractView view)
  {
    super(view);
  }

  private Beitragsgruppe getBeitragsgruppe()
  {
    if (beitrag != null)
    {
      return beitrag;
    }
    beitrag = (Beitragsgruppe) getCurrentObject();
    return beitrag;
  }

  public Input getBezeichnung(boolean withFocus) throws RemoteException
  {
    if (bezeichnung != null)
      return bezeichnung;
    bezeichnung = new TextInput(getBeitragsgruppe().getBezeichnung(), 30);
    bezeichnung.setMandatory(true);
    if (withFocus)
    {
      bezeichnung.focus();
    }
    return bezeichnung;
  }

  public CheckboxInput getSekundaer() throws RemoteException
  {
    if (sekundaer != null)
    {
      return sekundaer;
    }
    sekundaer = new CheckboxInput(getBeitragsgruppe().getSekundaer());
    return sekundaer;
  }

  public BigDecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new BigDecimalInput(getBeitragsgruppe().getBetrag(),
        Einstellungen.BIGDECIMALFORMAT);
    if (getBeitragsgruppe().getID() != null
        && getBeitragsgruppe().getHasAltersstaffel())
      betrag.disable();
    betrag.setMandatory(true);
    return betrag;
  }

  public BigDecimalInput getBetragMonatlich() throws RemoteException
  {
    if (betragmonatlich != null)
    {
      return betragmonatlich;
    }
    betragmonatlich = new BigDecimalInput(
        getBeitragsgruppe().getBetragMonatlich(),
        Einstellungen.BIGDECIMALFORMAT);
    if (getBeitragsgruppe().getHasAltersstaffel())
      betragmonatlich.disable();
    betragmonatlich.setMandatory(true);
    return betragmonatlich;
  }

  public BigDecimalInput getBetragVierteljaehrlich() throws RemoteException
  {
    if (betragvierteljaehrlich != null)
    {
      return betragvierteljaehrlich;
    }
    betragvierteljaehrlich = new BigDecimalInput(
        getBeitragsgruppe().getBetragVierteljaehrlich(),
        Einstellungen.BIGDECIMALFORMAT);
    if (getBeitragsgruppe().getHasAltersstaffel())
      betragvierteljaehrlich.disable();
    betragvierteljaehrlich.setMandatory(true);
    return betragvierteljaehrlich;
  }

  public BigDecimalInput getBetragHalbjaehrlich() throws RemoteException
  {
    if (betraghalbjaehrlich != null)
    {
      return betraghalbjaehrlich;
    }
    betraghalbjaehrlich = new BigDecimalInput(
        getBeitragsgruppe().getBetragHalbjaehrlich(),
        Einstellungen.BIGDECIMALFORMAT);
    if (getBeitragsgruppe().getHasAltersstaffel())
      betraghalbjaehrlich.disable();
    betraghalbjaehrlich.setMandatory(true);
    return betraghalbjaehrlich;
  }

  public BigDecimalInput getBetragJaehrlich() throws RemoteException
  {
    if (betragjaehrlich != null)
    {
      return betragjaehrlich;
    }
    betragjaehrlich = new BigDecimalInput(
        getBeitragsgruppe().getBetragJaehrlich(),
        Einstellungen.BIGDECIMALFORMAT);
    if (getBeitragsgruppe().getHasAltersstaffel())
      betragjaehrlich.disable();
    betragjaehrlich.setMandatory(true);
    return betragjaehrlich;
  }

  public CheckboxInput getIsAltersstaffel() throws RemoteException
  {
    if (isAltersstaffel != null)
    {
      return isAltersstaffel;
    }
    isAltersstaffel = new CheckboxInput(
        getBeitragsgruppe().getHasAltersstaffel());
    isAltersstaffel.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        if (event != null && event.type == SWT.Selection)
        {
          Boolean b = (Boolean) isAltersstaffel.getValue();

          if (betrag != null)
          {
            betrag.setMandatory(!b);
            betrag.setEnabled(!b);
          }
          if (betragjaehrlich != null)
          {
            betragjaehrlich.setMandatory(!b);
            betragjaehrlich.setEnabled(!b);
          }
          if (betraghalbjaehrlich != null)
          {
            betraghalbjaehrlich.setMandatory(!b);
            betraghalbjaehrlich.setEnabled(!b);
          }
          if (betragvierteljaehrlich != null)
          {
            betragvierteljaehrlich.setMandatory(!b);
            betragvierteljaehrlich.setEnabled(!b);
          }
          if (betragmonatlich != null)
          {
            betragmonatlich.setMandatory(!b);
            betragmonatlich.setEnabled(!b);
          }

          if (alterstaffel != null)
          {
            for (Input i : alterstaffel)
            {
              i.setMandatory(b);
              i.setEnabled(b);
            }
          }
        }
      }
    });
    return isAltersstaffel;
  }

  public BigDecimalInput[] getAltersstaffel() throws RemoteException
  {
    if (alterstaffel != null)
    {
      return alterstaffel;
    }
    String stufen = (String) Einstellungen
        .getEinstellung(Property.BEITRAGALTERSSTUFEN);
    if (stufen == null || stufen.isEmpty())
      return null;
    AltersgruppenParser ap = new AltersgruppenParser(stufen);

    int i = 0;
    List<BigDecimalInput> list = new ArrayList<BigDecimalInput>();
    while (ap.hasNext())
    {
      VonBis vb = ap.getNext();
      BigDecimal betrag = null;
      Altersstaffel a = beitrag.getAltersstaffel(i);
      if (a != null)
        betrag = a.getBetrag();
      BigDecimalInput d = new BigDecimalInput(betrag,
          Einstellungen.BIGDECIMALFORMAT);
      if (!getBeitragsgruppe().getHasAltersstaffel())
        d.disable();
      d.setData("nummer", Integer.valueOf(i));
      d.setName(vb.getVon() + "-" + vb.getBis() + " Jahre");
      list.add(d);
      i++;
    }
    alterstaffel = list.toArray(new BigDecimalInput[0]);
    return alterstaffel;
  }

  public SelectInput getBeitragsArt() throws RemoteException
  {
    if (beitragsart != null)
    {
      return beitragsart;
    }
    beitragsart = new SelectInput(ArtBeitragsart.values(),
        getBeitragsgruppe().getBeitragsArt());
    return beitragsart;
  }

  public BigDecimalInput getArbeitseinsatzStunden() throws RemoteException
  {
    if (arbeitseinsatzstunden != null)
    {
      return arbeitseinsatzstunden;
    }
    arbeitseinsatzstunden = new BigDecimalInput(
        getBeitragsgruppe().getArbeitseinsatzStunden(),
        Einstellungen.BIGDECIMALINTFORMAT);
    return arbeitseinsatzstunden;
  }

  public BigDecimalInput getArbeitseinsatzBetrag() throws RemoteException
  {
    if (arbeitseinsatzbetrag != null)
    {
      return arbeitseinsatzbetrag;
    }
    arbeitseinsatzbetrag = new BigDecimalInput(
        getBeitragsgruppe().getArbeitseinsatzBetrag(),
        Einstellungen.BIGDECIMALFORMAT);
    return arbeitseinsatzbetrag;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(
        getBeitragsgruppe().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsart.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Buchungsart bua = (Buchungsart) buchungsart.getValue();
          if (buchungsklasse != null && buchungsklasse.getValue() == null
              && bua != null)
            buchungsklasse.setValue(bua.getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    buchungsart.addListener(e -> {
      if (steuer != null && buchungsart.getValue() != null)
      {
        try
        {
          steuer.setValue(((Buchungsart) buchungsart.getValue()).getSteuer());
        }
        catch (RemoteException e1)
        {
          Logger.error("Fehler", e1);
        }
      }
    });
    return buchungsart;
  }

  public SelectInput getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    buchungsklasse = new BuchungsklasseInput().getBuchungsklasseInput(
        buchungsklasse, getBeitragsgruppe().getBuchungsklasse());
    return buchungsklasse;
  }

  private Long getSelectedBuchungsKlasseId() throws ApplicationException
  {
    try
    {
      if (null == buchungsklasse)
        return null;
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse()
          .getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public TextAreaInput getNotiz() throws RemoteException
  {
    if (notiz != null)
    {
      return notiz;
    }
    notiz = new TextAreaInput(getBeitragsgruppe().getNotiz(), 200);
    notiz.setName("Notiz");
    notiz.setHeight(50);
    return notiz;
  }

  public SelectInput getSteuer() throws RemoteException
  {
    if (steuer != null)
    {
      return steuer;
    }
    steuer = new SteuerInput(getBeitragsgruppe().getSteuer());

    steuer.setAttribute("name");
    steuer.setPleaseChoose("Keine Steuer");

    return steuer;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Beitragsgruppe b = getBeitragsgruppe();
    b.setBezeichnung((String) getBezeichnung(false).getValue());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN))
    {
      b.setSekundaer((Boolean) sekundaer.getValue());
    }
    else
    {
      b.setSekundaer(false);
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.FAMILIENBEITRAG))
    {
      ArtBeitragsart ba = (ArtBeitragsart) getBeitragsArt().getValue();
      b.setBeitragsArt(ba.getKey());
    }
    else
    {
      b.setBeitragsArt(ArtBeitragsart.NORMAL.getKey());
    }
    b.setBuchungsart((Buchungsart) getBuchungsart().getValue());
    b.setBuchungsklasseId(getSelectedBuchungsKlasseId());
    if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
    {
      b.setArbeitseinsatzStunden(
          (BigDecimal) getArbeitseinsatzStunden().getValue());
      b.setArbeitseinsatzBetrag(
          (BigDecimal) getArbeitseinsatzBetrag().getValue());
    }
    b.setNotiz((String) getNotiz().getValue());
    if (steuer != null)
    {
      b.setSteuer((Steuer) steuer.getValue());
    }

    switch (Beitragsmodel.getByKey(
        (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)))
    {
      case GLEICHERTERMINFUERALLE:
      case MONATLICH12631:
        if (isAltersstaffel != null && (Boolean) isAltersstaffel.getValue()
            && alterstaffel != null)
        {
          b.setHasAltersstaffel(true);
        }
        else
        {
          b.setBetrag((BigDecimal) getBetrag().getValue());
          b.setHasAltersstaffel(false);
        }
        break;
      case FLEXIBEL:
        b.setBetragMonatlich((BigDecimal) getBetragMonatlich().getValue());
        b.setBetragVierteljaehrlich(
            (BigDecimal) getBetragVierteljaehrlich().getValue());
        b.setBetragHalbjaehrlich(
            (BigDecimal) getBetragHalbjaehrlich().getValue());
        b.setBetragJaehrlich((BigDecimal) getBetragJaehrlich().getValue());
        b.setHasAltersstaffel(false);
        break;
    }
    return b;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Beitragsgruppe b = (Beitragsgruppe) prepareStore();
      if (isAltersstaffel != null && (Boolean) isAltersstaffel.getValue()
          && alterstaffel != null)
      {
        if (b.isNewObject())
        {
          b.store();
        }
        for (BigDecimalInput i : alterstaffel)
        {
          Altersstaffel a = null;
          BigDecimal betrag = (BigDecimal) i.getValue();
          if (betrag == null)
          {
            throw new ApplicationException(
                "Bitte Betrag bei Altersstaffel eingeben!");
          }
          a = b.getAltersstaffel((Integer) i.getData("nummer"));
          if (a != null)
          {
            a.setBetrag(betrag);
          }
          else
          {
            a = (Altersstaffel) Einstellungen.getDBService()
                .createObject(Altersstaffel.class, null);
            a.setBeitragsgruppe(b);
            a.setBetrag(betrag);
            a.setNummer((Integer) i.getData("nummer"));
          }
          a.store();
        }
      }
      b.store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Beitragsgruppe";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
