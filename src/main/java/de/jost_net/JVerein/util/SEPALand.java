/**********************************************************************
 * Copyright (c) by Markus Spann
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
package de.jost_net.JVerein.util;

import java.util.Locale;

import de.speedbanking.iban.IbanRegistry;

/**
 * Ein SEPA-Land. Kapselt {@link IbanRegistry} (Ersatz für OBanToos
 * {@code SEPALand}, siehe {@link SEPALaender}), hält die iban-commons-API
 * aber bewusst aus dem Rest von JVerein heraus.
 *
 * @since 4.3.0
 */
public final class SEPALand
{
  private final IbanRegistry registry;

  private final String bezeichnung;

  SEPALand(IbanRegistry registry)
  {
    this.registry = registry;
    Locale locale = new Locale.Builder().setRegion(registry.getCountryCode())
        .build();
    this.bezeichnung = locale.getDisplayCountry(Locale.GERMAN);
  }

  public String getKennzeichen()
  {
    return registry.getCountryCode();
  }

  public String getBezeichnung()
  {
    return bezeichnung;
  }
}
