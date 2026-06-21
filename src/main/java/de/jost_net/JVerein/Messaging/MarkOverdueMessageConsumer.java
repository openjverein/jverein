/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.jost_net.JVerein.Messaging;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.server.RechnungImpl;
import de.jost_net.JVerein.server.SollbuchungImpl;
import de.jost_net.JVerein.server.UnreadCounter;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Wird benachrichtigt, wenn ein Objekt gespeichert wurde und aktualisiert die
 * Anzahl der faelligen Auftraege.
 */
public class MarkOverdueMessageConsumer implements MessageConsumer
{
  private final static ScheduledExecutorService worker = Executors
      .newSingleThreadScheduledExecutor();

  private final static Map<String, AtomicLong> counters = new HashMap<String, AtomicLong>();

  private static Class<UnreadCounter>[] types = null;

  @Override
  public Class<?>[] getExpectedMessageTypes()
  {
    return new Class<?>[] { QueryMessage.class };
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (Application.inServerMode())
      return;

    QueryMessage msg = (QueryMessage) message;

    if (msg.getData() == null)
    {
      updateAll();
      return;
    }

    // Beim speichern/löschen von Buchungen müssen die Counter von Rechnung und
    // Sollbuchung aktuallisiert werden, daher schicken wir die manuell
    if (msg.getData() instanceof Buchung)
    {
      handleMessage(new QueryMessage(new RechnungImpl()));
      handleMessage(new QueryMessage(new SollbuchungImpl()));
    }
    if (!UnreadCounter.class.isAssignableFrom(msg.getData().getClass()))
    {
      return;
    }
    Class<?> type = msg.getData().getClass();
    final String typeName = type.getSimpleName();
    final long currentValue = this.getCounter(type.getSimpleName())
        .incrementAndGet();

    worker.schedule(new Runnable()
    {

      @Override
      public void run()
      {
        // Zwischenzeitlich kam noch ein Aufruf rein. Dann soll der sich drum
        // kuemmern. Das dient dazu, schnell aufeinander folgende Requests zu
        // buendeln, damit z.Bsp. beim Import von 100 Objecten nicht fuer jedes
        // Object 10 x pro Sekunde der Zaehler in der Navi angepasst werden
        // muss.
        if (getCounter(typeName).get() != currentValue)
        {
          Logger.debug(
              "ignoring frequent UnreadCounter counter updates for " + type);
          return;
        }

        update(type);
      }
    }, 300, TimeUnit.MILLISECONDS);
  }

  /**
   * Initialisiert den Message-Consumer.
   */
  @SuppressWarnings("unchecked")
  @PostConstruct
  private void init()
  {
    if (types == null)
    {
      // Klassen wurden noch nicht geladen. Das tun wir jetzt
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      try
      {
        types = finder.findImplementors(UnreadCounter.class);
        if (types == null || types.length == 0)
          throw new ClassNotFoundException();
      }
      catch (ClassNotFoundException ce)
      {
        Logger.warn("no UnreadCounter found");
        types = new Class[0];
      }
    }

    // Wir erstellen noch einen Worker, der einmal pro Stunde die Counter
    // aktualisiert - unabhängig davon, ob etwas geändert wurde. Dadurch werden
    // die Counter auch dann korrekt aktualisiert, wenn man das Programm über
    // Nacht durchlaufen lässt

    if (Application.inServerMode())
      return;

    Logger.info("init mark-overdue message consumer");
    worker.scheduleAtFixedRate(() -> updateAll(), 1, 1, TimeUnit.HOURS);

    // Inital einmal ausführen
    updateAll();
  }

  /**
   * Liefert den aktuellen Zaehlerstand fuer den Objekttyp.
   * 
   * @param key
   *          der Objekttyp.
   * @return der Zaehlerstand.
   */
  private AtomicLong getCounter(String key)
  {
    AtomicLong result = counters.get(key);
    if (result == null)
    {
      result = new AtomicLong(0);
      counters.put(key, result);
    }
    return result;
  }

  /**
   * Aktualisiert einmalig alle Uberfaellig-Counter.
   */
  void updateAll()
  {
    Logger.info("update all unread counters");
    for (Class<UnreadCounter> d : types)
    {
      update(d);
    }
  }

  /**
   * Aktualisiert den Zaehler fuer den angegebenen Typ.
   * 
   * @param type
   *          der Typ.
   * @param id
   *          die ID des Navi-Elements.
   */
  private void update(final Class<?> type)
  {
    try
    {
      Logger
          .debug("updating UnreadCounter counter for " + type.getSimpleName());

      UnreadCounter unreadCounter = (UnreadCounter) type.getConstructor()
          .newInstance();

      GUI.getDisplay().asyncExec(() -> {
        try
        {
          GUI.getNavigation().setUnreadCount(unreadCounter.getMenueID(),
              unreadCounter.getUeberfaellig());
        }
        catch (RemoteException e)
        {
          Logger.error("unable to update number of overdue elements", e);
        }
      });

    }
    catch (Exception e)
    {
      Logger.error("unable to update number of overdue elements", e);
    }
  }

  @Override
  public boolean autoRegister()
  {
    return false;
  }
}
