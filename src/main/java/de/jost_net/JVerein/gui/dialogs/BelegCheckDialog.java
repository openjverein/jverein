package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.BelegCheckAction.FehlerArt;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;

public class BelegCheckDialog extends AbstractDialog<Boolean>
{

  private Map<Beleg, FehlerArt> fehlerMap;

  public BelegCheckDialog(Map<Beleg, FehlerArt> fehlerMap)
  {
    super(BelegCheckDialog.POSITION_CENTER);
    setTitle("Belege geprüft");
    this.fehlerMap = fehlerMap;
    setSize(850, 600);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent, true);
    c.addText("Belege geprüft, folgende Fehler wurden gefunden", true);

    JVereinTablePart list = new JVereinTablePart(
        new ArrayList<>(fehlerMap.entrySet()), null);
    list.addColumn("Fehler", "value.name");
    list.addColumn("Belegnummer", "key.belegnummer");
    list.addColumn("Datum", "key.datum");
    list.addColumn("Pfad", "key.pfad");
    list.addColumn("Art", "key.belegart",
        i -> BelegArt.getByKey((Integer) i).toString());

    list.paint(c.getComposite());

    ButtonArea buttons = new ButtonArea();
    if (fehlerMap.values().contains(FehlerArt.PREUEFSUMME_FEHLT))
    {
      buttons.addButton("Fehlende Prüfsummen erstellen", co -> {
        int count = 0;
        for (Entry<Beleg, FehlerArt> entry : fehlerMap.entrySet())
        {
          if (entry.getValue() != FehlerArt.PREUEFSUMME_FEHLT)
          {
            continue;
          }
          try
          {
            // Beim speichern wird automatisch ein fehlender Hash ergänzt
            entry.getKey().store();
            count++;
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler beim Speichern der Prüfsumme", e);
          }
          catch (Exception e)
          {
            throw e;
          }
          GUI.getStatusBar()
              .setSuccessText("Prüfsumme bei " + count + " Belegen ergänzt");
        }
      }, null, true, "ok.png");
    }
    buttons.addButton("Schließen", co -> close(), null, true, "ok.png");

    c.addButtonArea(buttons);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return null;
  }

}
