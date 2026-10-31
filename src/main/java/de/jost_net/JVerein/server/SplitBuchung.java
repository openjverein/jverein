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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

public class SplitBuchung
{
  private ArrayList<Buchung> splitbuchungen = new ArrayList<>();

  private Buchung[] hauptBuchungen = null;

  private int anzahl = 0;

  public SplitBuchung(Buchung b) throws RemoteException, ApplicationException
  {
    this(new Buchung[] { b });
  }

  public SplitBuchung(Buchung[] buchungen)
      throws RemoteException, ApplicationException
  {
    anzahl = buchungen.length;
    hauptBuchungen = buchungen;

    Buchung b = buchungen[0];

    // Wenn eine gesplittete Buchung aufgerufen wird, wird die Hauptbuchung
    // gelesen
    if (b.getSplitId() != null)
    {
      b = createNewBuchung(b.getSplitId().toString());
    }
    else
    {
      // Ansonsten eine neue Splitbuchung erstellen
      b.setSplitId(Long.valueOf(b.getID()));
      b.setSplitTyp(SplitbuchungTyp.HAUPT);
      addBuchung(b);
    }
    DBIterator<Buchung> it = getBuchungIt(b.getID());
    if (!it.hasNext())
    {
      // Wenn keine Buchung gefunden wurde, gibt es auch keine Gegenbuchung.
      // Dann wird die jetzt erstellt.
      addBuchung(createGegenbuchung(b));
    }
    while (it.hasNext())
    {
      Buchung buchung = (Buchung) it.next();
      addBuchung(buchung);
    }
  }

  private DBIterator<Buchung> getBuchungIt(String id) throws RemoteException
  {
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.addFilter("splitid = ?", id);
    return it;
  }

  private Buchung createNewBuchung(String id) throws RemoteException
  {
    return (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
        id);
  }

  /**
   * Holt alle Buchungen die zur Splitbuchung gehören
   * 
   * @return
   */
  public ArrayList<Buchung> getBuchungen()
  {
    return new ArrayList<>(splitbuchungen);
  }

  /**
   * Die Hauptbuchung
   * 
   * @return
   * @throws RemoteException
   */
  public Buchung getMaster() throws RemoteException
  {
    for (Buchung b : splitbuchungen)
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        return b;
      }
    }
    throw new RemoteException("Hauptbuchung fehlt");
  }

  /**
   * Fügt der Splittbuchung eine Buchung hinzu
   * 
   * @param b
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void addBuchung(Buchung b) throws RemoteException, ApplicationException
  {
    b.setSplitbuchung(this);
    for (Buchung buchung : splitbuchungen)
    {
      if (BeanUtil.equals(buchung, b))
      {
        return;
      }
    }
    splitbuchungen.add(b);
  }

  /**
   * Hollt die Summe aller Buchungen diese Typs
   * 
   * @param typ
   * @return Summe
   * @throws RemoteException
   */
  public BigDecimal getSumme(Integer typ) throws RemoteException
  {
    BigDecimal summe = BigDecimal.ZERO;
    for (Buchung b : splitbuchungen)
    {
      if (typ.equals(b.getSplitTyp()) && !b.isToDelete())
      {
        summe = summe.add(b.getBetrag());
      }
    }
    return summe;
  }

  /**
   * Löst die Splitbuchung auf
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void aufloesen() throws RemoteException, ApplicationException
  {
    DBTransaction.starten();
    try
    {
      for (Buchung b : splitbuchungen)
      {
        if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
        {
          b.setSplitId(null);
          b.setSplitTyp(null);
          b.store();
        }
        else
        {
          b.delete();
        }
      }
      DBTransaction.commit();
      splitbuchungen.clear();
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  /**
   * Prüft ob es eine ungespeicherte Änderung gibt
   * 
   * @return
   * @throws RemoteException
   */
  public boolean hasChanged() throws RemoteException
  {
    for (Buchung b : getBuchungen())
    {
      if (b.isChanged())
      {
        return true;
      }
    }
    return false;
  }

  public void prepareStore() throws RemoteException, ApplicationException
  {
    if (splitbuchungen.size() == 0) // Splitbuchungen wurden aufgelöst
    {
      return;
    }
    BigDecimal gegen = getSumme(SplitbuchungTyp.GEGEN).negate();
    if (!getSumme(SplitbuchungTyp.HAUPT).equals(gegen))
    {
      throw new ApplicationException(
          "Die Minusbuchung muss den gleichen Betrag mit umgekehrtem Vorzeichen wie die Hauptbuchung haben.");
    }
    BigDecimal differenz = getSumme(SplitbuchungTyp.HAUPT)
        .subtract(getSumme(SplitbuchungTyp.SPLIT));
    if (differenz.signum() != 0)
    {
      throw new ApplicationException(
          "Differenz zwischen Hauptbuchung und Splitbuchungen: " + differenz);
    }

    Buchungsart ba_haupt = null;
    Buchungsart ba_gegen = null;
    Steuer steuer_haupt = null;
    Steuer steuer_gegen = null;

    for (Buchung b : getBuchungen())
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        ba_haupt = b.getBuchungsart();
        if (ba_haupt == null)
        {
          throw new ApplicationException(
              "Buchungsart bei der Hauptbuchung fehlt");
        }
        steuer_haupt = b.getSteuer();
      }
      if (b.getSplitTyp() == SplitbuchungTyp.GEGEN)
      {
        ba_gegen = b.getBuchungsart();
        if (ba_gegen == null)
        {
          throw new ApplicationException(
              "Buchungsart bei der Gegenbuchung fehlt");
        }
        steuer_gegen = b.getSteuer();
      }
    }
    if (!ba_haupt.getNummer().equals(ba_gegen.getNummer()))
    {
      throw new ApplicationException(
          "Buchungsarten bei Haupt- und Gegenbuchung müssen identisch sein");
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG)
        && ((steuer_haupt == null && steuer_gegen != null)
            || (steuer_haupt != null && !steuer_haupt.equals(steuer_gegen))))
    {
      throw new ApplicationException(
          "Steuer bei Haupt- und Gegenbuchung müssen identisch sein");
    }
  }

  /**
   * Speichert alle Buchungen der Splitbuchung
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void store() throws RemoteException, ApplicationException
  {
    DBTransaction.starten();
    try
    {
      prepareStore();
      for (Buchung b : getBuchungen())
      {
        if (b.isToDelete())
        {
          b.delete();
        }
        else
        {
          b.store();
        }
      }
      if (hauptBuchungen != null && hauptBuchungen.length > 1)
      {
        Buchung hauptBuchung = hauptBuchungen[0];

        // ggf. Zweck und Kommentar übertragen
        HashMap<Buchung, Boolean> zweckeVonHauptbuchung = new HashMap<>();
        HashMap<Buchung, Boolean> kommentareVonHauptbuchung = new HashMap<>();
        Boolean zweckVonHauptbuchung = false;
        Boolean kommentarVonHauptbuchung = false;
        for (Buchung b : getBuchungen())
        {
          zweckVonHauptbuchung = false;
          kommentarVonHauptbuchung = false;
          if (!b.isToDelete() && (b.getSplitTyp() == SplitbuchungTyp.SPLIT))
          {
            if (hauptBuchung.getZweck() != null
                && hauptBuchung.getZweck().equals(b.getZweck()))
            {
              zweckVonHauptbuchung = true;
            }
            zweckeVonHauptbuchung.put(b, zweckVonHauptbuchung);
            if (hauptBuchung.getKommentar() != null
                && hauptBuchung.getKommentar().equals(b.getKommentar()))
            {
              kommentarVonHauptbuchung = true;
            }
            kommentareVonHauptbuchung.put(b, kommentarVonHauptbuchung);
          }
        }

        // Diese Plittung auf alle andern Buchungen übertragen
        Buchung split = null;
        for (Buchung master : hauptBuchungen)
        {
          master.setSplitTyp(SplitbuchungTyp.HAUPT);
          master.setSplitId(Long.valueOf(master.getID()));
          master.store();

          createGegenbuchung(master).store();

          for (Buchung b : getBuchungen())
          {
            if (!b.isToDelete() && (b.getSplitTyp() == SplitbuchungTyp.SPLIT))
            {
              split = getSplitbuchung(master, b, zweckeVonHauptbuchung,
                  kommentareVonHauptbuchung);
              split.store();
            }
          }
        }
        hauptBuchungen = null;
        splitbuchungen.clear();
      }
      DBTransaction.commit();
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  /**
   * Erstellt eine Gegenbuchung zur Hauptbuchung
   * 
   * @param hauptbuchung
   *          die zu kopierende Buchung
   * @return die neue erstellt (Split-) Buchung
   * @throws RemoteException
   */
  public Buchung createGegenbuchung(Buchung hauptbuchung) throws RemoteException
  {
    Buchung buch = createNewBuchung(null);
    buch.setAbrechnungslauf(hauptbuchung.getAbrechnungslauf());
    buch.setArt(hauptbuchung.getArt());
    buch.setAuszugsnummer(hauptbuchung.getAuszugsnummer());
    buch.setBetrag(hauptbuchung.getBetrag().negate());
    buch.setBlattnummer(hauptbuchung.getBlattnummer());
    buch.setBuchungsartId(hauptbuchung.getBuchungsartId());
    buch.setBuchungsklasseId(hauptbuchung.getBuchungsklasseId());
    buch.setDatum(hauptbuchung.getDatum());
    buch.setKommentar(hauptbuchung.getKommentar());
    buch.setKonto(hauptbuchung.getKonto());
    buch.setSollbuchungID(hauptbuchung.getSollbuchungID());
    buch.setName(hauptbuchung.getName());
    buch.setProjekt(hauptbuchung.getProjekt());
    buch.setSplitId(Long.valueOf(hauptbuchung.getID()));
    buch.setUmsatzid(hauptbuchung.getUmsatzid());
    buch.setZweck(hauptbuchung.getZweck());
    buch.setIban(hauptbuchung.getIban());
    buch.setSteuer(hauptbuchung.getSteuer());
    buch.setSplitTyp(SplitbuchungTyp.GEGEN);

    // Folgende Spalten werden nicht übertragen (Stand 4.1.4):
    // sollbuchung
    // splitid
    // splittyp
    // spendenbescheinigung
    // verzicht
    // abschluss
    // geprueft
    // unterlagenwertermittlung
    // herkunftspende
    // bezeichnungsachzuwendung

    return buch;
  }

  /**
   * Erstellt eine Splitbuchung
   * 
   * @param master
   *          Die Hauptbuchung
   * @param origin
   *          Nach diesem Vorbild soll die Buchung erstellt werden
   * @param zweckeVonHauptbuchung
   * @param kommentareVonHauptbuchung
   * @return
   * @throws RemoteException
   */
  private Buchung getSplitbuchung(Buchung master, Buchung origin,
      HashMap<Buchung, Boolean> zweckeVonHauptbuchung,
      HashMap<Buchung, Boolean> kommentareVonHauptbuchung)
      throws RemoteException
  {
    Buchung buch = createNewBuchung(null);
    buch.setAuszugsnummer(master.getAuszugsnummer());
    buch.setBetrag(origin.getBetrag());
    buch.setBlattnummer(master.getBlattnummer());
    buch.setBuchungsartId(origin.getBuchungsartId());
    buch.setBuchungsklasseId(origin.getBuchungsklasseId());
    buch.setDatum(master.getDatum());
    if (kommentareVonHauptbuchung.get(origin))
    {
      buch.setKommentar(master.getKommentar());
    }
    else
    {
      buch.setKommentar(origin.getKommentar());
    }
    buch.setKonto(master.getKonto());
    buch.setSollbuchungID(master.getSollbuchungID());
    buch.setName(master.getName());
    buch.setProjekt(master.getProjekt());
    buch.setSplitId(Long.valueOf(master.getID()));
    buch.setUmsatzid(master.getUmsatzid());
    if (zweckeVonHauptbuchung.get(origin))
    {
      buch.setZweck(master.getZweck());
    }
    else
    {
      buch.setZweck(origin.getZweck());
    }
    buch.setSteuer(origin.getSteuer());
    buch.setIban(master.getIban());
    buch.setSplitTyp(SplitbuchungTyp.SPLIT);
    return buch;
  }

  public int getAnzahl()
  {
    return anzahl;
  }

  /**
   * Entfernt eine Buchung aus der Splitbuchung
   * 
   * @param buchung
   */
  public void remove(Buchung buchung)
  {
    splitbuchungen.remove(buchung);
  }

}
