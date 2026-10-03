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

import java.util.ArrayList;
import java.util.List;

import de.speedbanking.iban.IbanRegistry;

/**
 * Liste der SEPA-Länder (Ersatz für OBanToos {@code SEPALaender}), auf Basis
 * von {@link IbanRegistry#getSepaCountries()}.
 *
 * @since 4.3.0
 */
public final class SEPALaender
{
  private SEPALaender()
  {
    throw new UnsupportedOperationException(String.format(
        "Utility class %s cannot be instantiated", getClass().getSimpleName()));
  }

  // Liefert NULL bei unbekanntem Kennzeichen oder einem Land ohne SEPA.
  public static SEPALand getLand(String kennzeichen)
  {
    IbanRegistry registry = IbanRegistry.getByCode(kennzeichen);
    if (registry == null || !registry.isSepa())
    {
      return null;
    }
    return new SEPALand(registry);
  }

  public static List<SEPALand> getLaender()
  {
    List<SEPALand> laender = new ArrayList<>();
    for (IbanRegistry registry : IbanRegistry.getSepaCountries())
    {
      laender.add(new SEPALand(registry));
    }
    return laender;
  }
}
