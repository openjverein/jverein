package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.io.FreiesFormularAusgabe;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
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
  {
    try
    {
      saveDruckMailSettings();
      new FreiesFormularAusgabe(getMitglieder(currentObject), this);
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(ae.getMessage());
    }
    catch (Exception e)
    {
      Logger.error("", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  private ArrayList<Mitglied> getMitglieder(Object object)
      throws RemoteException, ApplicationException
  {
    Mitgliedstyp mitgliedstyp = (Mitgliedstyp) getSuchMitgliedstyp(
        Mitgliedstypen.ALLE).getValue();
    int type = -1;
    if (mitgliedstyp != null)
    {
      type = Integer.parseInt(mitgliedstyp.getID());
    }
    ArrayList<Mitglied> mitglieder = new MitgliedQuery(this).get(type, null);
    if (mitglieder.size() == 0)
    {
      throw new ApplicationException(
          "Für die gewählten Filterkriterien wurden keine Mitglieder gefunden.");
    }
    return mitglieder;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object object, String option)
      throws RemoteException, ApplicationException
  {
    ArrayList<Mitglied> mitglieder = getMitglieder(object);
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = null;
    int ohneMail = 0;

    for (Mitglied m : mitglieder)
    {
      String mail = m.getEmail();
      if ((mail == null || mail.isEmpty())
          && getAusgabeart().getValue() == Ausgabeart.MAIL)
      {
        ohneMail++;
      }
      liste.add(new DruckMailEmpfaengerEntry("Freies Formular", mail,
          m.getName(), m.getVorname(), m.getMitgliedstyp()));
    }

    if (ohneMail == 1)
    {
      text = ohneMail + " Mitglied hat keine Mail Adresse.";
    }
    else if (ohneMail > 1)
    {
      text = ohneMail + " Mitglieder haben keine Mail Adresse.";
    }
    return new DruckMailEmpfaenger(liste, text);
  }
}
