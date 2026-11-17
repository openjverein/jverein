package de.jost_net.JVerein.io;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.BuchungMap;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.Beleggruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungReportBeleg
{
  private Formular formular;

  private Buchung buchung;

  private Date datum;

  private Beleggruppe beleggruppe;

  public BuchungReportBeleg(Formular formular, Buchung buchung, Date datum,
      Beleggruppe beleggruppe)
  {
    this.formular = formular;
    this.buchung = buchung;
    this.datum = datum;
    this.beleggruppe = beleggruppe;
  }

  public void store() throws Exception
  {
    if (buchung != null)
    {
      File file = null;
      try
      {
        Beleg doc = Einstellungen.getDBService().createObject(Beleg.class,
            null);
        doc.setDatum(datum);
        doc.setBelegArt(BelegArt.BUCHUNG);
        doc.setBelegGruppe(beleggruppe);
        doc.erzeugeBelegnummer();

        // Belegnummer wird in der Formularaufbereitung gebraucht
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new BuchungMap().getMap(buchung, map);
        map.put("beleg_nummer", doc.getBelegnummer());

        // PDF erstellen
        String dateiname = VorlageUtil.getName(
            VorlageTyp.BUCHUNGSREPORT_DATEINAME, buchung,
            formular.getBezeichnung()) + ".pdf";
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        file = new File(tempDir, dateiname);
        FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
            false);
        aufbereitung.writeForm(formular, map);
        // ggf. hochgezählten Zähler speichern
        formular.store();
        aufbereitung.closeFormular();

        doc.setBemerkung(file.getName());
        doc.setFile(file);
        doc.store();

        buchung.addBeleg(doc);
      }
      catch (IOException | DocumentException e)
      {
        String text = "Fehler beim Speichern des Buchungsreports als Buchungsdokument.";
        Logger.error(text, e);
        throw new ApplicationException(text);
      }
      finally
      {
        if (file != null)
        {
          file.delete();
        }
      }
    }
  }
}
