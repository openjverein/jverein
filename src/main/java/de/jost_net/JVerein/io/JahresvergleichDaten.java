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
package de.jost_net.JVerein.io;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Datenmodell für den Vergleich zweier Geschäftsjahre. */
public final class JahresvergleichDaten
{
  public enum Abschnitt
  {
    GESAMT, BUCHUNGSART, PROJEKT
  }

  public enum Kennzahl
  {
    EINNAHMEN("Einnahmen"), AUSGABEN("Ausgaben"),
    UMBUCHUNGEN("Umbuchungen"), SALDO("Saldo");

    private final String bezeichnung;

    Kennzahl(String bezeichnung)
    {
      this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung()
    {
      return bezeichnung;
    }
  }

  public enum Zeilentyp
  {
    NORMAL, GRUPPENKOPF, ZWISCHENSUMME
  }

  public static final class Vergleichszeile
  {
    private final Abschnitt abschnitt;
    private final String name;
    private final String gruppe;
    private final Kennzahl kennzahl;
    private final Zeilentyp typ;
    private final BigDecimal aktuell;
    private final BigDecimal vorjahr;

    public Vergleichszeile(Abschnitt abschnitt, String name,
        Kennzahl kennzahl, BigDecimal aktuell, BigDecimal vorjahr)
    {
      this(abschnitt, null, name, kennzahl, aktuell, vorjahr, Zeilentyp.NORMAL);
    }

    private Vergleichszeile(Abschnitt abschnitt, String gruppe, String name,
        Kennzahl kennzahl, BigDecimal aktuell, BigDecimal vorjahr,
        Zeilentyp typ)
    {
      this.abschnitt = abschnitt;
      this.gruppe = gruppe;
      this.name = name;
      this.kennzahl = kennzahl;
      this.typ = typ;
      this.aktuell = nullToZero(aktuell);
      this.vorjahr = nullToZero(vorjahr);
    }

    public Abschnitt getAbschnitt()
    {
      return abschnitt;
    }

    public String getName()
    {
      return name;
    }

    public Kennzahl getKennzahl()
    {
      return kennzahl;
    }

    public String getGruppe()
    {
      return gruppe;
    }

    public Zeilentyp getTyp()
    {
      return typ;
    }

    public BigDecimal getAktuell()
    {
      return aktuell;
    }

    public BigDecimal getVorjahr()
    {
      return vorjahr;
    }

    public BigDecimal getVeraenderung()
    {
      return aktuell.subtract(vorjahr);
    }

    public String getVeraenderungProzent()
    {
      if (vorjahr.signum() == 0)
      {
        return "–";
      }
      BigDecimal prozent = getVeraenderung().multiply(BigDecimal.valueOf(100))
          .divide(vorjahr, 2, RoundingMode.HALF_UP);
      DecimalFormat format = new DecimalFormat("0.00",
          DecimalFormatSymbols.getInstance(Locale.GERMANY));
      return format.format(prozent) + " %";
    }
  }

  /** Aggregierte Werte einer Buchungsart oder eines Projekts. */
  public static final class Werte
  {
    private BigDecimal einnahmen = BigDecimal.ZERO;
    private BigDecimal ausgaben = BigDecimal.ZERO;
    private BigDecimal umbuchungen = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;

    public void add(Kennzahl kennzahl, BigDecimal betrag)
    {
      BigDecimal wert = nullToZero(betrag);
      switch (kennzahl)
      {
        case EINNAHMEN:
          einnahmen = einnahmen.add(wert);
          break;
        case AUSGABEN:
          ausgaben = ausgaben.add(wert);
          break;
        case UMBUCHUNGEN:
          umbuchungen = umbuchungen.add(wert);
          break;
        default:
          break;
      }
      if (kennzahl != Kennzahl.SALDO)
      {
        saldo = saldo.add(wert);
      }
    }

    public BigDecimal get(Kennzahl kennzahl)
    {
      switch (kennzahl)
      {
        case EINNAHMEN:
          return einnahmen;
        case AUSGABEN:
          return ausgaben;
        case UMBUCHUNGEN:
          return umbuchungen;
        default:
          return saldo;
      }
    }
  }

  private final List<Vergleichszeile> zeilen = new ArrayList<>();

  public void add(Abschnitt abschnitt, String name, Kennzahl kennzahl,
      BigDecimal aktuell, BigDecimal vorjahr)
  {
    if (nullToZero(aktuell).signum() == 0
        && nullToZero(vorjahr).signum() == 0)
    {
      return;
    }
    zeilen.add(new Vergleichszeile(abschnitt, null, name, kennzahl, aktuell,
        vorjahr, Zeilentyp.NORMAL));
  }

  private void add(Abschnitt abschnitt, String gruppe, String name,
      Kennzahl kennzahl, BigDecimal aktuell, BigDecimal vorjahr,
      Zeilentyp typ, boolean auchWennNull)
  {
    if (!auchWennNull && nullToZero(aktuell).signum() == 0
        && nullToZero(vorjahr).signum() == 0)
    {
      return;
    }
    zeilen.add(new Vergleichszeile(abschnitt, gruppe, name, kennzahl, aktuell,
        vorjahr, typ));
  }

  private void addGruppenkopf(Abschnitt abschnitt, String name)
  {
    add(abschnitt, null, name, null, BigDecimal.ZERO, BigDecimal.ZERO,
        Zeilentyp.GRUPPENKOPF, true);
  }

  private void addZwischensumme(Abschnitt abschnitt, String gruppe,
      BigDecimal aktuell, BigDecimal vorjahr)
  {
    add(abschnitt, gruppe, "Saldo Buchungsklasse", Kennzahl.SALDO, aktuell,
        vorjahr, Zeilentyp.ZWISCHENSUMME, true);
  }

  public void addBereich(Abschnitt abschnitt, Map<String, String> namen,
      Map<String, Werte> aktuelleWerte, Map<String, Werte> vorjahresWerte)
  {
    addBereich(abschnitt, namen, aktuelleWerte, vorjahresWerte, true);
  }

  public void addBuchungsklassenBereich(Abschnitt abschnitt,
      Map<String, String> gruppenNamen,
      Map<String, Map<String, String>> buchungsartNamen,
      Map<String, Map<String, Werte>> aktuelleWerte,
      Map<String, Map<String, Werte>> vorjahresWerte,
      boolean detaillierteKennzahlen)
  {
    Collection<String> gruppen = new LinkedHashSet<>();
    gruppen.addAll(aktuelleWerte.keySet());
    gruppen.addAll(vorjahresWerte.keySet());
    for (String gruppenKey : gruppen)
    {
      Map<String, Werte> aktuell = aktuelleWerte.getOrDefault(gruppenKey,
          Map.of());
      Map<String, Werte> vorjahr = vorjahresWerte.getOrDefault(gruppenKey,
          Map.of());
      Collection<String> arten = new LinkedHashSet<>();
      arten.addAll(aktuell.keySet());
      arten.addAll(vorjahr.keySet());

      int gruppenStart = zeilen.size();
      Werte summeAktuell = new Werte();
      Werte summeVorjahr = new Werte();
      boolean hatWerte = false;
      for (String artKey : arten)
      {
        Werte aktuelleArt = aktuell.getOrDefault(artKey, new Werte());
        Werte vorjahresArt = vorjahr.getOrDefault(artKey, new Werte());
        for (Kennzahl kennzahl : Kennzahl.values())
        {
          BigDecimal aktuellerWert = aktuelleArt.get(kennzahl);
          BigDecimal vorjahresWert = vorjahresArt.get(kennzahl);
          if (aktuellerWert.signum() != 0 || vorjahresWert.signum() != 0)
          {
            hatWerte = true;
          }
          if (kennzahl != Kennzahl.SALDO)
          {
            summeAktuell.add(kennzahl, aktuellerWert);
            summeVorjahr.add(kennzahl, vorjahresWert);
          }
          if (detaillierteKennzahlen || kennzahl == Kennzahl.SALDO)
          {
            String artName = buchungsartNamen.getOrDefault(gruppenKey,
                Map.of()).getOrDefault(artKey, artKey);
            add(abschnitt, gruppenNamen.getOrDefault(gruppenKey, gruppenKey),
                artName, kennzahl, aktuellerWert, vorjahresWert,
                Zeilentyp.NORMAL, false);
          }
        }
      }
      if (hatWerte)
      {
        // Die Gruppenüberschrift folgt vor den zugehörigen Buchungsarten.
        addGruppenkopf(abschnitt,
            gruppenNamen.getOrDefault(gruppenKey, gruppenKey));
        Vergleichszeile kopf = zeilen.remove(zeilen.size() - 1);
        zeilen.add(gruppenStart, kopf);
        addZwischensumme(abschnitt,
            gruppenNamen.getOrDefault(gruppenKey, gruppenKey),
            summeAktuell.get(Kennzahl.SALDO), summeVorjahr.get(Kennzahl.SALDO));
      }
    }
  }

  public void addBereich(Abschnitt abschnitt, Map<String, String> namen,
      Map<String, Werte> aktuelleWerte, Map<String, Werte> vorjahresWerte,
      boolean detaillierteKennzahlen)
  {
    Collection<String> keys = new LinkedHashSet<>();
    keys.addAll(aktuelleWerte.keySet());
    keys.addAll(vorjahresWerte.keySet());
    for (String key : keys)
    {
      Werte aktuell = aktuelleWerte.getOrDefault(key, new Werte());
      Werte vorjahr = vorjahresWerte.getOrDefault(key, new Werte());
      for (Kennzahl kennzahl : Kennzahl.values())
      {
        if (!detaillierteKennzahlen && kennzahl != Kennzahl.SALDO)
        {
          continue;
        }
        add(abschnitt, namen.getOrDefault(key, key), kennzahl,
            aktuell.get(kennzahl),
            vorjahr.get(kennzahl));
      }
    }
  }

  public List<Vergleichszeile> getZeilen(Abschnitt abschnitt)
  {
    return zeilen.stream().filter(z -> z.getAbschnitt() == abschnitt).toList();
  }

  private static BigDecimal nullToZero(BigDecimal wert)
  {
    return wert == null ? BigDecimal.ZERO : wert;
  }
}
