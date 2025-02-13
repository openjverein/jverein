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
package de.jost_net.JVerein.Queries;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.BuchungsControl.SplitFilter;
import de.jost_net.JVerein.io.Suchbetrag;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;

public class BuchungQuery
{
  private Date datumvon;

  private Date datumbis;

  private Konto konto;

  public Buchungsart buchungart;

  private Projekt projekt;

  public String text;

  public String betrag;
  
  private String mitglied;

  private List<Buchung> ergebnis;

  private Boolean hasMitglied;
  
  private boolean geldkonto;
  
  private HashMap<String, String> sortValues = new HashMap<String, String>();

  private void SortHashMap() {
	  sortValues.put("ORDER_ID","order by id");
	  sortValues.put("ORDER_DATUM","order by datum");
	  sortValues.put("ORDER_DATUM_NAME","order by datum, name");
	  sortValues.put("ORDER_DATUM_ID","order by datum, id");
	  sortValues.put("ORDER_DATUM_ID_NAME","order by datum, id, name");
	  sortValues.put("ORDER_DATUM_AUSZUGSNUMMER","order by datum, auszugsnummer");
	  sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_NAME","order by datum, auszugsnummer, name");
	  sortValues.put("ORDER_DATUM_BLATTNUMMER","order by datum, blattnummer");
	  sortValues.put("ORDER_DATUM_BLATTNUMMER_NAME","order by datum, blattnummer, name");
	  sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_ID","order by datum, auszugsnummer, id");
	  sortValues.put("ORDER_DATUM_BLATTNUMMER_ID","order by datum, blattnummer, id");
	  sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_BLATTNUMMER_ID","order by datum, auszugsnummer, blattnummer, id");
	  sortValues.put("DEFAULT","order by datum");
  }
  
  public String ordername = null;

  private SplitFilter split;

  private boolean ungeprueft;

  public BuchungQuery(Date datumvon, Date datumbis, Konto konto,
      Buchungsart buchungsart, Projekt projekt, String text, String betrag,
      Boolean hasMitglied, String mitglied, boolean geldkonto,
      SplitFilter split, boolean ungeprueft)
  {
    this.datumvon = datumvon;
    this.datumbis = datumbis;
    this.konto = konto;
    this.buchungart = buchungsart;
    this.projekt = projekt;
    this.text = text;
    this.betrag = betrag;
    this.hasMitglied = hasMitglied;
    this.geldkonto = geldkonto;
    this.mitglied = mitglied;
    this.split = split;
    this.ungeprueft = ungeprueft;
  }
  
  public String getOrder(String value) {
	  SortHashMap();
	  String newvalue = null;
	  if ( value == null ) {
		  return sortValues.get("DEFAULT");
	  } else {
		  newvalue = value.replaceAll(", ", "_");
		  newvalue = newvalue.toUpperCase();
		  newvalue = "ORDER_" + newvalue;
          return sortValues.get(newvalue);
	  }
  }
  
  public void setOrdername(String value)
  {
    if ( value != null ) {
    	ordername = value;
    }
  }
  
  public Boolean getHasMitglied()
  {
    return hasMitglied;
  }

  public void setHasMitglied(Boolean hasMitglied)
  {
    this.hasMitglied = hasMitglied;
  }

  public Date getDatumvon()
  {
    return datumvon;
  }

  public Date getDatumbis()
  {
    return datumbis;
  }

  public Konto getKonto()
  {
    return konto;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungart;
  }

  public Projekt getProjekt()
  {
    return projekt;
  }

  public String getText()
  {
    return text;
  }

  @SuppressWarnings("unchecked")
  public List<Buchung> get() throws RemoteException
  {
    final DBService service = Einstellungen.getDBService();
    DBIterator<Buchung> it = service.createList(Buchung.class);
    
    if (mitglied != null && !mitglied.isEmpty())
    {
      String mitgliedsuche = "%" + mitglied.toLowerCase() + "%";
      it.join(Sollbuchung.TABLE_NAME);
      it.addFilter(Sollbuchung.TABLE_NAME_ID + " = " + Buchung.SOLLBUCHUNG);
      it.join("mitglied");
      it.addFilter("mitglied.id = " + Sollbuchung.T_MITGLIED);
      it.addFilter("(lower(mitglied.name) like ? or lower(mitglied.vorname) like ?)",
          new Object[] { mitgliedsuche, mitgliedsuche });
    }
    
    it.addFilter("buchung.datum >= ? ", datumvon);
    it.addFilter("buchung.datum <= ? ", datumbis);

    if (konto != null)
    {
      it.addFilter("konto = ? ", konto.getID());
    }
    else if (!geldkonto)
    {
      it.join("konto");
      it.addFilter("konto.id = buchung.konto");
      it.addFilter("kontoart = ?",
          new Object[] { Kontoart.ANLAGE.getKey() });
    }


    if (buchungart != null)
    {
      if (buchungart.getNummer() == -1)
      {
        it.addFilter("buchung.buchungsart is null ");
      }
      else if (buchungart.getNummer() >= 0)
      {
        it.addFilter("buchung.buchungsart = ? ", buchungart.getID());
      }
    }

    if (hasMitglied != null)
    {
      if (hasMitglied)
      {
        it.addFilter(Buchung.SOLLBUCHUNG + " is not null");
      }
      else
      {
        it.addFilter(Buchung.SOLLBUCHUNG + " is null");
      }
    }

    if (projekt != null)
    {
      if (projekt.getID() == null)
      {
        it.addFilter("projekt is null");
      }
      else
      {
        it.addFilter("projekt = ?", projekt.getID());
      }
    }

    switch (split)
    {
      case SPLIT:
        it.addFilter("(buchung.splittyp is null or buchung.splittyp = ?)",
            SplitbuchungTyp.SPLIT);
        break;
      case HAUPT:
        it.addFilter("(buchung.splittyp is null or buchung.splittyp = ?)",
            SplitbuchungTyp.HAUPT);
        break;
      default:
        break;
    }

    if (ungeprueft)
    {
      it.addFilter("(geprueft = 0 or geprueft is null)");
    }

    if (betrag != null && betrag.length() > 0)
    {
      try
      {
        Suchbetrag suchbetrag = new Suchbetrag(betrag);
        switch (suchbetrag.getSuchstrategie())
        {
          case GLEICH:
          {
            it.addFilter("buchung.betrag = ?", suchbetrag.getBetrag());
            break;
          }
          case GRÖSSER:
          {
            it.addFilter("buchung.betrag > ?", suchbetrag.getBetrag());
            break;
          }
          case GRÖSSERGLEICH:
          {
            it.addFilter("buchung.betrag >= ?", suchbetrag.getBetrag());
            break;
          }
          case BEREICH:
            it.addFilter("buchung.betrag >= ? AND buchung.betrag <= ?", suchbetrag.getBetrag(),
                suchbetrag.getBetrag2());
            break;
          case KEINE:
            break;
          case KLEINER:
            it.addFilter("buchung.betrag < ?", suchbetrag.getBetrag());
            break;
          case KLEINERGLEICH:
            it.addFilter("buchung.betrag <= ?", suchbetrag.getBetrag());
            break;
          case BETRAG:
            it.addFilter("(buchung.betrag = ? OR buchung.betrag = ?)", suchbetrag.getBetrag(), suchbetrag.getBetrag().negate());
            break;
          default:
            break;
        }
      }
      catch (Exception e)
      {
        // throw new RemoteException(e.getMessage());
      }
    }

    if (text.length() > 0)
    {
      Long id = 0L;
      try
      {
        id = Long.parseLong(text);
      }
      catch (Exception e)
      {
        ;
      }
      String ttext = text.toUpperCase();
      ttext = "%" + ttext + "%";
      it.addFilter(
          "(upper(buchung.name) like ? or upper(buchung.zweck) like ? "
          + "or upper(buchung.kommentar) like ? or buchung.id = ?) ",
          ttext, ttext, ttext, id);
    }

    // 20220823: sbuer: Neue Sortierfelder
    SortHashMap();
    String orderString = getOrder(ordername);
    // System.out.println("ordervalue : " + ordername + " ,orderString : " + orderString);
    it.setOrder(orderString);
    
    this.ergebnis = it != null ? PseudoIterator.asList(it) : null;
    return ergebnis;
  }

  public String getSubtitle() throws RemoteException
  {
    String subtitle = String.format("vom %s bis %s",
        new JVDateFormatTTMMJJJJ().format(getDatumvon()),
        new JVDateFormatTTMMJJJJ().format(getDatumbis()));
    if (getKonto() != null)
    {
      subtitle += " " + String.format("für Konto %s - %s",
          getKonto().getNummer(), getKonto().getBezeichnung());
    }
    if (getProjekt() != null)
    {
      subtitle += ", "
          + String.format("Projekt %s", getProjekt().getBezeichnung());
    }
    if (getText() != null && getText().length() > 0)
    {
      subtitle += ", " + String.format("Text=%s", getText());
    }
    return subtitle;
  }

  public int getSize()
  {
    return ergebnis.size();
  }

}
