/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzAuswertungPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.Auswertung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Suchjahr;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ArbeitseinsatzAuswertungControl extends FilterControl
{
  private ArbeitseinsatzAuswertungPart auswertungPart;

  public ArbeitseinsatzAuswertungControl(AbstractView view)
  {
    super(view);
  }

  public Button dialogExportButton(ExportArt art) throws ApplicationException
  {
    return new Button(art.equals(ExportArt.PDF) ? "PDF" : "CSV", context -> {
      try
      {
        ((FilterControl) this).refresh();
        getTablePart().export(getTableTitle(), getTableSubtitle(),
            getTableDateiname(), art, getFilterText(false));
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (ObjectNotFoundException e)
      {
        throw new ApplicationException("Keine Tabelle vorhanden!");
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Tabellen-Export", e);
        throw new ApplicationException("Fehler beim Tabellen-Export");
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
    }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  public Button getAbbrechenButton(AbstractDialog<Boolean> dialog)
      throws RemoteException
  {
    Button b = new Button("Abbrechen", context -> {
      dialog.close();
    }, null, false, "process-stop.png");
    return b;
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (auswertungPart != null)
    {
      return auswertungPart.getArbeitseinsatzUeberpruefungList();
    }
    else
    {
      auswertungPart = new ArbeitseinsatzAuswertungPart(null,
          (Auswertung) getFilterValue(Filter.AUSWERTUNG),
          ((Suchjahr) getFilterValue(Filter.SUCHJAHR)).getKey());
      return auswertungPart.getArbeitseinsatzUeberpruefungList();
    }
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    auswertungPart.refreshList((Auswertung) getFilterValue(Filter.AUSWERTUNG),
        ((Suchjahr) getFilterValue(Filter.SUCHJAHR)).getKey());
  }

  @Override
  protected String getTableTitle() throws ApplicationException
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_TITEL,
        this);
  }

  @Override
  protected String getTableSubtitle() throws ApplicationException
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL,
        this);
  }

  @Override
  protected String getTableDateiname() throws ApplicationException
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME,
        this);
  }

}
