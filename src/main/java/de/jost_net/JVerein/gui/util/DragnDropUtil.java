package de.jost_net.JVerein.gui.util;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;

public class DragnDropUtil
{
  public interface SaveAction
  {
    public void save(String filename);
  }

  /**
   * DEm composit wird drag'n'drop Funktionalität hinzuzugefügt
   * 
   * @param composit
   * @param action
   *          Aktion die für jede Datei ausgeführt werden soll
   */
  public static void setDragDrop(Composite composit, SaveAction action)
  {
    DropTarget target = new DropTarget(composit,
        DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener()
    {
      @Override
      public void dragEnter(DropTargetEvent event)
      {
        if (event.detail == DND.DROP_DEFAULT)
        {
          if ((event.operations & DND.DROP_COPY) != 0)
            event.detail = DND.DROP_COPY;
          else
            event.detail = DND.DROP_NONE;
        }
        for (int i = 0; i < event.dataTypes.length; i++)
        {
          if (fileTransfer.isSupportedType(event.dataTypes[i]))
          {
            event.currentDataType = event.dataTypes[i];
            // files should only be copied
            if (event.detail != DND.DROP_COPY)
              event.detail = DND.DROP_NONE;
            break;
          }
        }
      }

      @Override
      public void drop(DropTargetEvent event)
      {
        if (event.data == null)
        {
          event.detail = DND.DROP_NONE;
          GUI.getStatusBar().setErrorText("Fehler beim Hinzufügen der Datei");
          return;
        }

        for (String filename : (String[]) event.data)
        {
          action.save(filename);
        }
      }

      @Override
      public void dragLeave(DropTargetEvent event)
      {
      }

      @Override
      public void dragOperationChanged(DropTargetEvent event)
      {
      }

      @Override
      public void dragOver(DropTargetEvent event)
      {
      }

      @Override
      public void dropAccept(DropTargetEvent event)
      {
      }
    });
  }
}
