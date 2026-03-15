package de.jost_net.JVerein.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailValidator
{
  public static void isValid(String emailAddress) throws AddressException
  {
    if (emailAddress == null)
    {
      throw new AddressException("Mailadresse ist leer");
    }

    InternetAddress internetAddress = new InternetAddress(emailAddress);
    if (internetAddress.isGroup())
    {
      for (InternetAddress member : internetAddress.getGroup(true))
      {
        Check(member);
      }
    }
    else
    {
      Check(internetAddress);
    }
  }

  private static void Check(InternetAddress internetAddress)
      throws AddressException
  {
    internetAddress.validate();
    String[] tokens = internetAddress.getAddress().split("@");
    if (tokens.length != 2)
    {
      throw new AddressException("Missing final '@domain'",
          internetAddress.getAddress());
    }
    if (tokens[1].lastIndexOf('.') == -1)
    {
      throw new AddressException("Missing final '.tld'",
          internetAddress.getAddress());
    }
  }
}
