/**********************************************************************
 * basiert auf KontoList aus Hibiscus
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

package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.BuchungsControl.Kontenfilter;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste aller Konten.
 */
public class KontoList extends TablePart implements Part
{
  public KontoList(Action action, boolean onlyHibiscus,
      boolean nurAktuelleKonten, Kontenfilter art) throws RemoteException
  {
    this(init(onlyHibiscus, nurAktuelleKonten, art), action);
  }

  public KontoList(List<Konto> konten, Action action)
  {
    super(konten, action);

    addColumn("Kontonummer", "nummer");
    addColumn("Bezeichnung", "bezeichnung");
    setRememberOrder(true);
    setRememberColWidths(true);

  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }

  /**
   * Update Konten-Liste nach neuen Kriterien.
   * 
   * @param onlyHibiscus, nurAktuelleKonten
   * @throws RemoteException
   */ 
  public synchronized void update(boolean onlyHibiscus,
	      boolean nurAktuelleKonten, Kontenfilter art) throws RemoteException
  {
    super.removeAll();
    List<Konto> list = init(onlyHibiscus, nurAktuelleKonten, art);
    for (Konto kto: list) 
    {
      super.addItem(kto);
    }
  }
  
  /**
   * Initialisiert die Konten-Liste.
   * 
   * @return Liste der Konten.
   * @throws RemoteException
   */
  @SuppressWarnings("unchecked")
  private static List<Konto> init(boolean onlyHibiscus,
      boolean nurAktuelleKonten, Kontenfilter art) throws RemoteException
  {
    DBIterator<Konto> i = Einstellungen.getDBService().createList(Konto.class);
    if (onlyHibiscus)
    {
      i.addFilter("hibiscusid > -1");
    }
    if (nurAktuelleKonten)
    {
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      year = year - Einstellungen.getEinstellung().getUnterdrueckungKonten();
      i.addFilter("(aufloesung is null or year(aufloesung) >= ?)", year);
    }
    if (art == Kontenfilter.GELDKONTO)
      i.addFilter("kontoart != ?",
          new Object[] { Kontoart.ANLAGE.getKey() });
    if (art == Kontenfilter.ANLAGEKONTO)
      i.addFilter("kontoart = ?",
          new Object[] { Kontoart.ANLAGE.getKey() });
    i.setOrder("ORDER BY nummer, bezeichnung");
    return i != null ? PseudoIterator.asList(i) : null;
  }
}
