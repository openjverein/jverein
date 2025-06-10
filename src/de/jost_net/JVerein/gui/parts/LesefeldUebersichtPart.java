package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.LesefeldDetailView;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Listet Namen der definierten Lesefelder und zeigt der Inhalt dieser
 * Lesefelder für ein auswählbares Mitglied an. Für jedes Lesefeld wird ein
 * Bearbeiten- und Löschen-Knopf angezeigt. Als Part implementiert um es ggf.
 * einfach verschieben zu können.
 * 
 * @author Julian
 */
public class LesefeldUebersichtPart implements Part
{

  private ColumnLayout lesefelderListeLayout;

  private Mitglied selectedMitglied;

  private LesefeldAuswerter lesefeldAuswerter;
  
  private AbstractInput mitglied;

  /**
   * Mit selectedMitglied kann ein beliebiges Mitglied in der GUI ausgwählt
   * werden. Ist selectedMitglied==null, wird das erste gefunde Mitglied
   * ausgewählt.
   * 
   * @param selectedMitglied
   *          Auszuwählendes Mitglied.
   */
  public LesefeldUebersichtPart(Mitglied selectedMitglied)
  {
    this.selectedMitglied = selectedMitglied;
  }

  @Override
  public void paint(final Composite parent) throws RemoteException
  {
    ScrolledContainer scrolled = new ScrolledContainer(parent, 1);

    SimpleContainer container = new SimpleContainer(scrolled.getComposite());

    container.addHeadline("Lesefelder");
    container.addLabelPair("Mitglied", getMitglied());

    if (selectedMitglied == null && getMitglied().getValue() != null)
    {
      selectedMitglied = (Mitglied) getMitglied().getValue();
    }
    // LesefelderListeLayout
    // darf nur über die Funktionen
    // addLesefeldEinstellungRow(), updateLesefeldEinstellungRow()
    // und deleteLesefeldEinstellungRow()
    // manipuliert werden.
    lesefelderListeLayout = new ColumnLayout(container.getComposite(), 4);
    lesefeldAuswerter = new LesefeldAuswerter();
    lesefeldAuswerter.setLesefelderDefinitionsFromDatabase();
    if (selectedMitglied != null)
    lesefeldAuswerter
        .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
    lesefeldAuswerter.evalAlleLesefelder();
    List<Lesefeld> lesefelder = lesefeldAuswerter.getLesefelder();
    for (Lesefeld lesefeld : lesefelder)
    {
      addLesefeldEinstellungRow(lesefeld);
    }

    // BUTTON AREA
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.LESEFELDER, false, "question-circle.png");
    buttons.addButton("Neu", new NewLesefeldAction(), null, false, "document-new.png");
    buttons.paint(parent);
    // END BUTTON AREA
  }

  /**
   * Fügt neue GUI-Elemente hinzu für ein neues Lesefeld.
   * 
   * @param lesefeld
   *          Anzuzeigendes, neues Lesefeld.
   * @throws RemoteException
   */
  private void addLesefeldEinstellungRow(final Lesefeld lesefeld)
      throws RemoteException
  {
    // Lesefeld Beschreibung
    final Label label = GUI.getStyleFactory()
        .createLabel(lesefelderListeLayout.getComposite(), SWT.NONE);
    label.setText(lesefeld.getBezeichnung());

    // Lesefeld Inhalt
    TextAreaInput tt = new TextAreaInput(lesefeld.getEvaluatedContent());
    tt.setEnabled(false);
    lesefelderListeLayout.add(tt);

    // Bearbeiten-Button
    Button button = new Button("Bearbeiten", new EditLesefeldAction(lesefeld),
        null, false, "text-x-generic.png");
    lesefelderListeLayout.add(button);

    // Löschen-Button
    button = new Button("Löschen", new DeleteLesefeldAction(lesefeld), null,
        false, "list-remove.png");
    lesefelderListeLayout.add(button);
  }

  /**
   * Aktualisiert die GUI, so dass Inhalt von lese angezeigt wird.
   * 
   * @param lesefeld
   *          Anzuzeigendes, neues Lesefeld.
   * @throws RemoteException
   */
  private void updateLesefeldEinstellungRow(final Lesefeld lesefeld)
      throws RemoteException
  {
    boolean updateThis = false;
    for (Control child : lesefelderListeLayout.getComposite().getChildren())
    {
      if (child instanceof Label)
      {
        Label label = (Label) child;
        if (label.getText().equals(lesefeld.getBezeichnung()))
        {
          updateThis = true;
        }
        else
          updateThis = false;
      }

      if (updateThis)
      {
        if (child instanceof Composite)
        {
          Composite c = (Composite) child;
          for (Control child2 : c.getChildren())
          {
            if (child2 instanceof org.eclipse.swt.widgets.Text)
            {
              org.eclipse.swt.widgets.Text t = (org.eclipse.swt.widgets.Text) child2;
              t.setText(lesefeld.getEvaluatedContent());
            }
          }
        }
      }
    }
    updateView();
  }

  /**
   * Löscht GUI-Elemente von Lesefeld lf, die von addLesefeldEinstellungRow()
   * erzeugt wurden.
   * 
   * @param lf
   *          Zu löschendes Lesefeld.
   * @throws RemoteException
   */
  private void deleteLesefeldEinstellungRow(Lesefeld lf) throws RemoteException
  {
    boolean deleteThis = false;
    lesefelderListeLayout.getComposite().getChildren();
    for (Control child : lesefelderListeLayout.getComposite().getChildren())
    {
      if (child instanceof Label)
      {
        Label label = (Label) child;

        // Solange das Lesefeld noch nicht in DB gespeichert wurde, besitzt lf
        // noch keine eindeutige ID.
        // Nutze daher die Bezeichnung. Die Eindeutigkeit von von der GUI
        // sichergestellt.
        if (label.getText().equals(lf.getBezeichnung()))
        {
          deleteThis = true;
        }
        else
          deleteThis = false;
      }

      if (deleteThis)
      {
        child.dispose();
      }
    }

    updateView();

  }

  /**
   * Veranlasst das Neu-Zeichen (inklusive Aktualisieren des Inhaltes,
   * Größenanpassung und Ausrichtung) der GUI-Elemente für die Lesefelder.
   */
  private void updateView()
  {

    Point currentSizeParentParent = lesefelderListeLayout.getComposite()
        .getParent().getParent().getSize();
    Point sizeParentParent = lesefelderListeLayout.getComposite().getParent()
        .computeSize(currentSizeParentParent.x, SWT.DEFAULT, true);
    lesefelderListeLayout.getComposite().getParent().getParent()
        .setSize(sizeParentParent);

    Point currentSizeParent = lesefelderListeLayout.getComposite().getParent()
        .getSize();
    Point sizeParent = lesefelderListeLayout.getComposite().getParent()
        .computeSize(currentSizeParent.x, SWT.DEFAULT, true);
    lesefelderListeLayout.getComposite().getParent().setSize(sizeParent);

    lesefelderListeLayout.getComposite().redraw();
    lesefelderListeLayout.getComposite().update();
    lesefelderListeLayout.getComposite().layout();

    lesefelderListeLayout.getComposite().getParent().redraw();
    lesefelderListeLayout.getComposite().getParent().update();
    lesefelderListeLayout.getComposite().getParent().layout();
  }

  class NewLesefeldAction implements Action
  {

    @Override
    public void handleAction(Object context)
    {
      openEditLesefeldDialog(null);
    }
  }

  class EditLesefeldAction implements Action
  {

    Lesefeld lesefeld;

    public EditLesefeldAction(Lesefeld lesefeld)
    {
      this.lesefeld = lesefeld;
    }

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      if (selectedMitglied != null)
        openEditLesefeldDialog(lesefeld);
      else
        throw new ApplicationException("Bitte Mitglied auswählen");
    }
  }

  /**
   * Öffnet eine neue View zum Editieren einer Lesefeld-Definition.
   * 
   * @param lesefeld
   *          Zu bearbeitendes Lesefeld oder null, wenn diese Lesefeld angelegt
   *          werden soll.
   * @throws ApplicationException
   */
  private void openEditLesefeldDialog(Lesefeld lesefeld)
  {

    GUI.startView(new LesefeldDetailView(lesefeldAuswerter, 
        lesefeld, selectedMitglied), null);
  }

  class DeleteLesefeldAction implements Action
  {

    Lesefeld lesefeld;

    public DeleteLesefeldAction(Lesefeld lesefeld)
    {
      this.lesefeld = lesefeld;
    }

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      /* Sicherheitsnachfrage */
      YesNoDialog ynd = new YesNoDialog(AbstractDialog.POSITION_CENTER);
      ynd.setText("Achtung! Lesefeld wird gelöscht. Weiter?");
      ynd.setTitle("Löschen");
      Boolean choice;
      try
      {
        choice = (Boolean) ynd.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e1)
      {
        Logger.error("Fehler", e1);
      }

      try
      {
        deleteLesefeldEinstellungRow(lesefeld);
        lesefeldAuswerter.deleteLesefelderDefinition(lesefeld);
        lesefeld.delete();
      }
      catch (RemoteException e)
      {
        String fehler = "Fehler beim Löschen des Lesefeldes";
        Logger.error(fehler, e);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }
  
  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    mitglied = new MitgliedInput().getMitgliedInput(mitglied, selectedMitglied,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    mitglied.addListener(new MitgliedListener());
    mitglied.setMandatory(true);
    return mitglied;
  }
  
  public class MitgliedListener implements Listener
  {

    MitgliedListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Mitglied selected = (Mitglied) getMitglied().getValue();
        if (selected == null || selected == selectedMitglied)
          return;
        selectedMitglied = selected;
        lesefeldAuswerter
            .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
        lesefeldAuswerter.evalAlleLesefelder();
        List<Lesefeld> lesefelder = lesefeldAuswerter.getLesefelder();
        for (Lesefeld lesefeld : lesefelder)
        {
          updateLesefeldEinstellungRow(lesefeld);
        }
      }
      catch (RemoteException e)
      {
        String fehler = "Fehler beim Auswählen des Mitgliedes";
        Logger.error(fehler, e);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }

}
