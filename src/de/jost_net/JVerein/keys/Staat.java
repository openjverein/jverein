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
package de.jost_net.JVerein.keys;

import java.rmi.RemoteException;

public enum Staat
{

  AB("ABCHASIEN"),
  AF("AFGANISTAN"),
  EG("ÄGYPTEN"),
  AL("ALBANIEN"),
  DZ("ALGERIEN"),
  AD("ANDORRA"),
  AO("ANGOLA"),
  AG("ANTIGUA UND BARBUDA"),
  GQ("ÄQUATORIALGUINEA"),
  AR("ARGENTINIEN"),
  AM("ARMENIEN"),
  AZ("ASERBAIDSCHAN"),
  ET("ÄTHIOPIEN"),
  AU("AUSTRALIEN"),
  BS("BAHAMAS"),
  BH("BAHRAIN"),
  BD("BANGLADESCH"),
  BB("BARBADOS"),
  BY("BELARUS"),
  BE("BELGIEN"),
  BZ("BELIZE"),
  BJ("BENIN"),
  // Bergkarabach (Armenien/Aserbaidschan)
  // AR("BERGKARABACH"),Kein ISO oder ISO ähnlicher Code vorhanden
  BT("BHUTAN"),
  BO("BOLIVIEN"),
  BA("BOSNIEN UND HERZEGOWINA"),
  BW("BOTSWANA"),
  BR("BRASILIEN"),
  BN("BRUNEI"),
  BG("BULGARIEN"),
  BF("BURKINA FASO"),
  BI("BURUNDI"),
  CV("CABO VERDE"),
  CL("CHILE"),
  CN("CHINA"),
  CR("COSTA RICA"),
  CI("CÔTE D`IVOIRE"),
  DK("DÄNEMARK"),
  CD("DEMOKRATISCHE REPUBLIK KONGO"),
  DE("DEUTSCHLAND"),
  DM("DOMINICA"),
  DO("DOMINIKANISCHE REPUBLIK"),
  DJ("DSCHIBUTI"),
  EC("ECUADOR"),
  SV("EL SALVADOR"),
  ER("ERITREA"),
  EE("ESTLAND"),
  SZ("ESWATINI"),
  FJ("FIDSCHI"),
  FI("FINNLAND"),
  FR("FRANKREICH"),
  GA("GABUN"),
  GM("GAMBIA"),
  GE("GEORGIEN"),
  GH("GHANA"),
  GD("GRENADA"),
  GR("GRIECHENLAND"),
  GT("GUATEMALA"),
  GN("GUINEA"),
  GW("GUINEA-BISSAU"),
  GY("GUYANA"),
  HT("HAITI"),
  HN("HONDURAS"),
  IN("INDIEN"),
  ID("INDONESIEN"),
  IQ("IRAK"),
  IR("IRAN"),
  IE("IRLAND"),
  IS("ISLAND"),
  IL("ISRAEL"),
  IT("ITALIEN"),
  JM("JAMAIKA"),
  JP("JAPAN"),
  YE("JEMEN"),
  JO("JORDANIEN"),
  KH("KAMBODSCHA"),
  CM("KAMERUN"),
  CA("KANADA"),
  KZ("KASACHSTAN"),
  QA("KATAR"),
  KE("KENIA"),
  KG("KIRGISISTAN"),
  KI("KIRIBATI"),
  CO("KOLOMBIEN"),
  KM("KOMOREN"),
  CG("KONGO"),
  XK("KOSOVO"),
  HR("KROATIEN"),
  CU("KUBA"),
  KW("KUWAIT"),
  LA("LAOS"),
  LS("LESOTHO"),
  LV("LETTLAND"),
  LB("LIBANON"),
  LR("LIBERIA"),
  LY("LIBYEN"),
  LI("LIECHTENSTEIN"),
  LT("LITAUEN"),
  LU("LUXEMBURG"),
  MG("MADAGASKAR"),
  MW("MALAWI"),
  MY("MALAYSIA"),
  MV("MALEDIVEN"),
  ML("MALI"),
  MT("MALTA"),
  MA("MAROKKO"),
  MH("MARSHALLINSELN"),
  MR("MAURETANIEN"),
  MU("MAURITIUS"),
  MX("MEXIKO"),
  FM("MIKRONESIEN"),
  MD("MOLDAU"),
  MC("MONACO"),
  MN("MONGOLEI"),
  ME("MONTENEGRO"),
  MZ("MOSAMBIK"),
  MM("MYANMAR"),
  NA("NAMIBIA"),
  NR("NAURU"),
  NP("NEPAL"),
  NZ("NEUSEELAND"),
  NI("NICARAGUA"),
  NL("NIEDERLANDE"),
  NE("NIGER"),
  NG("NIGERIA"),
  MK("NORDMAZEDONIEN"),
  // Nordzypern (Türkei)
  CTR("NORDZYPERN"),
  NO("NORWEGEN"),
  OM("OMAN"),
  AT("ÖSTERREICH"),
  PK("PAKISTAN"),
  PS("PALÄSTINA"),
  PW("PALAU"),
  PA("PANAMA"),
  PG("PAPUA-NEUGUINEA"),
  PY("PARAGUAY"),
  PE("PERU"),
  PH("PHILIPPINEN"),
  PL("POLEN"),
  PT("PORTUGAL"),
  RW("RUANDA"),
  RO("RUMÄNIEN"),
  RU("RUSSLAND"),
  SB("SALOMONEN"),
  ZM("SAMBIA"),
  WS("SAMOA"),
  SM("SAN MARINO"),
  ST("SÃO TOMÉ UND PRÍNCIPE"),
  SA("SAUDI-ARABIEN"),
  SE("SCHWEDEN"),
  CH("SCHWEIZ"),
  SN("SENEGAL"),
  RS("SERBIEN"),
  SC("SEYCHELLEN"),
  SL("SIERRA LEONE"),
  ZW("SIMBABWE"),
  SG("SINGAPUR"),
  SK("SLOWAKEI"),
  SI("SLOWENIEN"),
  SO("SOMALIA"),
  ES("SPANIEN"),
  LK("SRI LANKA"),
  KN("ST. KITTS UND NEVIS"),
  LC("ST. LUCIA"),
  VC("ST. VINCENT UND DIE GRENADINEN"),
  ZA("SÜDAFRIKA"),
  SD("SUDAN"),
  KR("SÜDKOREA"),
  // Südossetien (Georgien)
  SOS("SÜDOSSETIEN"),
  SS("SÜDSUDAN"),
  SR("SURINAME"),
  SY("SYRIEN"),
  TJ("TADSCHIKISTAN"),
  TW("TAIWAN"),
  TZ("TANSANIA"),
  TH("THAILAND"),
  TL("TIMOR-LESTE"),
  TG("TOGO"),
  TO("TONGA"),
  // Transnistrien (Republik Moldau)
  PMR("TRANSNISTRIEN"),
  TT("TRINIDAD UND TOBAGO"),
  TD("TSCHAD"),
  CZ("TSCHECHIEN"),
  TN("TUNESIEN"),
  TR("TÜRKEI"),
  TM("TURKMENISTAN"),
  TV("TUVALU"),
  UG("UGANDA"),
  UA("UKRAINE"),
  HU("UNGARN"),
  UY("URUGUAY"),
  UZ("USBEKISTAN"),
  VU("VANUATU"),
  VA("VATIKANSTADT"),
  VE("VENEZUELA"),
  AE("VEREINIGTE ARABISCHE EMIRATE"),
  US("VEREINIGTE STAATEN"),
  GB("VEREINIGTES KÖNIGREICH"),
  VN("VIETNAM"),
  CF("ZENTRALAFRIKANISCHE REPUBLIK"),
  CY("ZYPERN");

  private final String text;

  Staat(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  public String getKey()
  {
    return this.name();
  }

  public static Staat getByKey(String key)
  {
    if (key == null)
    {
      return null;
    }
    for (Staat s : Staat.values())
    {
      if (s.getKey().equals(key.toUpperCase()))
      {
        return s;
      }
    }
    return null;
  }

  public static Staat getByText(String text)
  {
    if (text == null)
    {
      return null;
    }
    for (Staat s : Staat.values())
    {
      if (s.getText().equals(text.toUpperCase()))
      {
        return s;
      }
    }
    return null;
  }

  public static String getStaat(String code) throws RemoteException
  {
    if (Staat.getByKey(code) != null)
    {
      return Staat.getByKey(code).getText();
    }
    // Wenn der Code nicht vorhanden ist, nehmen wir
    // zur Kompatibilität den Text wie er in der DB Steht
    return code;
  }

  public static String getStaatCode(String code) throws RemoteException
  {
    // Wenn noch das ganze Land drin steht, bestimmen wir den Code
    if (Staat.getByText(code) != null)
      return Staat.getByText(code).getKey();
    return code;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
