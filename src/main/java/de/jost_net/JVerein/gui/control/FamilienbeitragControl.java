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
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Messaging.FamilienbeitragMessage;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.menu.FamilienbeitragMenu;
import de.jost_net.JVerein.gui.parts.JVereinTreePart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

public class FamilienbeitragControl extends FilterControl
{
  private JVereinTreePart familienbeitragtree;

  private FamilienbeitragMessageConsumer fbc = null;

  public FamilienbeitragControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public JVereinTreePart getTablePart() throws RemoteException
  {
    if (familienbeitragtree != null)
    {
      return familienbeitragtree;
    }
    familienbeitragtree = new JVereinTreePart(
        new FamilienbeitragNode(getMitgliedStatus()),
        new MitgliedDetailAction());
    familienbeitragtree.addColumn("Name", "name");
    familienbeitragtree.addColumn("Zahlungsweg", "zahlungsweg");
    familienbeitragtree.addColumn("IBAN", "iban", new IBANFormatter());
    familienbeitragtree.addColumn("Austritt", "austritt", new DateFormatter());
    familienbeitragtree.setRememberColWidths(true);

    familienbeitragtree.setContextMenu(new FamilienbeitragMenu());
    this.fbc = new FamilienbeitragMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.fbc);
    familienbeitragtree.setFormatter(new TreeFormatter()
    {
      @Override
      public void format(TreeItem item)
      {
        FamilienbeitragNode fbn = (FamilienbeitragNode) item.getData();
        try
        {
          if (fbn.getType() == FamilienbeitragNode.ROOT)
            item.setImage(SWTUtil.getImage("users.png"));
          if (fbn.getType() == FamilienbeitragNode.ZAHLER
              && fbn.getMitglied().getAustritt() == null)
            item.setImage(SWTUtil.getImage("user-friends.png"));
          if (fbn.getType() == FamilienbeitragNode.ZAHLER
              && fbn.getMitglied().getAustritt() != null)
            item.setImage(SWTUtil.getImage("eraser.png"));
          if (fbn.getType() == FamilienbeitragNode.ANGEHOERIGER
              && fbn.getMitglied().getAustritt() == null)
            item.setImage(SWTUtil.getImage("user.png"));
          if (fbn.getType() == FamilienbeitragNode.ANGEHOERIGER
              && fbn.getMitglied().getAustritt() != null)
            item.setImage(SWTUtil.getImage("eraser.png"));
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim TreeFormatter", e);
        }
      }
    });
    VorZurueckControl.setObjektListe(null, null);
    return familienbeitragtree;
  }

  @Override
  protected void TabRefresh()
  {
    if (familienbeitragtree != null)
    {
      try
      {
        familienbeitragtree.removeAll();
        familienbeitragtree
            .setRootObject(new FamilienbeitragNode(getMitgliedStatus()));
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
  private class FamilienbeitragMessageConsumer implements MessageConsumer
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
      return new Class[] { FamilienbeitragMessage.class };
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
            if (familienbeitragtree == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(
                  FamilienbeitragMessageConsumer.this);
              return;
            }
            familienbeitragtree
                .setRootObject(new FamilienbeitragNode(getMitgliedStatus()));
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(FamilienbeitragMessageConsumer.this);
          }
        }
      });
    }
  }

  public void deregisterFamilienbeitragConsumer()
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(fbc);
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.FAMILIENVERBAND_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.FAMILIENVERBAND_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.FAMILIENVERBAND_DATEINAME);
  }

}
