package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
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

  public void storeBuchungsDokument(Rechnung re, Buchung buchung, Date datum,
      Map<String, Object> map) throws ApplicationException
  {
    if (re != null && buchung != null)
    {
      FileInputStream fis = null;
      try
      {
        // PDF erstellen
        String dateiname = VorlageUtil.getName(
            VorlageTyp.RECHNUNG_MITGLIED_DATEINAME, re, re.getMitglied());
        File file = File.createTempFile(dateiname, ".pdf");
        FormularAufbereitung aufbereitung = new FormularAufbereitung(file,
            false, true);
        aufbereitung.writeForm(re.getFormular(), map);
        aufbereitung.closeFormular();

        fis = new FileInputStream(file);
        if (fis.available() <= 0)
        {
          throw new ApplicationException("Datei ist leer");
        }
        AbstractDokument doc = Einstellungen.getDBService()
            .createObject(BuchungDokument.class, null);
        doc.setReferenz(Long.valueOf(buchung.getID()));

        // Dokument speichern
        String locverz = "buchungen" + doc.getReferenz();
        QueryMessage qm = new QueryMessage(locverz, fis);
        Application.getMessagingFactory()
            .getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);

        // Satz in die DB schreiben
        doc.setBemerkung(
            dateiname.length() > 50 ? dateiname.substring(0, 50) : dateiname);
        String uuid = qm.getData().toString();
        doc.setUUID(uuid);
        doc.setDatum(datum);
        doc.store();

        // Zusätzliche Eigenschaft speichern
        Map<String, String> filenameMap = new HashMap<>();
        filenameMap.put("filename", file.getName());
        qm = new QueryMessage(uuid, filenameMap);
        Application.getMessagingFactory()
            .getMessagingQueue("jameica.messaging.putmeta").sendMessage(qm);
        file.delete();
      }
      catch (IOException e)
      {
        Logger.error("Fehler beim Speichern der Rechnung als Buchungsdokument",
            e);
      }
      finally
      {
        try
        {
          fis.close();
        }
        catch (IOException ignore)
        {
        }
      }
    }
  }
}
