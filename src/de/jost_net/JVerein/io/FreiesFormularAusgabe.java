package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.gui.control.FreieFormulareControl;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class FreiesFormularAusgabe
{
  FreieFormulareControl control;

  File file = null;

  FormularAufbereitung formularaufbereitung = null;

  ZipOutputStream zos = null;

  public FreiesFormularAusgabe(FreieFormulareControl control)
      throws IOException, ApplicationException
  {
    this.control = control;
    Formular formular = (Formular) control
        .getFormular(FormularArt.FREIESFORMULAR).getValue();
    if (formular == null)
    {
      GUI.getStatusBar().setErrorText("Kein Formular ausgewählt.");
      return;
    }

    Mitgliedstyp mitgliedstyp = (Mitgliedstyp) control
        .getSuchMitgliedstyp(Mitgliedstypen.ALLE).getValue();
    int type = -1;
    if (mitgliedstyp != null)
      type = Integer.parseInt(mitgliedstyp.getID());
    ArrayList<Mitglied> mitglieder = new MitgliedQuery(control).get(type, null);

    if (mitglieder.size() == 0)
    {
      GUI.getStatusBar().setErrorText("Keine passenden Mitglieder gefunden.");
      return;
    }

    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        file = getDateiAuswahl("pdf", formular.getBezeichnung(), mitglieder);
        if (file == null)
        {
          return;
        }
        formularaufbereitung = new FormularAufbereitung(file, false, false);
        break;
      case MAIL:
        file = getDateiAuswahl("zip", formular.getBezeichnung(), mitglieder);
        if (file == null)
        {
          return;
        }
        zos = new ZipOutputStream(new FileOutputStream(file));
        break;
    }

    aufbereitung(formular, mitglieder);
  }

  public void aufbereitung(Formular formular, ArrayList<Mitglied> mitglieder)
      throws IOException, ApplicationException
  {
    for (Mitglied m : mitglieder)
    {
      switch ((Ausgabeart) control.getAusgabeart().getValue())
      {
        case DRUCK:
          aufbereitenFormular(m, formularaufbereitung, formular);
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
        formularaufbereitung.showFormular();
        break;
      case MAIL:
        zos.close();
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue());
        break;
    }

  }

  File getDateiAuswahl(String extension, String name,
      ArrayList<Mitglied> mitglieder) throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = control.getSettings().getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    if (mitglieder.size() == 1)
    {
      fd.setFileName(
          VorlageUtil.getName(VorlageTyp.FREIES_FORMULAR_MITGLIED_DATEINAME,
              name, mitglieder.get(0)) + "." + extension);
    }
    else
    {
      fd.setFileName(
          VorlageUtil.getName(VorlageTyp.FREIES_FORMULAR_DATEINAME, name) + "."
              + extension);
    }
    fd.setFilterExtensions(new String[] { "*." + extension });
    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return null;
    }
    if (!s.toLowerCase().endsWith("." + extension))
    {
      s = s + "." + extension;
    }
    final File file = new File(s);
    control.getSettings().setAttribute("lastdir", file.getParent());
    return file;
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
