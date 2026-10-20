package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.Map;

import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinPanelButton;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.server.AbstractJVereinDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractJVereinControl extends AbstractControl
{

  public AbstractJVereinControl(AbstractView view)
  {
    super(view);
  }

  public PanelButton getSpaltenPanelButton()
  {
    return new PanelButton("document-properties.png", context -> {
      try
      {
        new TabelleSpaltenAuswahlDialog(getTablePart()).open();
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (ObjectNotFoundException e)
      {
        throw new ApplicationException("Keine Tabelle vorhanden!");
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Spalten-Auswahl-Dialog");
      }
    }, "Spalten auswählen");
  }

  public PanelButton exportButton(ExportArt art) throws ApplicationException
  {
    return new JVereinPanelButton(
        art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png", context -> {
          try
          {
            Map<Filter, String> filtertext = null;
            // Gefilterte Listen müssen vor dem Export aktuallisiert werden,
            // dann sonst stimmen ggf. die Filter nicht mit dem Inhalt der Liste
            // überein.
            if (this instanceof FilterControl)
            {
              ((FilterControl) this).refresh();
              // Nur wenn der Liste View auch Filter Inputs erzeugt hat
              if (!((FilterControl) this).isFilterMapEmpty())
              {
                filtertext = ((FilterControl) this).getFilterText(false, true);
              }
            }
            getTablePart().export(getTableTitle(), getTableSubtitle(),
                getTableDateiname(), art, filtertext);
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
        }, art.equals(ExportArt.PDF) ? "PDF" : "CSV",
        art.equals(ExportArt.PDF) ? "CTRL+P" : null);
  }

  /**
   * Holten den TablePart mit der Auflistung aller Objecte
   * 
   * @return
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected abstract IJVereinPart getTablePart()
      throws RemoteException, ApplicationException;

  /**
   * Liefert den Titel für die Tabellenreports
   * 
   * @return
   */
  abstract protected String getTableTitle() throws ApplicationException;

  /**
   * Liefert den Subtitel für die Tabellenreports
   * 
   * @return
   */
  abstract protected String getTableSubtitle() throws ApplicationException;

  /**
   * Liefert den Dateinamen für die Tabellenreports
   * 
   * @return
   */
  abstract protected String getTableDateiname() throws ApplicationException;

  public Column getGesperrtColumn()
  {
    return new Column(AbstractJVereinDBObject.GESPERRT,
        AbstractJVereinDBObject.GESPERRT);
  }

  public Column getAbgeschlossenColumn()
  {
    return new Column(AbstractJVereinDBObject.ABGESCHLOSSEN,
        AbstractJVereinDBObject.ABGESCHLOSSEN);
  }
}
