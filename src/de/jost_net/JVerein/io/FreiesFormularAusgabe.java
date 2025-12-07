package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.control.DruckMailControl;
import de.jost_net.JVerein.gui.control.FreieFormulareControl;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class FreiesFormularAusgabe extends AbstractAusgabe
{
  FreieFormulareControl control;

  public FreiesFormularAusgabe(ArrayList<Mitglied> mitglieder,
      FreieFormulareControl control, String pdfMode)
      throws IOException, ApplicationException
  {
    this.control = control;
    boolean einzelnePdfs = pdfMode.equals(DruckMailControl.EINZELN)
        && mitglieder.size() > 1;

    Formular formular = (Formular) control
        .getFormular(FormularArt.FREIESFORMULAR).getValue();
    if (formular == null)
    {
      GUI.getStatusBar().setErrorText("Kein Formular ausgewählt.");
      return;
    }

    Ausgabeart art = (Ausgabeart) control.getAusgabeart().getValue();
    String extension = getExtension(art);
    String dateiname = null;
    if (mitglieder.size() == 1)
    {
      dateiname = VorlageUtil.getName(
          VorlageTyp.FREIES_FORMULAR_MITGLIED_DATEINAME,
          formular.getBezeichnung(), mitglieder.get(0)) + "." + extension;
    }
    else
    {
      dateiname = VorlageUtil.getName(VorlageTyp.FREIES_FORMULAR_DATEINAME,
          formular.getBezeichnung()) + "." + extension;
    }
    file = getDateiAuswahl(extension, dateiname, einzelnePdfs, control,
        control.getSettings());
    if (file == null)
    {
      return;
    }

    init(art, einzelnePdfs, false);
    aufbereitung(formular, mitglieder, einzelnePdfs);
  }

  public void aufbereitung(Formular formular, ArrayList<Mitglied> mitglieder,
      boolean einzelnePdfs) throws IOException, ApplicationException
  {
    for (Mitglied m : mitglieder)
    {
      switch ((Ausgabeart) control.getAusgabeart().getValue())
      {
        case DRUCK:
          if (einzelnePdfs)
          {
            final File fx = new File(file.getParent() + File.separator
                + VorlageUtil.getName(
                    VorlageTyp.FREIES_FORMULAR_MITGLIED_DATEINAME,
                    formular.getBezeichnung(), m)
                + ".pdf");
            formularaufbereitung = new FormularAufbereitung(fx, true, false);
          }
          aufbereitenFormular(m, formularaufbereitung, formular);
          if (einzelnePdfs)
          {
            formularaufbereitung.closeFormular();
          }
          break;
        case MAIL:
          if (m.getEmail() == null || m.getEmail().isEmpty())
          {
            continue;
          }
          File f = File.createTempFile(getDateiname(m, formular), ".pdf");
          formularaufbereitung = new FormularAufbereitung(f, false, false);
          aufbereitenFormular(m, formularaufbereitung, formular);
          formularaufbereitung.closeFormular();
          zos.putNextEntry(new ZipEntry(getDateiname(m, formular) + ".pdf"));
          FileInputStream in = new FileInputStream(f);
          // buffer size
          byte[] b = new byte[1024];
          int count;
          while ((count = in.read(b)) > 0)
          {
            zos.write(b, 0, count);
          }
          in.close();
          break;
      }
    }
    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        if (!einzelnePdfs)
        {
          formularaufbereitung.showFormular();
        }
        else
        {
          formularaufbereitung.closeFormular();
          GUI.getStatusBar()
              .setSuccessText("Die freien Formulare wurden erstellt und unter: "
                  + file.getParent() + " gespeichert.");
        }
        break;
      case MAIL:
        zos.close();
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue());
        break;
    }

  }

  void aufbereitenFormular(Mitglied m, FormularAufbereitung fa, Formular fo)
      throws RemoteException, ApplicationException
  {
    Map<String, Object> map = new MitgliedMap().getMap(m, null);
    map = new AllgemeineMap().getMap(map);
    fa.writeForm(fo, map);
    fo.store();
  }

  String getDateiname(Mitglied m, Formular formular) throws RemoteException
  {
    // MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf
    String filename = m.getID() + "#freiesformular# #";
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename + "#" + formular.getBezeichnung();
  }

}
