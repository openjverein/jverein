package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.view.FreieFormulareView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;

public class FreieFormulareAction implements Action {

	@Override
	  public void handleAction(Object context)
	  {
	      GUI.startView(FreieFormulareView.class.getName(), null);
	  }
}
