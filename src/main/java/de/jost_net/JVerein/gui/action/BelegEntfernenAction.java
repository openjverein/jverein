package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.Messaging.BelegRemoveMessage;
import de.jost_net.JVerein.gui.dialogs.JVereinYesNoDialog;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.IBeleg;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegEntfernenAction implements Action
{

  private IBeleg belegContext;

  public BelegEntfernenAction(IBeleg belegContext)
  {
    this.belegContext = belegContext;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    JVereinYesNoDialog d = new JVereinYesNoDialog(YesNoDialog.POSITION_CENTER);

    d.setTitle("Beleg entfernen");
    String text = "";
    BuchungDokument[] belege;
    if (context instanceof BuchungDokument[])
    {
      belege = (BuchungDokument[]) context;
      text = "Sollen die Dokumente wirklich entfernt werden?";
    }
    else if (context instanceof BuchungDokument)
    {
      belege = new BuchungDokument[] { (BuchungDokument) context };
      text = "Soll das Dokument wirklich entfernt werden?";
    }
    else
    {
      throw new ApplicationException("Falscher Kontext");
    }
    d.setText(text);
    try
    {
      if ((boolean) d.open())
      {
        for (BuchungDokument beleg : belege)
        {
          belegContext.removeBeleg((BuchungDokument) beleg);
          Application.getMessagingFactory()
              .sendMessage(new BelegRemoveMessage(beleg));
        }
        GUI.getStatusBar().setSuccessText("Erfolgreich entfernt");
      }
    }
    catch (ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim entfernen", e);
      throw new ApplicationException("Fehler beim entfernen");
    }
  }
}
