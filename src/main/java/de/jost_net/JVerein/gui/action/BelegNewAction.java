package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.view.BelegDetailView;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class BelegNewAction extends NewAction
{

  private AbstractBelegDBObject belegObject;

  public BelegNewAction()
  {
    super(BelegDetailView.class, Beleg.class);
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof AbstractBelegDBObject))
    {
      throw new ApplicationException("Falsches Kontextobjekt");
    }
    this.belegObject = (AbstractBelegDBObject) context;
    super.handleAction(context);
  }

  @Override
  protected void startView(Class<? extends AbstractView> viewClass,
      DBObject context)
  {
    GUI.startView(new BelegDetailView(belegObject), context);
  }
}
