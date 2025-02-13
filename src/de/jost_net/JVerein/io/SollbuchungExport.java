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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.SollbuchungQuery;
import de.jost_net.JVerein.gui.action.SollbuchungExportAction.EXPORT_TYP;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.input.MailAuswertungInput.MailAuswertungObject;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public abstract class SollbuchungExport implements Exporter
{

  @Override
  public abstract String getName();

  @Override
  public abstract IOFormat[] getIOFormats(Class<?> objectType);

  protected EXPORT_TYP exportTyp = EXPORT_TYP.MITGLIEDSKONTO;

  protected File file;

  protected SollbuchungControl control = new SollbuchungControl(null);

  @Override
  public void doExport(final Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws DocumentException, IOException, ApplicationException
  {
    this.file = file;
    this.control.getSuchname().setValue(objects[0]);
    this.control.getDifferenz().setValue(objects[1]);
    this.control.getOhneAbbucher().setValue(objects[2]);
    this.control.getDatumvon().setValue(objects[3]);
    this.control.getDatumbis().setValue(objects[4]);
    this.control.getMailauswahl()
        .setValue(new MailAuswertungObject((int) objects[5]));

    open();

    DBIterator<Mitglied> mitgl = Einstellungen.getDBService()
        .createList(Mitglied.class);
    mitgl.setOrder("ORDER BY name, vorname");

    while (mitgl.hasNext())
    {
      Mitglied m = (Mitglied) mitgl.next();
      startMitglied(m);
      GenericIterator<Sollbuchung> sollbuchnungen = new SollbuchungQuery(
          control, false, m).get();
      if (sollbuchnungen != null)
      {
        while (sollbuchnungen.hasNext())
        {
          add(sollbuchnungen.next());
          monitor.log("Vorbereitung: " + Adressaufbereitung.getNameVorname(m));
        }
      }
      endeMitglied();
    }
    close(monitor);
  }

  public void setExportTyp(EXPORT_TYP typ)
  {
    exportTyp = typ;
  }

  @Override
  public String getDateiname()
  {
    return exportTyp.getDateiName();
  }

  protected abstract void startMitglied(Mitglied m) throws DocumentException;

  protected abstract void endeMitglied() throws DocumentException;

  protected abstract void open()
      throws DocumentException, FileNotFoundException;

  protected abstract void add(Sollbuchung sollb) throws RemoteException;

  protected abstract void close(ProgressMonitor monitor)
      throws IOException, DocumentException;
}
