/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/

package de.jost_net.JVerein.gui.util;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.MitgliedVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;

public class EvalMail
{

  VelocityContext context;

  Map<String, Object> map;

  public EvalMail(MailEmpfaenger empfaenger) throws RemoteException
  {
    this(empfaenger.getMitglied());
  }

  public EvalMail(Mitglied mitglied) throws RemoteException
  {
    context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    if (mitglied != null)
    {
      context.put("email", mitglied.getEmail());
      context.put("empf", mitglied);
    }
    this.map = new MitgliedMap().getMap(mitglied,
        new AllgemeineMap().getMap(null));
    VarTools.add(context, map);
  }

  public EvalMail(Map<String, Object> map) throws RemoteException
  {
    context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    VarTools.add(context, map);
    this.map = map;
  }

  public String evalBetreff(String betr) throws ParseErrorException,
      MethodInvocationException, ResourceNotFoundException
  {
    if (context == null)
      return null;
    StringWriter wbetr = new StringWriter();
    Velocity.evaluate(context, wbetr, "LOG", betr);
    return wbetr.getBuffer().toString();
  }

  public String evalText(String txt) throws ParseErrorException,
      MethodInvocationException, ResourceNotFoundException
  {
    if (context == null)
      return null;
    StringWriter wtext = new StringWriter();
    Velocity.evaluate(context, wtext, "LOG", txt);
    return wtext.getBuffer().toString();
  }

  public String getMitgliedVar(MitgliedVar var)
  {
    return (String) map.get(var.getName());
  }
}
