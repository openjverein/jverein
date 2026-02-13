package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.io.FreiesFormularAusgabe;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FreieFormulareControl extends DruckMailControl
{

  public FreieFormulareControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Button getStartFreieFormulareButton(Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveFilterSettings();
          new FreiesFormularAusgabe((Formular) FreieFormulareControl.this
              .getFormular(null).getValue()).aufbereiten(
                  getMitglieder(currentObject),
                  (Ausgabeart) getAusgabeart().getValue(), getBetreffString(),
                  getTxtString(), false, false, false);
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Freie Formulare Ausgabe.", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private ArrayList<Mitglied> getMitglieder(Object object)
      throws RemoteException, ApplicationException
  {
    if (object instanceof Mitglied)
    {
      object = new Mitglied[] { (Mitglied) object };
    }
    if (object instanceof Mitglied[])
    {
      return new ArrayList<Mitglied>(Arrays.asList((Mitglied[]) object));
    }
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

  @Override
  public String getInfoText(Object selection) throws RemoteException
  {
    Mitglied[] mitglieder = null;
    String text = "";

    if (selection instanceof Mitglied)
    {
      mitglieder = new Mitglied[] { (Mitglied) selection };
    }
    else if (selection instanceof Mitglied[])
    {
      mitglieder = (Mitglied[]) selection;
    }
    else
    {
      return "";
    }

    // Aufruf aus Mitglieder View
    if (mitglieder != null)
    {
      text = "Es wurden " + mitglieder.length + " Mitglieder ausgewählt";
      String fehlen = "";
      for (Mitglied m : mitglieder)
      {
        if (m.getEmail() == null || m.getEmail().isEmpty())
        {
          fehlen = fehlen + "\n - " + m.getName() + ", " + m.getVorname();
        }
      }
      if (fehlen.length() > 0)
      {
        text += "\nFolgende Mitglieder haben keine Mailadresse:" + fehlen;
      }
    }
    return text;
  }

  @Override
  protected void TabRefresh()
  {
    // Nichts tun, hier ist keine Tabelle implementiert
  }
}
