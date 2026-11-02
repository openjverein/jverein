package de.jost_net.JVerein.gui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialog.BelegCheckDialog;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.util.ApplicationException;

public class BelegCheckAction implements Action
{

  public enum FehlerArt
  {
    DATEI_FEHLT,
    CHECKSUM_FEHLT,
    CHECKSUM_FALSCH
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      DBIterator<Beleg> it = Einstellungen.getDBService()
          .createList(Beleg.class);

      Map<Beleg, FehlerArt> fehlerMap = new HashMap<>();
      while (it.hasNext())
      {
        Beleg beleg = it.next();
        try
        {
          beleg.getFile();
        }
        catch (IOException | ApplicationException e)
        {
          fehlerMap.put(beleg, FehlerArt.DATEI_FEHLT);
          continue;
        }
        Boolean ergebnis = beleg.checkChecksum();
        if (ergebnis == null)
        {
          fehlerMap.put(beleg, FehlerArt.CHECKSUM_FEHLT);
        }
        else if (!ergebnis)
        {
          fehlerMap.put(beleg, FehlerArt.CHECKSUM_FALSCH);
        }
      }
      if (fehlerMap.isEmpty())
      {
        SimpleDialog dialog = new SimpleDialog(SimpleDialog.POSITION_CENTER);
        dialog.setText("Die Dateien aller " + it.size()
            + " Belege sind vorhanden und haben korrekte Hash-Werte.");
        dialog.setTitle("Belege geprüft");
        dialog.open();
      }
      else
      {
        new BelegCheckDialog(fehlerMap).open();
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler beim prüfen der Hash-Werte: " + e.getMessage());
    }
  }

}
