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

package de.jost_net.JVerein.gui.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.gui.control.listener.TreeEditListener;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein TreePart, bei dem Spalten editiert werden können. Dazu müssen die Spalten
 * <code>editable</code> sein. Per
 * <code>addChangeListener(TableChangeListener l)</code> kann ein Listener
 * angehängt werden, der bei Änderungen benachrichtigt wird.
 */
public class EditTreePart extends TreePart
{
  private TreeEditor editor;

  private Tree tree;

  private int selectedCol = -1;

  private List<TableChangeListener> changeListeners = new ArrayList<>();

  private List<TreeEditListener> editListeners = new ArrayList<>();

  public EditTreePart(Object object, Action action)
  {
    super(object, action);
  }

  public EditTreePart(List<?> list, Action action)
  {
    super(list, action);
  }

  public EditTreePart(GenericIterator<?> list, Action action)
  {
    super(list, action);
  }

  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    if (!e.equals(Feature.Event.PAINT))
    {
      return ctx;
    }
    tree = (Tree) ctx.control;
    if (this.changeable)
    {
      this.editor = new TreeEditor(tree);
      this.editor.horizontalAlignment = SWT.LEFT;
      this.editor.grabHorizontal = true;

      tree.addListener(SWT.MouseDown, new Listener()
      {
        @Override
        public void handleEvent(Event e)
        {
          // Bearbeiten nur dann, wenn man mit der linken Maustaste klickt
          if (e.button != 1)
            return;

          selectedCol = -1;

          final TreeItem item = getSelectedItem(new Point(e.x, e.y),
              tree.getColumnCount(), tree.getItems());

          if (item == null || selectedCol == -1)
            return;

          final int index = selectedCol;

          // Jetzt checken wir noch, ob die Spalte änderbar ist
          final Column col = columns.get(index);
          if (!col.canChange())
            return;

          // Wir rufen den Listener auf, wenn dieser false zurückgibt, soll das
          // Feld nicht bearbeitet werden können.
          // Das ist nötig, da es bei einem Tree verschiedene Nodes Typen gibt,
          // die nicht alle gleich behandelt werden sollen.
          for (TreeEditListener l : editListeners)
          {
            if (!l.editItem(item.getData(), col.getColumnId()))
            {
              return;
            }
          }

          final String oldValue = item.getText(index);

          final Control editorControl = getEditorControl(index, item, oldValue);
          if (editorControl == null)
            return;
          editor.setEditor(editorControl, item, index);

          // Wir merken uns noch die letzte Farbe des Items.
          // Denn falls der User etwas Ungültiges eingibt, färben wir
          // sie rot. Allerdings wollen wir sie anschliessend
          // wieder auf die richtige ursprüngliche Farbe
          // zurücksetzen, wenn der User den Wert korrigiert hat.
          if (item.getData("color") == null)
          {
            // wir hatten den Wert noch nicht gespeichert
            item.setData("color", item.getForeground());
          }
          final org.eclipse.swt.graphics.Color color = (org.eclipse.swt.graphics.Color) item
              .getData("color");

          // Wir deaktivieren den Default-Button für den Zeitraum der
          // Bearbeitung
          Button b = GUI.getShell().getDefaultButton();
          final boolean enabled;
          if (b != null && !b.isDisposed() && b.isEnabled())
          {
            enabled = b.getEnabled();
            b.setEnabled(false);
          }
          else
            enabled = false;

          //////////////////////////////////////////////////////////////////////
          // Beendet das Editieren
          final Runnable done = new Runnable()
          {
            @Override
            public void run()
            {
              if (editorControl != null)
                editorControl.dispose();

              Button b = GUI.getShell().getDefaultButton();
              if (b != null && !b.isDisposed())
                b.setEnabled(enabled);

              // Aktuelle Zeile markieren
              if (!item.isDisposed())
                select(item.getData());
            }
          };
          //
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          // Übernimmt die Änderungen
          final Runnable commit = new Runnable()
          {
            @Override
            public void run()
            {
              try
              {
                String newValue = getControlValue(editorControl);
                if (oldValue == null && newValue == null)
                  return; // nothing changed
                if (oldValue != null && oldValue.equals(newValue))
                  return; // nothing changed

                // Wir versuchen den neuen Wert zu formatieren. Wenn es nicht
                // klappt, verwenden wir ihn so wie er ist.
                String formattedValue = newValue;
                try
                {
                  // Wird gebraucht, um den ursprünglichen Typ zu kennen.
                  Object value = BeanUtil.get(item.getData(),
                      col.getColumnId());
                  Object o = newValue;
                  if (value instanceof Double)
                  {
                    o = Double.parseDouble(newValue);
                  }
                  else if (value instanceof Integer)
                  {
                    o = Integer.parseInt(newValue);
                  }
                  formattedValue = col.getFormattedValue(o, item);
                }
                catch (Exception e)
                {
                  Logger.error(
                      "error while formatting new value: " + e.getMessage());
                }

                try
                {
                  item.setText(index, formattedValue);

                  for (TableChangeListener l : changeListeners)
                  {
                    l.itemChanged(item.getData(), col.getColumnId(), newValue);
                  }
                  if (color != null && !item.isDisposed())
                    item.setForeground(index, color);
                }
                catch (SWTException | ApplicationException ae)
                {
                  if (!item.isDisposed())
                    item.setForeground(index, Color.ERROR.getSWTColor());
                  String msg = ae.getMessage();
                  if (msg == null || msg.length() == 0)
                  {
                    msg = "Fehler beim Ändern des Wertes";
                    Logger.error("error while changing value", ae);
                  }
                  GUI.getStatusBar().setErrorText(msg);
                }
              }
              finally
              {
                done.run();
              }
            }
          };
          //
          //////////////////////////////////////////////////////////////////////

          // Listener für Tastatur
          editorControl.addTraverseListener(new TraverseListener()
          {
            @Override
            public void keyTraversed(TraverseEvent e)
            {
              if (!editorControl.isFocusControl())
                return;

              if (e.detail == SWT.TRAVERSE_RETURN)
              {
                e.doit = false;
                commit.run();
              }
              else if (e.detail == SWT.TRAVERSE_ESCAPE)
              {
                e.doit = false;
                done.run();
              }
            }
          });
          // Listener für Maus
          editorControl.addFocusListener(new FocusAdapter()
          {
            @Override
            public void focusLost(FocusEvent e)
            {
              commit.run();
            }
          });
        }
      });
    }
    return ctx;
  }

  /**
   * Das zu bearbeitende Item bestimmen
   * 
   * @param pt
   *          der angeklickte Punkt
   * @param cols
   *          Anzahl Spalten des Trees
   * @param items
   *          die SubItems des aktuellen Nodes
   * @return TreeItem
   */
  private TreeItem getSelectedItem(Point pt, int cols, TreeItem[] items)
  {
    for (TreeItem current : items)
    {
      for (int i = 0; i < cols; ++i)
      {
        Rectangle rect = current.getBounds(i);
        if (rect.contains(pt))
        {
          selectedCol = i;
          return current;
        }
      }
      TreeItem item = getSelectedItem(pt, cols, current.getItems());
      if (item != null)
      {
        return item;
      }
    }
    return null;
  }

  /**
   * fügt dem Tree einen Listener hinzu, der ausgelöst wird, wenn ein Feld
   * änderbar ist und vom Benutzer gäendert wurde.
   * 
   * @param l
   *          der Listener.
   */
  public void addChangeListener(TableChangeListener l)
  {
    if (l != null)
      this.changeListeners.add(l);
  }

  /**
   * fügt dem Tree einen Listener hinzu, der ausgeloöt wird, wenn ein Feld
   * änderbar ist und vom Benutzer zum bearbeitet angeklickt wurde.
   * 
   * @param l
   *          der Listener.
   */
  public void addEditListener(TreeEditListener l)
  {
    if (l != null)
      this.editListeners.add(l);
  }

  /**
   * Liefert das Editor-Control.
   * 
   * @param row
   *          die Spalte.
   * @param item
   *          das Tabellen-Element.
   * @param oldValue
   *          der bisherige Wert.
   * @return der Editor oder {@code null}, wenn das Bearbeiten nicht erlaubt
   *         werden soll.
   */
  protected Control getEditorControl(int row, TreeItem item,
      final String oldValue)
  {
    Text newText = new Text(tree, SWT.NONE);
    newText.setText(oldValue);
    newText.selectAll();
    newText.setFocus();

    // Falls der tree neu geladen wird sofort auch den Editro entfernen
    item.addDisposeListener(e -> newText.dispose());
    return newText;
  }

  /**
   * Liefert den eingegebenen Wert im Editor.
   * 
   * @param control
   *          das Control des Editors.
   * @return der eingegebene Wert.
   */
  protected String getControlValue(Control control)
  {
    if (control instanceof Text)
      return ((Text) control).getText();
    else
      return "";
  }
}
