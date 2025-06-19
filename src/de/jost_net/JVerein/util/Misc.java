package de.jost_net.JVerein.util;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;

public class Misc {
	
	public static String getBuchungsZweckKorrektur(String value, boolean withRealLineBreak) {
	  if (value == null) {
	    return null;
	  }
	  try
	  {
	    Transfer t = new Verwendungszweck(value);
	    String s = StringUtils.trimToNull(VerwendungszweckUtil.getTag(t, Tag.SVWZ));
	    if (!withRealLineBreak)
	    {
	      s = s.replaceAll("\r\n", "|");
        s = s.replaceAll("\r", "|");
        s = s.replaceAll("\n", "|");
	    }
	    return s;
	  }
	  catch (RemoteException ex)
	  {
	    return null;
	  }
	}
}

// Dummy Klasse um die Methode "VerwendungszweckUtil.getTag(t, Tag.SVWZ)"
// verwenden zu können
final class Verwendungszweck implements Transfer
{
  String zweck = null;

  public Verwendungszweck(String zweck)
  {
    this.zweck = zweck;
  }

  @Override
  public String getGegenkontoNummer() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getGegenkontoBLZ() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getGegenkontoName() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getBetrag() throws RemoteException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getZweck() throws RemoteException
  {
    // TODO Auto-generated method stub
    return zweck;
  }

  @Override
  public String getZweck2() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

}

