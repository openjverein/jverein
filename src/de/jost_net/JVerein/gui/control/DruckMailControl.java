package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.Ausgabesortierung;
import de.jost_net.JVerein.keys.FormularArt;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;

public class DruckMailControl extends FilterControl
{
  public DruckMailControl(AbstractView view)
  {
    super(view);
  }
  
  public static final String EMAIL = "Mail";

  public static final String PDF1 = "PDF (Lastschriften ohne Mail Empfänger)";

  public static final String PDF2 = "PDF (Alle)";

  public static final String NICHT_EINZELN = "Eine PDF-Datei";

  public static final String EINZELN_NUMMERIERT = "Einzelne PDF-Dateien, nummeriert";

  public static final String EINZELN_MITGLIEDSNUMMER = "Einzelne PDF-Dateien, mit Mitgliedsnummer";

  public static final String EINZELN_NUMMERIERT_UND_MNR = "Einzelne PDF-Dateien, nummeriert mit Mitgliedsnummer";
  
  protected TextAreaInput info = null;

  protected FormularInput formular = null;

  protected SelectInput ausgabeart = null;
  
  protected SelectInput ausgabesortierung = null;

  protected SelectInput adressblatt = null;
  
  protected SelectInput output = null;

  protected SelectInput pdfModus = null;
  
  protected TextInput mailbetreff = null;

  protected TextAreaInput mailtext = null;
  
  
  public String getInfoText(Object selection)
  {
    return "";
  }
  
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
    formular = new FormularInput(formulartyp);
    return formular;
  }
  
  public SelectInput getAusgabeart()
  {
    if (ausgabeart != null)
    {
      return ausgabeart;
    }
    ausgabeart = new SelectInput(Ausgabeart.values(),
        Ausgabeart.getByKey(settings.getInt(settingsprefix + "ausgabeart.key", 1)));
    ausgabeart.setName("Ausgabe");
    return ausgabeart;
  }
  
  public SelectInput getAusgabesortierung()
  {
    if (ausgabesortierung != null)
    {
      return ausgabesortierung;
    }
    ausgabesortierung = new SelectInput(Ausgabesortierung.values(),
        Ausgabesortierung.getByKey(settings.getInt(settingsprefix + "ausgabesortierung", 1)));
    ausgabesortierung.setName("Sortierung");
    return ausgabesortierung;
  }
  
  public SelectInput getAdressblatt()
  {
    if (adressblatt != null)
    {
      return adressblatt;
    }
    adressblatt = new SelectInput( Adressblatt.values(),
        Adressblatt.getByKey(settings.getInt(settingsprefix + "adressblatt.key", 1)));
    adressblatt.setName("Adressblatt");
    return adressblatt;
  }
  
  public SelectInput getOutput()
  {
    if (output != null)
    {
      return output;
    }
    Object[] values = new Object[] { EMAIL, PDF1, PDF2 };
    String out = settings.getString(settingsprefix + "output", PDF1);
    if (out.equals("EMail"))
      out = "Mail";
    output = new SelectInput(values, out);
    output.setName("Ausgabe");
    return output;
  }
  
  public SelectInput getPdfModus()
  {
    if (pdfModus != null)
    {
      return pdfModus;
    }
    Object[] values = new Object[] { NICHT_EINZELN, EINZELN_NUMMERIERT,
        EINZELN_MITGLIEDSNUMMER, EINZELN_NUMMERIERT_UND_MNR };
    pdfModus = new SelectInput(values,
        settings.getString(settingsprefix +"pdfModus", NICHT_EINZELN));
    pdfModus.setName("PDF als");
    return pdfModus;
  }
  
  public TextInput getBetreff()
  {
    if (mailbetreff != null)
    {
      return mailbetreff;
    }
    mailbetreff = new TextInput(settings.
        getString(settingsprefix + "mail.betreff", ""), 100);
    mailbetreff.setName("Betreff");
    return mailbetreff;
  }

  public TextAreaInput getTxt()
  {
    if (mailtext != null)
    {
      return mailtext;
    }
    mailtext = new TextAreaInput(settings.
        getString(settingsprefix + "mail.text", ""), 10000);
    mailtext.setName("Text");
    return mailtext;
  }
  
  protected void saveDruckMailSettings() throws RemoteException
  {
    if (ausgabeart != null )
    {
      Ausgabeart aa = (Ausgabeart) getAusgabeart().getValue();
      settings.setAttribute(settingsprefix + "ausgabeart.key", aa.getKey());
    }
    if (adressblatt != null)
    {
      Adressblatt ab = (Adressblatt) getAdressblatt().getValue();
      settings.setAttribute(settingsprefix + "adressblatt.key", ab.getKey());
    }
    if (ausgabesortierung != null)
    {
      Ausgabesortierung as = (Ausgabesortierung) getAusgabesortierung()
          .getValue();
      settings.setAttribute(settingsprefix + "ausgabesortierung", as.getKey());
    }
    if (output != null)
    {
      String val = (String) getOutput().getValue();
      settings.setAttribute(settingsprefix + "output", val);
    }
    if (pdfModus != null)
    {
      String pdfMode = (String) getPdfModus().getValue();
      settings.setAttribute(settingsprefix + "pdfModus", pdfMode);
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
    saveFilterSettings();
  }
  
}
