package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart.ExportArt;
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
            getTablePart().export(getTableTitle(), getTableSubtitle(),
                getTableDateiname(), getTableSettingPrefix(), art);
          }
          catch (OperationCanceledException e)
          {
            throw e;
          }
          catch (ApplicationException e)
          {
            throw e;
          }
          catch (Exception e)
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
   */
  protected abstract JVereinTablePart getTablePart() throws RemoteException;

  /**
   * Liefert den Titel für die Tabellenreports
   * 
   * @return
   */
  protected String getTableTitle()
  {
    return "";
  }

  /**
   * Liefert den Subtitel für die Tabellenreports
   * 
   * @return
   */
  protected String getTableSubtitle()
  {
    return "";
  }

  /**
   * Liefert den Dateinamen für die Tabellenreports
   * 
   * @return
   */
  protected String getTableDateiname()
  {
    return "";
  }

  /**
   * Liefert den Settingsprefix für die Tabellenreports
   * 
   * @return
   */
  protected String getTableSettingPrefix()
  {
    return "";
  }
}
