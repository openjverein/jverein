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
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedNextBGruppe;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeitragsgruppeImpl extends AbstractJVereinDBObject
    implements Beitragsgruppe
{

  private static final long serialVersionUID = 1L;

  public BeitragsgruppeImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "beitragsgruppe";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bezeichnung";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (getSekundaer())
      {
        DBIterator<SekundaereBeitragsgruppe> sek = Einstellungen.getDBService()
            .createList(SekundaereBeitragsgruppe.class);
        sek.addFilter("beitragsgruppe = ?", new Object[] { getID() });
        sek.setLimit(1);
        if (sek.size() > 0)
        {
          throw new ApplicationException(String.format(
              "Die sekundäre Beitragsgruppe ist Mitgliedern zugeordnet.",
              sek.size()));
        }
      }
      else
      {
        DBIterator<Mitglied> mitgl = Einstellungen.getDBService()
            .createList(Mitglied.class);
        mitgl.addFilter("beitragsgruppe = ?", new Object[] { getID() });
        mitgl.setLimit(1);
        if (mitgl.size() > 0)
        {
          throw new ApplicationException(
              "Die Beitragsgruppe ist Mitgliedern zugeordnet.");
        }
      }
      DBIterator<MitgliedNextBGruppe> nextbg = Einstellungen.getDBService()
          .createList(MitgliedNextBGruppe.class);
      nextbg.addFilter("beitragsgruppe = ?", new Object[] { getID() });
      if (nextbg.size() > 0)
      {
        throw new ApplicationException(String.format(
            "Bei %d Mitglied(er) ist diese Beitragsgruppe als zukünftige Beitragsgrupe hinterlegt.",
            nextbg.size()));
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Beitragsgruppe kann nicht gelöscht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().isEmpty())
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben!");
      }
      DBIterator<Beitragsgruppe> gruppeIt = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      if (!this.isNewObject())
      {
        gruppeIt.addFilter("id != ?", getID());
      }
      gruppeIt.addFilter("bezeichnung = ?", getBezeichnung());
      if (gruppeIt.hasNext())
      {
        throw new ApplicationException(
            "Bitte eindeutige Bezeichnung eingeben!");
      }
      switch (Beitragsmodel.getByKey(
          (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)))
      {
        case GLEICHERTERMINFUERALLE:
        case MONATLICH12631:
          if (getBetrag() < 0)
          {
            throw new ApplicationException("Betrag nicht gültig");
          }

          break;
        case FLEXIBEL:
          if (getBetragMonatlich() < 0 || getBetragVierteljaehrlich() < 0
              || getBetragHalbjaehrlich() < 0 || getBetragJaehrlich() < 0)
          {
            throw new ApplicationException("Betrag nicht gültig");
          }

          break;
      }
      Beitragsgruppe gruppeAlt = null;
      try
      {
        gruppeAlt = (Beitragsgruppe) Einstellungen.getDBService()
            .createObject(Beitragsgruppe.class, getID());
      }
      catch (RemoteException e)
      {
        // Alte Beitragsgruppe nicht gefunden
      }
      if (getBeitragsArt() != null)
      {
        if (gruppeAlt != null)
        {
          // Da es die Beitragsart ZAHLER nicht mehr gibt sie aber noch in der
          // Datenbank stehen kann, müssen wir auf null prüfen
          ArtBeitragsart artAlt = gruppeAlt.getBeitragsArt();
          if (artAlt == null)
            artAlt = ArtBeitragsart.NORMAL;
          if (artAlt.getKey() != getBeitragsArt().getKey())
          {
            DBIterator<Mitglied> list = Einstellungen.getDBService()
                .createList(Mitglied.class);
            list.addFilter("beitragsgruppe = ?", getID());
            if (list.hasNext())
            {
              throw new ApplicationException(
                  "Es existieren Mitglieder mit diesem Beitrag, Beitragsart kann nicht geändert werden!");
            }
          }
        }
        if (getSekundaer() && getBeitragsArt()
            .getKey() == ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey())
        {
          throw new ApplicationException(
              "Sekundäre Beitragsgrupe kann nicht Beitragsart Familienangehöriger haben!");
        }
      }
      if (getSekundaer() != null)
      {
        if (gruppeAlt != null && gruppeAlt.getSekundaer() != getSekundaer())
        {
          if (gruppeAlt.getSekundaer())
          {
            DBIterator<SekundaereBeitragsgruppe> list = Einstellungen
                .getDBService().createList(SekundaereBeitragsgruppe.class);
            list.addFilter("beitragsgruppe = ?", getID());
            if (list.hasNext())
            {
              throw new ApplicationException(
                  "Es existieren Mitglieder mit diesem sekundären Beitrag, Sekundär kann nicht geändert werden!");
            }
          }
          else
          {
            DBIterator<Mitglied> list = Einstellungen.getDBService()
                .createList(Mitglied.class);
            list.addFilter("beitragsgruppe = ?", getID());
            if (list.hasNext())
            {
              throw new ApplicationException(
                  "Es existieren Mitglieder mit diesem Beitrag, Sekundär kann nicht geändert werden!");
            }
          }
        }
        if (getSekundaer() && getBeitragsArt()
            .getKey() == ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey())
        {
          throw new ApplicationException(
              "Sekundäre Beitragsgrupe kann nicht Beitragsart Angehöriger haben!");
        }
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        if (getSteuer() != null && getBuchungsart() != null && getSteuer()
            .getBuchungsart().getArt() != getBuchungsart().getArt())
        {
          switch (getBuchungsart().getArt())
          {
            case ArtBuchungsart.AUSGABE:
              throw new ApplicationException(
                  "Umsatzsteuer statt Vorsteuer gewählt.");
            case ArtBuchungsart.EINNAHME:
              throw new ApplicationException(
                  "Vorsteuer statt Umsatzsteuer gewählt.");
            // Umbuchung ist bei Anlagebuchungen möglich,
            // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf) möglich
            case ArtBuchungsart.UMBUCHUNG:
              break;
          }
        }
        if (getSteuer() != null && getBuchungsart() != null
            && (getBuchungsart().getSpende()
                || getBuchungsart().getAbschreibung()))
        {
          throw new ApplicationException(
              "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
        }
      }
    }
    catch (RemoteException e)
    {
      Logger.error("insert check of beitragsgruppe failed", e);
      throw new ApplicationException(
          "Beitragsgruppe kann nicht gespeichert werden. Siehe system log");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  @Override
  public Boolean getSekundaer() throws RemoteException
  {
    return Util.getBoolean(getAttribute("sekundaer"));
  }

  @Override
  public void setSekundaer(Boolean sekundaer) throws RemoteException
  {
    setAttribute("sekundaer", Boolean.valueOf(sekundaer));
  }

  @Override
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetrag(double d) throws RemoteException
  {
    setAttribute("betrag", Double.valueOf(d));
  }

  @Override
  public double getBetragMonatlich() throws RemoteException
  {
    Double d = (Double) getAttribute("betragmonatlich");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetragMonatlich(double d) throws RemoteException
  {
    setAttribute("betragmonatlich", Double.valueOf(d));
  }

  @Override
  public double getBetragVierteljaehrlich() throws RemoteException
  {
    Double d = (Double) getAttribute("betragvierteljaehrlich");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetragVierteljaehrlich(double d) throws RemoteException
  {
    setAttribute("betragvierteljaehrlich", Double.valueOf(d));
  }

  @Override
  public double getBetragHalbjaehrlich() throws RemoteException
  {
    Double d = (Double) getAttribute("betraghalbjaehrlich");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetragHalbjaehrlich(double d) throws RemoteException
  {
    setAttribute("betraghalbjaehrlich", Double.valueOf(d));
  }

  @Override
  public double getBetragJaehrlich() throws RemoteException
  {
    Double d = (Double) getAttribute("betragjaehrlich");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetragJaehrlich(double d) throws RemoteException
  {
    setAttribute("betragjaehrlich", Double.valueOf(d));
  }

  @Override
  public ArtBeitragsart getBeitragsArt() throws RemoteException
  {
    Integer i = (Integer) getAttribute("beitragsart");
    if (i == null)
    {
      i = Integer.valueOf("0");
    }
    return ArtBeitragsart.getByKey(i);
  }

  @Override
  public void setBeitragsArt(int art) throws RemoteException
  {
    setAttribute("beitragsart", art);
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsklasse");
    if (l == null)
    {
      return null; // Keine Buchungsklasse zugeordnet
    }

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public Long getBuchungsklasseId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsklasse");
  }

  @Override
  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException
  {
    setAttribute("buchungsklasse", buchungsklasseId);
  }

  @Override
  public double getArbeitseinsatzStunden() throws RemoteException
  {
    return (Double) getAttribute("arbeitseinsatzstunden");
  }

  @Override
  public void setArbeitseinsatzStunden(double arbeitseinsatzStunden)
      throws RemoteException
  {
    setAttribute("arbeitseinsatzstunden",
        Double.valueOf(arbeitseinsatzStunden));
  }

  @Override
  public double getArbeitseinsatzBetrag() throws RemoteException
  {
    return (Double) getAttribute("arbeitseinsatzbetrag");
  }

  @Override
  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException
  {
    setAttribute("buchungsart", buchungsart);
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsart");
  }

  @Override
  public void setArbeitseinsatzBetrag(double arbeitseinsatzBetrag)
      throws RemoteException
  {
    setAttribute("arbeitseinsatzbetrag", Double.valueOf(arbeitseinsatzBetrag));
  }

  @Override
  public String getNotiz() throws RemoteException
  {
    return (String) getAttribute("notiz");
  }

  @Override
  public void setNotiz(String notiz) throws RemoteException
  {
    setAttribute("notiz", notiz);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
    }
    else if (fieldName.equals("buchungsart"))
    {
      return getBuchungsart();
    }
    if (fieldName.equals("steuer"))
    {
      return getSteuer();
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public boolean getHasAltersstaffel() throws RemoteException
  {
    Object o = getAttribute("altersstaffel");
    if (o == null)
      return false;
    return (boolean) o;
  }

  @Override
  public DBIterator<Altersstaffel> getAltersstaffelIterator()
      throws RemoteException
  {
    DBIterator<Altersstaffel> a = Einstellungen.getDBService()
        .createList(Altersstaffel.class);
    a.addFilter("beitragsgruppe = ?", getID());
    a.setOrder("order by nummer");
    return a;
  }

  @Override
  public Altersstaffel getAltersstaffel(int nummer) throws RemoteException
  {
    DBIterator<Altersstaffel> a = Einstellungen.getDBService()
        .createList(Altersstaffel.class);
    a.addFilter("beitragsgruppe = ?", getID());
    a.addFilter("nummer = ?", nummer);
    if (a.hasNext())
      return a.next();
    else
      return null;
  }

  @Override
  public void setHasAltersstaffel(boolean b) throws RemoteException
  {
    setAttribute("altersstaffel", b);
  }

  @Override
  public Steuer getSteuer() throws RemoteException
  {
    Object l = (Object) super.getAttribute("steuer");
    if (l == null)
    {
      return null; // Keine Steuer zugeordnet
    }

    Cache cache = Cache.get(Steuer.class, true);
    return (Steuer) cache.get(l);
  }

  @Override
  public void setSteuer(Steuer steuer) throws RemoteException
  {
    setAttribute("steuer", steuer);
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "notiz":
        return "";
      case "sekundaer":
      case "altersstaffel":
        return false;
      case "arbeitseinsatzstunden":
      case "arbeitseinsatzbetrag":
        return 0d;
      case "beitragsart":
        return ArtBeitragsart.NORMAL.getKey();
      default:
        return null;
    }
  }

  @Override
  public String getObjektName()
  {
    return "Beitragsgruppe";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Beitragsgruppen";
  }
}
