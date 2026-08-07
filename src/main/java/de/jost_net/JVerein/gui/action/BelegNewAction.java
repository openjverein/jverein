package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.view.BelegDetailView;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.IBeleg;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class BelegNewAction extends NewAction
{

  private IBeleg belegObject;

  public BelegNewAction()
  {
    super(BelegDetailView.class, BuchungDokument.class);
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof IBeleg))
    {
      throw new ApplicationException("Falsches Kontextobjekt");
    }
    this.belegObject = (IBeleg) context;
    super.handleAction(context);
  }

  @Override
  protected void startView(Class<? extends AbstractView> viewClass,
      DBObject context)
  {
    GUI.startView(new BelegDetailView(belegObject), context);
  }
}
