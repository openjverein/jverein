package de.jost_net.JVerein.io;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SEPASupport
{

  public Konto getKonto() throws RemoteException, ApplicationException
  {
    if (Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID) == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gesetzt. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    Konto k = Einstellungen.getDBService().createObject(Konto.class,
        Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID).toString());
    if (k == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    return k;
  }

  public void storeBuchungsDokument(Rechnung re, Buchung buchung, Date datum)
      throws ApplicationException
  {
    if (re != null && buchung != null)
    {
      try
      {
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap(re.getMitglied(), map);
        map = new RechnungMap().getMap(re, map);

        // PDF erstellen
        String dateiname = VorlageUtil
            .getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME, re);
        File file = File.createTempFile(dateiname, ".pdf");
        FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
            false);
        aufbereitung.writeForm(re.getFormular(), map);
        aufbereitung.closeFormular();

        AbstractDokument doc = Einstellungen.getDBService()
            .createObject(BuchungDokument.class, null);
        doc.setReferenz(Long.valueOf(buchung.getID()));
        doc.setBemerkung(file.getName());
        doc.setDatum(new Date());
        doc.setFile(file);
        doc.store();
        file.delete();
      }
      catch (IOException | DocumentException e)
      {
        Logger.error("Fehler beim Speichern der Rechnung als Buchungsdokument",
            e);
      }
    }
  }
}
