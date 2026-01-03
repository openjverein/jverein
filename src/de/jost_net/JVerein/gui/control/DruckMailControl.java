package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.gui.dialogs.DruckMailMitgliedDialog;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class DruckMailControl extends FilterControl
    implements IMailControl
{
  public DruckMailControl(AbstractView view)
  {
    super(view);
  }

  protected TextAreaInput info = null;

  protected FormularInput formular = null;

  protected SelectInput ausgabeart = null;

  protected SelectInput adressblatt = null;

  protected TextInput mailbetreff = null;

  protected TextAreaInput mailtext = null;

  abstract public String getInfoText(Object selection) throws RemoteException;

  public TextAreaInput getInfo() throws RemoteException
  {
    if (info != null)
    {
      return info;
    }
    info = new TextAreaInput(getInfoText(getCurrentObject()), 10000);
    info.setHeight(100);
    info.setEnabled(false);
    return info;
  }

  public FormularInput getFormular(FormularArt formulartyp)
      throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    formular = new FormularInput(formulartyp,
        settings.getString(settingsprefix + "formular.key", ""));
    // Pre-Notifications können auch ohne Formular gemailt werden
    if (formulartyp == FormularArt.SEPA_PRENOTIFICATION)
    {
      formular.setPleaseChoose("Kein Formular");
    }
    return formular;
  }

  public SelectInput getAusgabeart()
  {
    if (ausgabeart != null)
    {
      return ausgabeart;
    }
    ausgabeart = new SelectInput(Ausgabeart.values(), Ausgabeart
        .getByKey(settings.getInt(settingsprefix + "ausgabeart.key", 1)));
    ausgabeart.setName("Ausgabe");
    return ausgabeart;
  }

  public SelectInput getAdressblatt()
  {
    if (adressblatt != null)
    {
      return adressblatt;
    }
    adressblatt = new SelectInput(Adressblatt.values(), Adressblatt
        .getByKey(settings.getInt(settingsprefix + "adressblatt.key", 1)));
    adressblatt.setName("Adressblatt");
    return adressblatt;
  }

  public TextInput getBetreff()
  {
    if (mailbetreff != null)
    {
      return mailbetreff;
    }
    mailbetreff = new TextInput(
        settings.getString(settingsprefix + "mail.betreff", ""), 100);
    mailbetreff.setName("Betreff");
    return mailbetreff;
  }

  @Override
  public String getBetreffString()
  {
    return (String) getBetreff().getValue();
  }

  public TextAreaInput getTxt()
  {
    if (mailtext != null)
    {
      return mailtext;
    }
    mailtext = new TextAreaInput(
        settings.getString(settingsprefix + "mail.text", ""), 10000);
    mailtext.setName("Text");
    return mailtext;
  }

  @Override
  public String getTxtString()
  {
    return (String) getTxt().getValue();
  }

  @Override
  public void saveFilterSettings() throws RemoteException
  {
    if (ausgabeart != null)
    {
      Ausgabeart aa = (Ausgabeart) getAusgabeart().getValue();
      settings.setAttribute(settingsprefix + "ausgabeart.key", aa.getKey());
    }
    if (adressblatt != null)
    {
      Adressblatt ab = (Adressblatt) getAdressblatt().getValue();
      settings.setAttribute(settingsprefix + "adressblatt.key", ab.getKey());
    }
    if (mailbetreff != null)
    {
      settings.setAttribute(settingsprefix + "mail.betreff",
          (String) getBetreff().getValue());
    }
    if (mailtext != null)
    {
      settings.setAttribute(settingsprefix + "mail.text",
          (String) getTxt().getValue());
    }
    if (formular != null)
    {
      Formular f = (Formular) getFormular(null).getValue();
      settings.setAttribute(settingsprefix + "formular.key",
          f == null ? "" : f.getID());
    }
    super.saveFilterSettings();
  }

  abstract DruckMailEmpfaenger getDruckMailMitglieder(Object object,
      String option) throws RemoteException, ApplicationException;

  public Button getDruckMailMitgliederButton(final Object object, String option)
  {
    Button button = new Button("Empfänger Liste", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        List<DruckMailEmpfaengerEntry> liste = null;
        String text = "";
        try
        {
          DruckMailEmpfaenger result = getDruckMailMitglieder(object, option);
          liste = result.getMitgliederListe();
          if (result.getText() != null)
          {
            text = result.getText();
          }
        }
        catch (ApplicationException ae)
        {
          text = ae.getMessage();
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Generierung der Empfängerliste.", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          return;
        }
        try
        {
          DruckMailMitgliedDialog mevd = new DruckMailMitgliedDialog(liste,
              text, DruckMailMitgliedDialog.POSITION_CENTER);
          mevd.open();
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          Logger.error("Fehler bei der Anzeige des Empfänger Liste Dialogs.",
              e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "gtk-info.png");
    return button;
  }

  public class DruckMailEmpfaenger
  {
    private List<DruckMailEmpfaengerEntry> liste;

    private String text;

    public DruckMailEmpfaenger(List<DruckMailEmpfaengerEntry> liste,
        String text)
    {
      this.liste = liste;
      this.text = text;

    }

    public List<DruckMailEmpfaengerEntry> getMitgliederListe()
    {
      return liste;
    }

    public String getText()
    {
      return text;
    }
  }

  public class DruckMailEmpfaengerEntry
  {
    private String dokument;

    private String email;

    private String name;

    private String vorname;

    private String mitgliedstyp = "";

    public DruckMailEmpfaengerEntry(String dokument, String email, String name,
        String vorname, Mitgliedstyp mitgliedstyp) throws RemoteException
    {
      this(dokument, email, name, vorname, mitgliedstyp.getBezeichnung());
    }

    public DruckMailEmpfaengerEntry(String dokument, String email, String name,
        String vorname, String mitgliedstyp)
    {
      this.dokument = dokument;
      this.email = email;
      this.name = name;
      this.vorname = vorname;
      this.mitgliedstyp = mitgliedstyp;
    }

    public String getDokument()
    {
      return dokument;
    }

    public String getEmail()
    {
      return email;
    }

    public String getName()
    {
      return name;
    }

    public String getVorname()
    {
      return vorname;
    }

    public String getMitgliedstyp()
    {
      return mitgliedstyp;
    }

    public Object getAttribute(String fieldName) throws RemoteException
    {
      switch (fieldName)
      {
        case "dokument":
          return dokument;
        case "email":
          return email;
        case "name":
          return name;
        case "vorname":
          return vorname;
        case "mitgliedstyp":
          return mitgliedstyp;
      }
      return null;
    }
  }
}
