package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.jost_net.JVerein.gui.control.MitgliedControl;

public class AuswertungJubilareFilterMap extends AbstractMap
{
  public Map<String, Object> getMap(MitgliedControl control,
      Map<String, Object> inMap) throws RemoteException
  {
    return populate(ensureMap(inMap), var -> switch (var)
    {
      case FILTER_JAHR -> control.getJubeljahr().getText();
    });
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
  {
    return populate(ensureMap(inMap), var -> switch (var)
    {
      case FILTER_JAHR -> "2024";
    });
  }

  private static Map<String, Object> ensureMap(Map<String, Object> inMap)
  {
    return (inMap != null) ? inMap : new HashMap<>();
  }

  private static Map<String, Object> populate(Map<String, Object> map,
      Function<AuswertungJubilareFilterVar, Object> resolver)
  {
    for (var v : AuswertungJubilareFilterVar.values())
    {
      map.put(v.getName(), resolver.apply(v));
    }
    return map;
  }
}
