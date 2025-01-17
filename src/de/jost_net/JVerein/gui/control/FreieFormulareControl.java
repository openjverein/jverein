package de.jost_net.JVerein.gui.control;

import java.io.IOException;

import de.jost_net.JVerein.io.FreiesFormularAusgabe;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FreieFormulareControl extends DruckMailControl
{

  public FreieFormulareControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Button getStartFreieFormulareButton(Object currentObject,
      FreieFormulareControl control)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          generiereFreieFormulare(context);
        }
        catch (Exception e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void generiereFreieFormulare(Object currentObject)
      throws IOException, ApplicationException
  {
    saveDruckMailSettings();
    new FreiesFormularAusgabe(this);
  }
}
