/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.Date;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Messaging.AbweichenderZahlerMessage;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.menu.AbweichenderZahlerMenu;
import de.jost_net.JVerein.gui.parts.JVereinTreePart;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MitgliedStatus;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

public class AbweichenderZahlerControl extends FilterControl
{
  private JVereinTreePart abweichenderzahlertree;

  private AbweichenderZahlerMessageConsumer azc = null;

  public AbweichenderZahlerControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public JVereinTreePart getTablePart() throws RemoteException
  {
    if (abweichenderzahlertree != null)
    {
      return abweichenderzahlertree;
    }
    abweichenderzahlertree = new JVereinTreePart(
        new AbweichenderZahlerNode(
            (MitgliedStatus) getFilter().get(Filter.MITGLIEDSCHAFT_STATUS)),
        new MitgliedDetailAction());
    abweichenderzahlertree.addColumn("Name", "name");
    abweichenderzahlertree.addColumn("Zahlungsweg", "zahlungsweg");
    abweichenderzahlertree.addColumn("IBAN", "iban", new IBANFormatter());
    abweichenderzahlertree.setRememberColWidths(true);

    abweichenderzahlertree.setContextMenu(new AbweichenderZahlerMenu());
    this.azc = new AbweichenderZahlerMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.azc);
    abweichenderzahlertree.setFormatter(new TreeFormatter()
    {
      @Override
      public void format(TreeItem item)
      {
        AbweichenderZahlerNode azn = (AbweichenderZahlerNode) item.getData();
        try
        {
          if (azn.getType() == AbweichenderZahlerNode.ROOT)
            item.setImage(SWTUtil.getImage("users.png"));
          if (azn.getType() == AbweichenderZahlerNode.ZAHLER
              && (azn.getMitglied().getAustritt() == null
                  || azn.getMitglied().getAustritt().after(new Date())))
            item.setImage(SWTUtil.getImage("user-friends.png"));
          if (azn.getType() == AbweichenderZahlerNode.ZAHLER
              && azn.getMitglied().getAustritt() != null
              && azn.getMitglied().getAustritt().before(new Date()))
            item.setImage(SWTUtil.getImage("eraser.png"));
          if (azn.getType() == AbweichenderZahlerNode.ANGEHOERIGER
              && (azn.getMitglied().getAustritt() == null
                  || azn.getMitglied().getAustritt().after(new Date())))
            item.setImage(SWTUtil.getImage("user.png"));
          if (azn.getType() == AbweichenderZahlerNode.ANGEHOERIGER
              && azn.getMitglied().getAustritt() != null
              && azn.getMitglied().getAustritt().before(new Date()))
            item.setImage(SWTUtil.getImage("eraser.png"));
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim TreeFormatter", e);
        }
      }
    });
    VorZurueckControl.setObjektListe(null, null);
    return abweichenderzahlertree;
  }

  @Override
  protected void TabRefresh()
  {
    if (abweichenderzahlertree != null)
    {
      try
      {
        abweichenderzahlertree.removeAll();
        abweichenderzahlertree.setRootObject(new AbweichenderZahlerNode(
            (MitgliedStatus) getFilter().get(Filter.MITGLIEDSCHAFT_STATUS)));
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    }
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class AbweichenderZahlerMessageConsumer implements MessageConsumer
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
      return new Class[] { AbweichenderZahlerMessage.class };
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
            if (abweichenderzahlertree == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(
                  AbweichenderZahlerMessageConsumer.this);
              return;
            }
            abweichenderzahlertree.setRootObject(new AbweichenderZahlerNode(
                (MitgliedStatus) getFilter().get(Filter.MITGLIEDSCHAFT_STATUS)));
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo", e);
            Application.getMessagingFactory().unRegisterMessageConsumer(
                AbweichenderZahlerMessageConsumer.this);
          }
        }
      });
    }
  }

  public void deregisterAlternativerZahlerConsumer()
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(azc);
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ABWEICHENDE_ZAHLER_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ABWEICHENDE_ZAHLER_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ABWEICHENDE_ZAHLER_DATEINAME);
  }

}
