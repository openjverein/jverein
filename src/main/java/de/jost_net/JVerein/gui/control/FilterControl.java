package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter;
import de.jost_net.JVerein.gui.dialogs.ZusatzfelderAuswahlDialog;
import de.jost_net.JVerein.gui.input.IntegerNullInput;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.Differenz;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.KeyEnum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class FilterControl extends VorZurueckControl
{
  private Calendar calendar = Calendar.getInstance();

  final static String ALLE = "Alle";

  // String für allgemeine Settings z.B. settings1
  protected String settingsprefix = "";

  // String für Zusatzfelder
  private String additionalparamprefix1 = "";

  // String für Zusatfelder Anzahl
  private String additionalparamprefix2 = "";

  protected Settings settings = null;

  /**
   * Map mit allen angewendeten Filtern und dem zugehörigen Input
   */
  private Map<Filter, Input> filterMap = new HashMap<>();

  public FilterControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Gibt alle angewendeten Filter zurück
   * 
   * @return
   */
  public Map<Filter, Object> getFilter()
  {
    Map<Filter, Object> filter = new HashMap<>();
    for (Entry<Filter, Input> entry : filterMap.entrySet())
    {
      Object value = entry.getValue().getValue();
      if (value == null
          || (value instanceof String && ((String) value).isBlank()))
      {
        continue;
      }
      filter.put(entry.getKey(), value);
    }
    return filter;
  }

  /**
   * Gibt alle Angewendeten Filter als Map zurück. Value ist der angezeigte
   * Text.
   * 
   * @param alle
   *          wenn true werden auch die nicht eingeblendeten Filter
   *          zurückgegeben
   * @return
   * @throws RemoteException
   */
  public Map<Filter, String> getFilterText(boolean alle) throws RemoteException
  {
    Map<Filter, String> map = new HashMap<>();
    for (Filter filter : Filter.values())
    {
      Input input = filterMap.get(filter);
      Object value = input == null ? null : input.getValue();
      String text = "";
      if (value == null
          || (value instanceof String && ((String) value).isBlank()))
      {
        if (alle)
        {
          value = "";
        }
        else
        {
          continue;
        }
      }
      if (value instanceof Date)
      {
        text = new SimpleDateFormat("yyyyMMdd").format(value);
      }
      else if (input instanceof SelectInput)
      {
        text = ((SelectInput) input).getText();
      }
      else if (input instanceof CheckboxInput)
      {
        text = ((Boolean) value) ? "Ja" : "Nein";
      }
      else
      {
        text = value.toString();
      }
      if (alle || (!text.isBlank() && !text.equals(ALLE)))
      {
        map.put(filter, text);
      }
    }
    return map;
  }

  /**
   * Erstellt den Filter-Input
   * 
   * @param filter
   * @return
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getFilterInput(Filter filter)
      throws RemoteException, ApplicationException
  {
    if (filterMap.get(filter) != null)
    {
      throw new ApplicationException(
          "Filter kann nicht doppelt verwendet werden: "
              + filter.getAnzeigeText());
    }
    Input input = null;
    String settingValue = settings
        .getString(settingsprefix + filter.getSetting(), "");
    switch (filter.getArt())
    {
      case TEXT:
        input = new TextInput(settingValue, 50);
        input.setName(filter.getAnzeigeText());
        break;
      case CHECKBOX:
        input = new CheckboxInput(Boolean.valueOf(settingValue));
        input.addListener(new FilterListener());
        break;
      case INTEGER:
        input = new IntegerNullInput(
            settingValue != null && !settingValue.isEmpty()
                ? Integer.parseInt(settingValue)
                : null);
        input.setName(filter.getAnzeigeText());
        break;
      case DOUBLE:
        input = new DecimalInput(
            (settingValue != null && !settingValue.isEmpty())
                ? Double.parseDouble(settingValue)
                : null,
            Einstellungen.DECIMALFORMAT);
        input.setName(filter.getAnzeigeText());

        if (filter.equals(Filter.DIFFERENZ_LIMIT))
        {
          Input dif = filterMap.get(Filter.DIFFERENZ);
          if (dif != null && dif.getValue() == null)
          {
            input.setEnabled(false);
          }
        }
        break;
      case DATE:
        Date d = null;
        if (settingValue != null)
        {
          try
          {
            d = new JVDateFormatTTMMJJJJ().parse(settingValue);
          }
          catch (ParseException ignore)
          {
          }
        }
        input = new DateInput(d, new JVDateFormatTTMMJJJJ());
        input.setName(filter.getAnzeigeText());
        break;
      case EIGENSCHAFTEN:
        EigenschaftenAuswahlParameter param = new EigenschaftenAuswahlParameter(
            settingValue);
        final EigenschaftenAuswahlDialog d1 = new EigenschaftenAuswahlDialog(
            param);
        DialogInput dialogInput = new DialogInput(param.getString(), d1)
        {
          @Override
          protected void update() throws OperationCanceledException
          {
            this.getControl().setToolTipText(this.getText());
            super.update();
          }
        };

        d1.addCloseListener(e -> {
          EigenschaftenAuswahlParameter p = (EigenschaftenAuswahlParameter) e.data;
          dialogInput.setText(p == null ? "" : p.getString());
          dialogInput.setValue(p);
          refresh();
        });
        dialogInput.disableClientControl();

        dialogInput.setName(filter.getAnzeigeText());
        dialogInput.setValue(param);
        input = dialogInput;
        break;
      case ZUSATZFELD:
        ZusatzfelderAuswahlDialog dialog = new ZusatzfelderAuswahlDialog(
            settings, additionalparamprefix1, additionalparamprefix2);

        DialogInput dInput = new DialogInput("", dialog)
        {
          @Override
          protected void update() throws OperationCanceledException
          {
            this.getControl().setToolTipText(this.getText());
            String text = "";
            int counter = settings.getInt(additionalparamprefix2 + "counter",
                0);
            for (int i = 1; i <= counter; i++)
            {
              String t = settings
                  .getString(additionalparamprefix1 + i + ".value", "");
              if (!t.isBlank() && !t.equals("false"))
              {
                text += settings.getString(additionalparamprefix1 + i + ".name",
                    "") + " " + t + "\n";
              }
            }
            this.setValue(text);
            super.update();
          }
        };
        dialog.addCloseListener(c -> {
          setZusatzfelderAuswahl(dInput);
          refresh();
        });
        setZusatzfelderAuswahl(dInput);
        dInput.setName("Zusatzfelder");
        dInput.disableClientControl();
        input = dInput;
        break;
      case SELECT:
        boolean alle = true;
        // EnumKey Select
        if (filter.getArray() != null)
        {
          KeyEnum[] array = filter.getArray();
          KeyEnum def = null;
          if (settingValue != null && !settingValue.isBlank())
          {
            try
            {
              int defaultInt = Integer.parseInt(settingValue);
              for (KeyEnum a : array)
              {
                if (defaultInt == a.getKey())
                {
                  def = a;
                  break;
                }
              }
            }
            catch (NumberFormatException ignore)
            {
            }
          }
          input = new SelectInput(array, def);
          if (filter.equals(Filter.DIFFERENZ))
          {
            input.addListener(new DifferenzListener());
          }
        }
        // DBObject Select
        else if (filter.getDbObject() != null)
        {
          DBIterator<?> it = Einstellungen.getDBService()
              .createList(filter.getDbObject());

          DBObject def = null;
          List<?> list = null;
          try
          {
            def = Einstellungen.getDBService()
                .createObject(filter.getDbObject(), settingValue);
          }
          catch (ObjectNotFoundException e)
          {
          }

          if (filter.equals(Filter.ABRECHNUNGSLAUF))
          {
            alle = false;
            it.setOrder("ORDER BY id desc");
            it.setLimit(10);
            if (it != null)
            {
              list = PseudoIterator.asList(it);
            }
          }
          else
          {
            if (it != null)
            {
              list = PseudoIterator.asList(it);

              list.sort((a, b) -> {
                try
                {
                  return BeanUtil.toString(a).toString()
                      .compareTo(BeanUtil.toString(b));
                }
                catch (RemoteException e)
                {
                  Logger.error("Fehler beim Sortieren der Filter-Liste", e);
                  return 0;
                }
              });
            }
          }
          input = new SelectInput(list, def);
        }
        else
        {
          throw new ApplicationException("Select hat keine Liste Definiert!");
        }
        if (alle)
        {
          ((SelectInput) input).setPleaseChoose(ALLE);
        }
        input.setName(filter.getAnzeigeText());
        input.addListener(new FilterListener());
        break;
    }
    filterMap.put(filter, input);

    return input;
  }

  protected void refresh()
  {
    try
    {
      saveFilterSettings();
      TabRefresh();
    }
    catch (RemoteException | ApplicationException e)
    {
      Logger.error("Fehler", e);
    }
  }

  abstract protected void TabRefresh() throws ApplicationException;

  public class FilterListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection)
      {
        return;
      }
      refresh();
    }
  }

  public void init(String settingsprefix, String additionalparamprefix1,
      String additionalparamprefix2)
  {
    if (settingsprefix != null)
      this.settingsprefix = settingsprefix;
    if (additionalparamprefix1 != null)
      this.additionalparamprefix1 = additionalparamprefix1;
    if (additionalparamprefix2 != null)
      this.additionalparamprefix2 = additionalparamprefix2;
  }

  public Settings getSettings()
  {
    return settings;
  }

  public String getAdditionalparamprefix1()
  {
    return additionalparamprefix1;
  }

  public String getAdditionalparamprefix2()
  {
    return additionalparamprefix2;
  }

  private void setZusatzfelderAuswahl(DialogInput input)
  {
    int selected = settings.getInt(additionalparamprefix2 + "selected", 0);
    String string = "";
    if (selected == 0)
    {
      string = "Kein Feld ausgewählt";
    }
    else if (selected == 1)
    {
      string = "1 Feld ausgewählt";
    }
    else
    {
      string = String.format("%d Felder ausgewählt", selected);
    }
    input.setText(string);
  }

  /**
   * Aktuelle Filter speichern.
   * 
   * @throws RemoteException
   */
  public void saveFilterSettings() throws RemoteException
  {
    for (Entry<Filter, Input> entry : filterMap.entrySet())
    {
      Filter filter = entry.getKey();
      Object value = entry.getValue().getValue();

      if (value == null)
      {
        value = "";
      }
      else if (value instanceof GenericObject)
      {
        value = ((GenericObject) value).getID();
      }
      else if (value instanceof Date)
      {
        value = new JVDateFormatTTMMJJJJ().format((Date) value);
      }
      else if (value instanceof KeyEnum)
      {
        value = ((KeyEnum) value).getKey();
      }
      else if (value instanceof EigenschaftenAuswahlParameter)
      {
        value = ((EigenschaftenAuswahlParameter) value).getIdString();
      }
      settings.setAttribute(settingsprefix + filter.getSetting(),
          value.toString());
    }
  }

  private class DifferenzListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      if (event != null && event.type != SWT.Selection
          && event.type != SWT.FocusOut)
      {
        return;
      }
      Differenz diff = (Differenz) getFilter().get(Filter.DIFFERENZ);
      Input doubleInput = filterMap.get(Filter.DIFFERENZ_LIMIT);
      if (doubleInput != null)
      {
        doubleInput.setEnabled(diff != null);
        if (diff == null)
        {
          doubleInput.setValue(null);
        }
      }
      refresh();
    }
  }

  private enum RANGE
  {
    MONAT,
    TAG
  }

  public ToolTipButton getZurueckButton(Input vonDatum, Input bisDatum)
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) vonDatum.getValue();
        Date bis = (Date) bisDatum.getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          vonDatum.setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          bisDatum.setValue(calendar.getTime());
        }
        else
        {
          LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.MONTH, -delta);
          vonDatum.setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          bisDatum.setValue(calendar.getTime());
        }
        refresh();
      }
    }, null, false, "go-previous.png");
  }

  public ToolTipButton getVorButton(Input vonDatum, Input bisDatum)
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) vonDatum.getValue();
        Date bis = (Date) bisDatum.getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          vonDatum.setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          bisDatum.setValue(calendar.getTime());
        }
        else
        {
          LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.MONTH, delta);
          vonDatum.setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          bisDatum.setValue(calendar.getTime());
        }
        refresh();
      }
    }, null, false, "go-next.png");
  }

  private RANGE getRangeTyp(Date von, Date bis) throws ApplicationException
  {
    if (von == null)
    {
      throw new ApplicationException("Bitte Von Datum eingeben!");
    }
    if (bis == null)
    {
      throw new ApplicationException("Bitte Bis Datum eingeben!");
    }
    if (von.after(bis))
    {
      throw new ApplicationException("Von Datum ist nach Bis Datum!");
    }
    calendar.setTime(von);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    calendar.setTime(bis);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    return RANGE.MONAT;
  }

  /**
   * Buttons
   */
  public Button getSuchenButton()
  {
    return new Button("Suchen", c -> refresh(), null, true, "search.png");
  }

  public Button getSpeichernButton()
  {
    Button b = new Button("Filter-Speichern", c -> {
      try
      {
        saveFilterSettings();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    }, null, false, "document-save.png");
    return b;
  }

  public Button getResetButton()
  {
    return new Button("Filter-Reset", c -> {
      settings.setAttribute("id", "");
      settings.setAttribute("profilname", "");

      for (Entry<Filter, Input> entry : filterMap.entrySet())
      {
        Input input = entry.getValue();
        Filter filter = entry.getKey();
        if (input instanceof CheckboxInput)
        {
          input.setValue(false);
        }
        else if (input instanceof IntegerNullInput)
        {
          input.setValue(null);
        }
        else if (input instanceof DecimalInput)
        {
          input.setValue(null);
        }
        else if (input instanceof TextInput)
        {
          input.setValue("");
        }
        else if (filter.equals(Filter.EIGENSCHAFTEN))
        {
          ((DialogInput) input).setText("");
          input.setValue(null);
        }
        else if (filter.equals(Filter.ZUSATZFELD))
        {
          settings.setAttribute(additionalparamprefix2 + "selected", 0);
          ((DialogInput) filterMap.get(filter)).setText("");
        }
        else
        {
          input.setValue(null);
        }
      }
      refresh();
    }, null, false, "eraser.png");
  }
}
