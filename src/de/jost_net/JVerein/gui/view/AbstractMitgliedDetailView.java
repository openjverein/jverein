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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.KontoauszugAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.MitgliedDuplizierenAction;
import de.jost_net.JVerein.gui.action.MitgliedMailSendenAction;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.PersonalbogenAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.DokumentControl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.util.SimpleVerticalContainer;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedDokument;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractMitgliedDetailView extends AbstractDetailView
{
  // Statische Variable, die den zuletzt ausgewählten Tab speichert.
  private static int tabindex = -1;

  // Die aufgerufene Funktion: B=Bearbeiten, N=Neu, D=Duplizieren
  int funktion = 'B';

  final MitgliedControl control = new MitgliedControl(this);

  @Override
  public void bind() throws Exception
  {
    // Funktion ermitteln
    if (control.getMitgliedsnummer().getValue() == null)
    {
      if (control.getName(false).getValue() == null)
        funktion = 'N';
      else
        funktion = 'D';
    }

    zeichneUeberschrift(); // Einschub Ende

    final SollbuchungControl controlSollb = new SollbuchungControl(this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent(), 1);

    SimpleContainer oben = new SimpleContainer(scrolled.getComposite(), false,
        1);

    final TabFolder folder = new TabFolder(scrolled.getComposite(), SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    DBIterator<Mitglied> zhl = Einstellungen.getDBService()
        .createList(Mitglied.class);
    MitgliedUtils.setNurAktive(zhl);
    MitgliedUtils.setMitglied(zhl);
    zhl.setOrder("ORDER BY name, vorname");

    int anzahlSpalten = Einstellungen.getSettingInt("AnzahlSpaltenStammdaten",2);
    boolean showInTab = Einstellungen.getSettingBoolean("ZeigeStammdatenInTab",
        false);
    zeicheStammdaten(showInTab ? folder : oben.getComposite(), anzahlSpalten);

    anzahlSpalten = Einstellungen.getSettingInt("AnzahlSpaltenMitgliedschaft",
        1);
    showInTab = Einstellungen.getSettingBoolean("ZeigeMitgliedschaftInTab",true);
    zeichneMitgliedschaft(showInTab ? folder : oben.getComposite(),
        anzahlSpalten);

    anzahlSpalten = Einstellungen.getSettingInt("AnzahlSpaltenZahlung",1);
    showInTab = Einstellungen.getSettingBoolean("ZeigeZahlungInTab",true);
    zeichneZahlung(showInTab ? folder : oben.getComposite(), anzahlSpalten);

    showInTab = Einstellungen.getSettingBoolean("ZeigeZusatzbetraegeInTab",true);
    zeichneZusatzbeitraege(showInTab ? folder : oben.getComposite());

    showInTab = Einstellungen.getSettingBoolean("ZeigeMitgliedskontoInTab",true);
    zeichneMitgliedkonto(controlSollb, showInTab ? folder : oben.getComposite());

    showInTab = Einstellungen.getSettingBoolean("ZeigeVermerkeInTab",true);
    zeichneVermerke(showInTab ? folder : oben.getComposite(), 1);

    showInTab = Einstellungen.getSettingBoolean("ZeigeWiedervorlageInTab",true);
    zeichneWiedervorlage(showInTab ? folder : oben.getComposite());

    showInTab = Einstellungen.getSettingBoolean("ZeigeMailsInTab",true);
    zeichneMails(showInTab ? folder : oben.getComposite());

    showInTab = Einstellungen.getSettingBoolean("ZeigeEigenschaftenInTab",true);
    zeichneEigenschaften(showInTab ? folder : oben.getComposite());

    anzahlSpalten = Einstellungen.getSettingInt("AnzahlSpaltenZusatzfelder", 1);
    showInTab = Einstellungen.getSettingBoolean("ZeigeZusatzfelderInTab",true);
    zeichneZusatzfelder(showInTab ? folder : oben.getComposite(),
        anzahlSpalten);

    showInTab = Einstellungen.getSettingBoolean("ZeigeLehrgaengeInTab",true);
    zeichneLehrgaenge(showInTab ? folder : oben.getComposite());

    showInTab = Einstellungen.getSettingBoolean("ZeigeFotoInTab",true);
    zeichneMitgliedfoto(showInTab ? folder : oben.getComposite());

    anzahlSpalten = Einstellungen.getSettingInt("AnzahlSpaltenLesefelder", 1);
    showInTab = Einstellungen.getSettingBoolean("ZeigeLesefelderInTab",true);
    zeichneLesefelder(showInTab ? folder : oben.getComposite(), anzahlSpalten);

    zeichneMitgliedDetail(showInTab ? folder : oben.getComposite());

    zeichneDokumente(showInTab ? folder : oben.getComposite());

    // Aktivier zuletzt ausgewählten Tab.
    if (tabindex != -1)
    {
      folder.setSelection(tabindex);
    }
    folder.addSelectionListener(new SelectionListener()
    {

      // Wenn Tab angeklickt, speicher diesen um ihn später automatisch
      // wieder auszuwählen.
      @Override
      public void widgetSelected(SelectionEvent arg0)
      {
        tabindex = folder.getSelectionIndex();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0)
      {
        //
      }
    });

    zeichneButtonArea(getParent());

  }

  private void zeichneButtonArea(Composite parent) throws RemoteException
  {
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIED, false, "question-circle.png");
    Button zurueckButton = control.getZurueckButton();
    Button vorButton = control.getVorButton();
    buttons.addButton(zurueckButton);
    buttons.addButton(vorButton);
    if (control.getMitglied().isNewObject())
    {
      zurueckButton.setEnabled(false);
      vorButton.setEnabled(false);
    }
    if (!control.getMitglied().isNewObject())
    {
      buttons.addButton(new Button("Kontoauszug", new KontoauszugAction(),
          control.getMitglied(), false, "file-invoice.png"));
    }
    if (isMitgliedDetail())
    {
      buttons.addButton(new Button("Personalbogen", new PersonalbogenAction(),
          control.getCurrentObject(), false, "receipt.png"));
      // R.M. 27.01.2013 Mitglieder sollten aus dem Dialog raus kopiert werden
      // können
      buttons
          .addButton(new Button("Duplizieren", new MitgliedDuplizierenAction(),
              control.getCurrentObject(), false, "edit-copy.png"));
    }
    buttons.addButton("Mail", new MitgliedMailSendenAction(),
        getCurrentObject(), false, "envelope-open.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          control.handleStore();
          zeichneUeberschrift();
        }
        catch (RemoteException | ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "document-save.png");

    buttons.addButton(new Button("Speichern und neu", context -> {
      try
      {
        control.handleStore();

        if (isMitgliedDetail())
        {
          new MitgliedDetailAction().handleAction(null);
        }
        else
        {
          new NichtMitgliedDetailAction().handleAction(null);
        }
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }, null, false, "go-next.png"));

    buttons.paint(parent);
  }

  private void zeichneDokumente(Composite parentComposite)
      throws RemoteException
  {
    if (JVereinPlugin.isArchiveServiceActive()
        && !control.getMitglied().isNewObject())
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Dokumente");

      MitgliedDokument mido = (MitgliedDokument) Einstellungen.getDBService()
          .createObject(MitgliedDokument.class, null);
      mido.setReferenz(Long.valueOf(control.getMitglied().getID()));
      DokumentControl dcontrol = new DokumentControl(this, "mitglieder", true);
      cont.addPart(dcontrol.getDokumenteList(mido));
      ButtonArea butts = new ButtonArea();
      butts.addButton(dcontrol.getNeuButton(mido));
      butts.paint(cont.getComposite());
    }
  }

  private void zeichneMitgliedDetail(Composite parentComposite)
      throws RemoteException
  {
    if (isMitgliedDetail()
        && (Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
    {
      Container cont = getTabOrLabelContainer(parentComposite,
          "Arbeitseinsatz");

      ButtonArea buttonsarbeins = new ButtonArea();
      buttonsarbeins.addButton(control.getArbeitseinsatzNeu());
      buttonsarbeins.paint(cont.getComposite());

      cont.getComposite().setLayoutData(new GridData(GridData.FILL_VERTICAL));
      cont.getComposite().setLayout(new GridLayout(1, false));

      control.getArbeitseinsatzTable().paint(cont.getComposite());
    }
  }

  private void zeichneLesefelder(Composite parentComposite, int spaltenanzahl)
      throws RemoteException
  {
    // TODO: getLesefelder() ist zu langsam. Inhalt von Lesefeldern sollte erst
    // evaluiert werden, wenn Lesefelder-Tab angeklickt wird.
    if ((Boolean) Einstellungen.getEinstellung(Property.USELESEFELDER))
    {
      Input[] lesefelder = control.getLesefelder();
      if (lesefelder != null)
      {
        Container cont = getTabOrLabelContainer(parentComposite, "Lesefelder");
        SimpleVerticalContainer svc = new SimpleVerticalContainer(
            cont.getComposite(), false, spaltenanzahl);
        for (Input inp : lesefelder)
        {
          if (inp == null)
          {
            String errorText = "Achtung! Ungültiges Lesefeld-Skript gefunden. Diesen Fehler bitte unter https://github.com/openjverein/jverein/issues melden!";
            Input errorInput = new TextInput(errorText);
            errorInput.setEnabled(false);
            svc.addInput(errorInput);
            GUI.getStatusBar().setErrorText(errorText);
          }
          else
          {
            svc.addInput(inp);
          }
        }
        svc.arrangeVertically();
        ButtonArea buttonszus = new ButtonArea();
        buttonszus.addButton(control.getLesefelderEdit());
        cont.addButtonArea(buttonszus);
      }
    }
  }

  private void zeichneMitgliedfoto(Composite parentComposite)
      throws RemoteException
  {
    if (isMitgliedDetail() && (Boolean) Einstellungen.getEinstellung(Property.MITGLIEDFOTO))
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Foto");
      cont.addLabelPair("Foto", control.getFoto());
    }
  }

  private void zeichneLehrgaenge(Composite parentComposite)
      throws RemoteException
  {
    if (isMitgliedDetail() && (Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE))
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Lehrgänge");

      cont.getComposite().setLayoutData(new GridData(GridData.FILL_VERTICAL));
      cont.getComposite().setLayout(new GridLayout(1, false));

      ButtonArea buttonslehrg = new ButtonArea();
      buttonslehrg.addButton(control.getLehrgangNeu());
      buttonslehrg.paint(cont.getComposite());
      control.getLehrgaengeTable().paint(cont.getComposite());
    }
  }

  private void zeichneZusatzfelder(Composite parentComposite, int spaltenanzahl)
      throws RemoteException
  {
    Input[] zusatzfelder = control.getZusatzfelder();
    if (zusatzfelder != null)
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Zusatzfelder");
      SimpleVerticalContainer svc = new SimpleVerticalContainer(
          cont.getComposite(), true, spaltenanzahl);
      for (Input inp : zusatzfelder)
      {
        svc.addInput(inp);
      }
      svc.arrangeVertically();
    }
  }

  private void zeichneEigenschaften(Composite parentComposite)
      throws RemoteException
  {
    // if (isMitgliedDetail())
    // {
    Container cont = getTabOrLabelContainer(parentComposite, "Eigenschaften");
    cont.getComposite().setLayout(new GridLayout(1, true));
    control.getEigenschaftenTree().paint(cont.getComposite());
    // }
  }

  private void zeichneWiedervorlage(Composite parentComposite)
      throws RemoteException
  {
    if ((Boolean) Einstellungen.getEinstellung(Property.WIEDERVORLAGE))
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Wiedervorlage");

      cont.getComposite().setLayoutData(new GridData(GridData.FILL_VERTICAL));
      cont.getComposite().setLayout(new GridLayout(1, false));

      ButtonArea buttonswvl = new ButtonArea();
      buttonswvl.addButton(control.getWiedervorlageNeu());
      buttonswvl.paint(cont.getComposite());
      control.getWiedervorlageTable().paint(cont.getComposite());
    }
  }

  private void zeichneMails(Composite parentComposite) throws RemoteException
  {
    if ((String) Einstellungen.getEinstellung(Property.SMTPSERVER) != null
        && ((String) Einstellungen.getEinstellung(Property.SMTPSERVER))
            .length() > 0)
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Mails");

      control.getMailTable().paint(cont.getComposite());
    }
  }

  private void zeichneVermerke(Composite parentComposite, int spaltenanzahl)
      throws RemoteException
  {
    if ((Boolean) Einstellungen.getEinstellung(Property.VERMERKE))
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Vermerke");
      SimpleContainer cols = new SimpleContainer(cont.getComposite(), true,
          spaltenanzahl * 2);

      // Stelle sicher, dass Eingabefeld sich über mehrere Zeilen erstreckt.
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.minimumHeight = 80;
      // wenn der Vermerk mehr Zeilen benötigt, sollte er die Scrollbar
      // einblenden.
      gridData.heightHint = 80;
      cols.getComposite().setLayoutData(gridData);

      cols.addLabelPair("Vermerk 1", control.getVermerk1());
      cols.addLabelPair("Vermerk 2", control.getVermerk2());
    }
  }

  /**
   * Zeichnet das Mitgliedskonto, wenn dieses aktiviert ist und es sich nicht um
   * ein neues Mitglied handelt (für dieses macht ein Mitgliedskonto noch kein
   * Sinn!)
   * 
   */
  private void zeichneMitgliedkonto(SollbuchungControl controlSollb,
      Composite parentComposite) throws RemoteException
  {
    if (!control.getMitglied().isNewObject())
    {
      Container cont = getTabOrLabelContainer(parentComposite,
          "Mitgliedskonto");

      cont.getComposite().setLayoutData(new GridData(GridData.FILL_VERTICAL));
      cont.getComposite().setLayout(new GridLayout(1, false));

      ButtonArea buttonszus = new ButtonArea();
      buttonszus.addButton(control.getSollbuchungNeu());
      buttonszus.paint(cont.getComposite());
      controlSollb.getMitgliedskontoTree(control.getMitglied())
          .paint(cont.getComposite());
    }
  }

  private void zeichneZusatzbeitraege(Composite parentComposite)
      throws RemoteException
  {
    if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
    {
      Container cont = getTabOrLabelContainer(parentComposite, "Zusatzbeträge");

      cont.getComposite().setLayoutData(new GridData(GridData.FILL_VERTICAL));
      cont.getComposite().setLayout(new GridLayout(1, false));

      ButtonArea buttonszus = new ButtonArea();
      buttonszus.addButton(control.getZusatzbetragNeu());
      buttonszus.paint(cont.getComposite());
      control.getZusatzbetraegeTable().paint(cont.getComposite());
    }
  }

  private void zeichneZahlung(Composite parentComposite, int spaltenanzahl)
      throws RemoteException
  {
    Container container = getTabOrLabelContainer(parentComposite, "Zahlung");
    GridLayout layout = new GridLayout(1, false);
    container.getComposite().setLayout(layout);

    ButtonArea buttons1 = new ButtonArea();
    buttons1.addButton(control.getKontoDatenLoeschenButton());
    buttons1.paint(container.getComposite());
    
    LabelGroup zahlungsweg = new LabelGroup(container.getComposite(),
        "Zahlungsweg");

    zahlungsweg.addInput(control.getZahlungsweg());
    if (isMitgliedDetail())
    {
      switch (Beitragsmodel.getByKey(
          (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)))
      {
        case GLEICHERTERMINFUERALLE:
          break;
        case MONATLICH12631:
          zahlungsweg.addInput(control.getZahlungsrhythmus());
          break;
        case FLEXIBEL:
          zahlungsweg.addInput(control.getZahlungstermin());
          break;
      }
    }

    LabelGroup bankverbindung = control
        .getBankverbindungLabelGroup(container.getComposite());

    SimpleVerticalContainer cols = new SimpleVerticalContainer(
        bankverbindung.getComposite(), false, spaltenanzahl);

    cols.addInput(control.getMandatID());
    cols.addInput(control.getMandatDatum());
    cols.addInput(control.getMandatVersion());
    cols.addInput(control.getLetzteLastschrift());
    cols.addInput(control.getIban());
    cols.addInput(control.getBic());
    cols.arrangeVertically();

    LabelGroup abweichenderKontoInhaber = control
        .getAbweichenderKontoinhaberLabelGroup(container.getComposite());
    SimpleVerticalContainer cols2 = new SimpleVerticalContainer(
        abweichenderKontoInhaber.getComposite(), false, spaltenanzahl);
    
    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton(control.getMitglied2KontoinhaberEintragenButton());
    addButtonArea(buttons2, cols2.getComposite());
    cols2.addInput(control.getKtoiPersonenart());
    cols2.addInput(control.getKtoiAnrede());
    cols2.addInput(control.getKtoiTitel());
    cols2.addInput(control.getKtoiName());
    cols2.addInput(control.getKtoiVorname());
    cols2.addInput(control.getKtoiStrasse());
    cols2.addInput(control.getKtoiAdressierungszusatz());
    cols2.addInput(control.getKtoiPlz());
    cols2.addInput(control.getKtoiOrt());
    if ((Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN))
    {
      cols2.addInput(control.getKtoiStaat());
    }
    cols2.addInput(control.getKtoiEmail());
    cols2.addInput(control.getKtoiGeschlecht());
    // cols.addInput(control.getBlz());
    // cols.addInput(control.getKonto());
    cols2.arrangeVertically();
  }

  /**
   * Erzeugt einen Container, der in einem TabFolder oder einer LabelGroup
   * eingebettet ist. Ist parentComposite ein TabFolder wird SimpleContainer in
   * eine TabGroup eingebettet, anderenfalls in eine LabelGroup.
   * 
   * @param parentComposite
   *          Parent composite in das TabGroup bzw. LabelGroup und
   *          SimpleContainer gezeichnet wird.
   * @param titel
   *          Beschriftung von TabGroup bzw. LabelGroup
   * @return SimpleContainer, in den Inhalt gezeichnet werden kann.
   */
  private Container getTabOrLabelContainer(Composite parentComposite,
      String titel)
  {
    Container container;
    if (parentComposite instanceof TabFolder)
    {
      container = new TabGroup((TabFolder) parentComposite, titel);
    }
    else
    {
      container = new LabelGroup(parentComposite, titel);
    }
    return container;
  }

  private void zeichneMitgliedschaft(Composite parentComposite,
      int spaltenanzahl) throws RemoteException
  {
    if (isMitgliedDetail())
    {
      Container container = getTabOrLabelContainer(parentComposite,
          "Mitgliedschaft");

      SimpleVerticalContainer cols = new SimpleVerticalContainer(
          container.getComposite(), false, spaltenanzahl);

      if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        cols.addInput(control.getExterneMitgliedsnummer());
      }
      else
      {
        cols.addInput(control.getMitgliedsnummer());
      }
      cols.addInput(control.getBeitragsgruppe(true));
      cols.addInput(control.getEintritt());
      cols.addInput(control.getAustritt());
      if ((Boolean) Einstellungen.getEinstellung(Property.INDIVIDUELLEBEITRAEGE))
      {
        cols.addInput(control.getIndividuellerBeitrag());
      }
      cols.addInput(control.getKuendigung());
      if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM)
          && control.getMitglied().getPersonenart().equalsIgnoreCase("n"))
      {
        cols.addInput(control.getSterbetag());
      }
      cols.arrangeVertically();
      if ((Boolean) Einstellungen.getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN))
      {
        container.addPart(control.getMitgliedSekundaereBeitragsgruppeView());
      }

      container.addPart(control.getZukuenftigeBeitraegeView());

      // Wenn es mindestens eine Beitragsgruppe mit Beitragsart
      // "Familienangehöriger" gibt, zeige Familienverband-Part.
      // Dieser Familien-Part soll über die komplette Breite angezeigt werden,
      // kann daher nicht im SimpleVerticalContainer angezeigt werden.
      DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      it.addFilter("beitragsart = ?",
          ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey());
      if (it.hasNext())
      {
        container.addPart(control.getFamilienverband());
      }
    }
  }

  /**
   * Zeichnet GUI-Felder für Stammdaten. Wenn Kommunikationsdaten aktiviert
   * sind, werden drei Spalten angezeigt, ansonsten zwei.
   * 
   * @param parentComposite
   *          Composite auf dem gezeichnet wird.
   * @throws RemoteException
   */
  private void zeicheStammdaten(Composite parentComposite, int spaltenanzahl)
      throws RemoteException
  {
    Container container = getTabOrLabelContainer(parentComposite, "Stammdaten");
    SimpleVerticalContainer cols = new SimpleVerticalContainer(
        container.getComposite(), true, spaltenanzahl);

    if (!isMitgliedDetail())
    {
      cols.addInput(control.getMitgliedstyp());
    }
    cols.addInput(control.getAnrede());
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("n"))
    {
      cols.addInput(control.getTitel());
    }
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("j"))
    {
      control.getName(true).setName("Name Zeile 1");
      control.getVorname().setName("Name Zeile 2");
      control.getVorname().setMandatory(false);
    }
    cols.addInput(control.getName(true));
    cols.addInput(control.getVorname());
    cols.addInput(control.getAdressierungszusatz());

    cols.addInput(control.getStrasse());
    cols.addInput(control.getPlz());
    cols.addInput(control.getOrt());
    if ((Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN))
    {
      cols.addInput(control.getStaat());
    }
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("n"))
    {
      cols.addInput(control.getGeburtsdatum());
      cols.addInput(control.getGeschlecht());
    }
    else
    {
      cols.addInput(control.getLeitwegID());
    }

    if ((Boolean) Einstellungen.getEinstellung(Property.KOMMUNIKATIONSDATEN))
    {
      cols.addInput(control.getTelefonprivat());
      cols.addInput(control.getHandy());
      cols.addInput(control.getTelefondienstlich());
      cols.addInput(control.getEmail());
    }
    cols.arrangeVertically();
  }

  /**
   * Zeichnet den Mitglieds-/Adressnamen in die Überschriftszeile
   */
  private void zeichneUeberschrift() throws RemoteException
  {
    String mgname = "";
    if (funktion == 'N')
      mgname = "- Neuanlage - ";
    if (funktion == 'D')
      mgname = "- Duplizieren - ";

    if (funktion == 'B' && isMitgliedDetail())
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        mgname = (String) control.getExterneMitgliedsnummer().getValue();
        if (mgname == null || mgname.isEmpty())
          mgname = "?";
        mgname = mgname + " - ";
      }
      else
        mgname = (String) control.getMitgliedsnummer().getValue() + " - ";
    }

    if (control.getName(false).getValue() != null)
      if (((String) control.getName(false).getValue()).isEmpty() == false)
      {
        mgname = mgname + (String) control.getName(false).getValue();
        if (((String) control.getTitel().getValue()).isEmpty() == false)
          mgname = mgname + ", " + (String) control.getTitel().getValue() + " "
              + (String) control.getVorname().getValue();
        else if (((String) control.getVorname().getValue()).isEmpty() == false)
          mgname = mgname + ", " + (String) control.getVorname().getValue();
      }
    GUI.getView().setTitle(getTitle() + " (" + mgname.trim() + ")");
  }

  public abstract String getTitle();

  public abstract boolean isMitgliedDetail();

  /**
   * Fuegt eine neue ButtonArea ohne Seperator hinzu.
   * @param buttonArea die hinzuzufuegende Button-Area.
   * @param composite in den gezeichnet werden soll
   * Code ist aus de.willuhn.jameica.gui.util.Container kopiert
   */
  public void addButtonArea(ButtonArea buttonArea, Composite composite)
  {
    try
    {
      final GridData g = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
      g.horizontalSpan = 2;
      final Composite comp = new Composite(composite,SWT.NONE);
      comp.setLayoutData(g);

      final GridLayout gl = new GridLayout();
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      comp.setLayout(gl);
      buttonArea.paint(comp);
    }
    catch (RemoteException e)
    {
      Logger.error("error while adding button area",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen des Buttons."),StatusBarMessage.TYPE_ERROR));
    }
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
