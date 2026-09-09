package de.jost_net.JVerein.gui.navigation;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.action.AboutAction;
import de.jost_net.JVerein.gui.action.AbrechnungAction;
import de.jost_net.JVerein.gui.action.LizenzAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.MenuItem;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.logging.Logger;

public class JVereinMenue implements Extension
{

  @Override
  public void extend(Extendable extendable)
  {
    MenuItem jverein = (MenuItem) extendable;
    try
    {
      jverein.addChild(new JVereinMenueItem(jverein, "Über", new AboutAction(),
          "gtk-info.png"));
      jverein.addChild(new JVereinMenueItem(jverein, "Lizenzinformationen",
          new LizenzAction(), "text-x-generic.png"));

      jverein.addChild(new JVereinMenueItem(jverein, "-", null, null));

      jverein.addChild(new JVereinMenueItem(jverein, "Neues Mitglied",
          new MitgliedDetailAction(), "user-friends.png", "ALT+M"));
      jverein.addChild(new JVereinMenueItem(jverein, "Neue Buchung",
          new NewAction(BuchungDetailView.class, Buchung.class),
          "emblem-documents.png", "ALT+B"));
      jverein.addChild(new JVereinMenueItem(jverein, "Neuer Abrechnungslauf",
          new AbrechnungAction(), "calculator.png", "ALT+A"));
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Aufbau des JVerein-Menüs", e);
    }
  }

}
