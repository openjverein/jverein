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
package de.jost_net.JVerein.rmi;

import de.jost_net.JVerein.util.IbanUtil;
import de.speedbanking.iban.Iban;
import de.speedbanking.iban.InvalidIbanException;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.logging.Logger;

public class MitgliedAddress implements Address
{
  private String name = null;

  private String kommentar = null;

  private String bic = null;

  private String iban = null;

  private Iban ib = null;

  private String kategorie = null;

  public MitgliedAddress()
  {
    //
  }

  public MitgliedAddress(String name, String kommentar, String bic, String iban,
      String kategorie)
  {
    this.name = name;
    this.kommentar = kommentar;
    this.bic = bic;
    this.iban = iban;
    try
    {
      ib = IbanUtil.parse(iban);
    }
    catch (InvalidIbanException e)
    {
      Logger.error("Fehler: ", e);
    }
    this.kategorie = kategorie;
  }

  @Override
  public String getKontonummer()
  {
    return ib.getAccountNumber();
  }

  @Override
  public String getBlz()
  {
    return ib.getBankCode();
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getKommentar()
  {
    return kommentar;
  }

  @Override
  public String getBic()
  {
    return bic;
  }

  @Override
  public String getIban()
  {
    return iban;
  }

  @Override
  public String getKategorie()
  {
    return kategorie;
  }

}
