package de.jost_net.JVerein.gui.control;

import java.io.IOException;
import java.rmi.RemoteException;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.io.FreiesFormularAusgabe;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;

public class FreieFormulareControl extends FilterControl
{

  private FormularInput formular = null;

  private TextInput betreff = null;

  private TextAreaInput txt = null;

  private SelectInput ausgabeart = null;

  public FreieFormulareControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public FormularInput getFormular(FormularArt frei) throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    formular = new FormularInput(frei);
    return formular;
  }

  public Input getAusgabeart()
  {
    if (ausgabeart != null)
    {
      return ausgabeart;
    }
    ausgabeart = new SelectInput(Ausgabeart.values(), Ausgabeart
        .valueOf(settings.getString(settingsprefix + "ausgabeart", "DRUCK")));
    ausgabeart.setName("Ausgabe");
    return ausgabeart;
  }

  public TextInput getBetreff()
  {
    if (betreff != null)
    {
      return betreff;
    }
    betreff = new TextInput(
        settings.getString(settingsprefix + "mail.betreff", ""), 100);
    betreff.setName("Betreff");
    return betreff;
  }

  public TextAreaInput getTxt()
  {
    if (txt != null)
    {
      return txt;
    }
    txt = new TextAreaInput(
        settings.getString(settingsprefix + "mail.text", ""), 10000);
    txt.setName("Text");
    return txt;
  }
  
  private void saveSettings() throws RemoteException
  {
    if (ausgabeart != null )
    {
      Ausgabeart aa = (Ausgabeart) getAusgabeart().getValue();
      settings.setAttribute(settingsprefix + "ausgabeart", aa.toString());
    }
    if (betreff != null)
    {
      settings.setAttribute(settingsprefix + "mail.betreff",
          (String) getBetreff().getValue());
    }
    if (txt != null)
    {
      settings.setAttribute(settingsprefix + "mail.text",
          (String) getTxt().getValue());
    }
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

  private void generiereFreieFormulare(Object currentObject) throws IOException
  {
    saveFilterSettings();
    saveSettings();
    new FreiesFormularAusgabe(this);
  }
}
