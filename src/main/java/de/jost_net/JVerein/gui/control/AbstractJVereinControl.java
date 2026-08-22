package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.TabelleProfilAuswahlDialog;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
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

  public PanelButton getProfilePanelButton()
  {
    return new PanelButton("user-check.png", context -> {
      try
      {
        new TabelleProfilAuswahlDialog(getTablePart()).open();
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
        Logger.error("Fehler beim Profil-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Profil-Auswahl-Dialog");
      }
    }, "Profile");
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
    return new PanelButton(
        art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png", context -> {
          try
          {
            // Gefilterte Listen müssen vor dem Export aktuallisiert werden,
            // dann sonst stimmen ggf. die Filter nicht mit dem Inhalt der Liste
            // überein.
            if (this instanceof FilterControl)
            {
              ((FilterControl) this).refresh();
            }
            // TODO BuchungsControl ist noch nicht Teil von FilterControl und
            // brauch noch eine extra Behandlung
            else if (this instanceof BuchungsControl)
            {
              ((BuchungsControl) this).refreshBuchungsList();
            }
            getTablePart().export(getTableTitle(), getTableSubtitle(),
                getTableDateiname(), art);
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
        }, art.equals(ExportArt.PDF) ? "PDF" : "CSV");
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
  abstract protected String getTableTitle();

  /**
   * Liefert den Subtitel für die Tabellenreports
   * 
   * @return
   */
  abstract protected String getTableSubtitle();

  /**
   * Liefert den Dateinamen für die Tabellenreports
   * 
   * @return
   */
  abstract protected String getTableDateiname();
}
