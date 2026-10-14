/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Messaging.MitgliedskontoMessage;
import de.jost_net.JVerein.gui.menu.MitgliedskontoMenu;
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedskontoControl extends AbstractControl
{

  private TreePart mitgliedskontoTree;

  private MitgliedskontoMessageConsumer mc = null;

  public MitgliedskontoControl(AbstractView view)
  {
    super(view);
  }

  public Part getMitgliedskontoTree(Mitglied mitglied) throws RemoteException
  {
    mitgliedskontoTree = new TreePart(new MitgliedskontoNode(mitglied),
        new Action()
        {

          @Override
          public void handleAction(Object context) throws ApplicationException
          {
            if (context == null || !(context instanceof MitgliedskontoNode))
            {
              return;
            }
            try
            {
              MitgliedskontoNode mkn = (MitgliedskontoNode) context;
              if (mkn.getType() == MitgliedskontoNode.IST)
              {
                Buchung bu = (Buchung) Einstellungen.getDBService()
                    .createObject(Buchung.class, mkn.getID());
                GUI.startView(BuchungDetailView.class.getName(), bu);
              }
              if (mkn.getType() == MitgliedskontoNode.SOLL)
              {
                Sollbuchung sollb = (Sollbuchung) Einstellungen.getDBService()
                    .createObject(Sollbuchung.class, mkn.getID());
                GUI.startView(new SollbuchungDetailView(), sollb);
              }
            }
            catch (RemoteException e)
            {
              Logger.error(e.getMessage());
              throw new ApplicationException(
                  "Fehler beim Editieren der Buchung");
            }
          }
        })

    {

      @SuppressWarnings("unchecked")
      @Override
      public void paint(Composite composite) throws RemoteException
      {
        super.paint(composite);
        List<MitgliedskontoNode> items = mitgliedskontoTree.getItems();
        for (MitgliedskontoNode mkn : items)
        {
          @SuppressWarnings("rawtypes")
          GenericIterator items2 = mkn.getChildren();
          while (items2.hasNext())
          {
            MitgliedskontoNode mkn2 = (MitgliedskontoNode) items2.next();
            mitgliedskontoTree.setExpanded(mkn2, false);
          }
        }
      }

    };
    mitgliedskontoTree.setMulti(true);
    mitgliedskontoTree.addColumn("Name, Vorname", "name");
    mitgliedskontoTree.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    mitgliedskontoTree.addColumn("Zweck1", "zweck1");
    mitgliedskontoTree.addColumn("Zahlungsweg", "zahlungsweg");
    mitgliedskontoTree.addColumn("Soll", "soll",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    mitgliedskontoTree.addColumn("Ist", "ist",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    mitgliedskontoTree.addColumn("Differenz", "differenz",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.LEERESPALTE))
    {
      mitgliedskontoTree.addColumn(new Column("leer", ""));
    }
    mitgliedskontoTree.setContextMenu(new MitgliedskontoMenu());
    mitgliedskontoTree.setFormatter(new MitgliedskontoTreeFormatter());
    mitgliedskontoTree.setRememberColWidths(true);
    this.mc = new MitgliedskontoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    return mitgliedskontoTree;
  }

  private static class MitgliedskontoTreeFormatter implements TreeFormatter
  {
    @Override
    public void format(TreeItem item)
    {
      MitgliedskontoNode mkn = (MitgliedskontoNode) item.getData();
      switch (mkn.getType())
      {
        case MitgliedskontoNode.MITGLIED:
          item.setImage(0, SWTUtil.getImage("user.png"));
          break;
        case MitgliedskontoNode.SOLL:
          item.setImage(0, SWTUtil.getImage("calculator.png"));
          item.setExpanded(false);
          break;
        case MitgliedskontoNode.IST:
          item.setImage(0, SWTUtil.getImage("euro-sign.png"));
          break;
      }
    }
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class MitgliedskontoMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[] { MitgliedskontoMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            if (mitgliedskontoTree != null)
            {
              Mitglied mitglied = (Mitglied) getCurrentObject();
              mitgliedskontoTree
                  .setRootObject(new MitgliedskontoNode(mitglied));
            }
            else
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(
                  MitgliedskontoMessageConsumer.this);
              return;
            }

          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(MitgliedskontoMessageConsumer.this);
          }
        }

      });
    }
  }

  public void deregisterMitgliedskontoConsumer()
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(mc);
  }
}
