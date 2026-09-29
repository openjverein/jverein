package de.jost_net.JVerein.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.io.idea.AnfangsbestandIDEATable;
import de.jost_net.JVerein.io.idea.BuchungIDEATable;
import de.jost_net.JVerein.io.idea.IDEATable;
import de.jost_net.JVerein.io.idea.KontoIDEATable;
import de.jost_net.JVerein.io.idea.BuchungsartIDEATable;
import de.jost_net.JVerein.io.idea.BuchungsklasseIDEATable;
import de.jost_net.JVerein.io.idea.IDEAColumn;
import de.jost_net.JVerein.io.idea.IDEAColumn.IDEAType;
import de.jost_net.JVerein.io.idea.IDEAReference;
import de.jost_net.JVerein.io.idea.SteuerIDEATable;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.GeschaeftsJahrList;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class IdeaFormatReport implements Exporter
{

  private final String DTD_FILE = "gdpdu-01-09-2004.dtd";

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {

    try (ZipOutputStream os = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(file))))
    {
      @SuppressWarnings("unchecked")
      Map<Filter, Object> filter = (Map<Filter, Object>) objects[0];
      int j = ((GeschaeftsJahrList) filter.get(Filter.GESCHAEFTSJAHR)).getKey();
      Geschaeftsjahr jahr = new Geschaeftsjahr(j);

      monitor.setStatusText("Erstelle Datenabzug");

      os.putNextEntry(new ZipEntry(DTD_FILE));
      try (InputStream is = Application.getPluginLoader()
          .getManifest(JVereinPlugin.class).getClassLoader()
          .getResourceAsStream(DTD_FILE))
      {
        if (is == null)
        {
          throw new IOException(DTD_FILE + " nicht gefunden");
        }
        is.transferTo(os);
      }
      finally
      {
        os.closeEntry();
      }

      List<IDEATable<?>> tables = new ArrayList<IDEATable<?>>();
      tables.add(new SteuerIDEATable());
      tables.add(new KontoIDEATable());
      tables.add(new BuchungsartIDEATable());
      tables.add(new BuchungsklasseIDEATable());
      tables.add(new BuchungIDEATable());
      tables.add(new AnfangsbestandIDEATable());

      Map<Class<?>, IDEATable<?>> registry = createTableRegistry(tables);

      writeIndexXml(tables, registry, os);

      for (IDEATable<?> table : tables)
      {
        addTables(table, os, jahr, monitor);
      }

      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setStatusText("Datenabzug erstellt");
      monitor.setPercentComplete(100);
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("error while creating export", e);
      throw new ApplicationException(
          "Fehler beim Erstellen des IDEA-Datenabzugs: " + e.getMessage(), e);
    }
  }

  private <T> void addTables(IDEATable<T> table, ZipOutputStream os,
      Geschaeftsjahr jahr, ProgressMonitor monitor) throws Exception
  {
    monitor.log(table.getFileName());

    os.putNextEntry(new ZipEntry(table.getFileName()));
    try
    {
      for (T row : table.getLines(jahr))
      {
        writeRow(table, row, os);
      }
    }
    finally
    {
      os.closeEntry();
    }
  }

  private <T> void writeRow(IDEATable<T> table, T row, ZipOutputStream os)
      throws Exception
  {
    List<IDEAColumn<T>> columns = table.getColumns();

    for (int i = 0; i < columns.size(); i++)
    {
      if (i > 0)
      {
        os.write(";".getBytes());
      }
      String wert = columns.get(i).getValue(row);
      if (columns.get(i).getType() != IDEAType.NUMERIC)
      {
        // Keine Zahl, also escapen
        wert = wert.replaceAll("\"", "\"\"");
        wert = "\"" + wert + "\"";
      }
      os.write(wert.getBytes("UTF-8"));
    }
    os.write("\r\n".getBytes());
  }

  private Map<Class<?>, IDEATable<?>> createTableRegistry(
      List<IDEATable<?>> tables)
  {
    Map<Class<?>, IDEATable<?>> registry = new HashMap<Class<?>, IDEATable<?>>();

    for (IDEATable<?> table : tables)
    {
      registry.put(table.getClass(), table);
    }

    return registry;
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != BuchungListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return IdeaFormatReport.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.zip" };
      }
    };
    return new IOFormat[] { f };
  }

  private void writeIndexXml(List<IDEATable<?>> tables,
      Map<Class<?>, IDEATable<?>> registry, ZipOutputStream os)
      throws IOException
  {
    StringBuilder xml = new StringBuilder();

    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    xml.append("<!DOCTYPE DataSet SYSTEM \"" + DTD_FILE + "\">\r\n");

    xml.append("<DataSet>\r\n");
    xml.append("  <Version>1.0</Version>\r\n");
    xml.append("  <DataSupplier>\r\n");
    xml.append("    <Name>JVerein</Name>\r\n\r\n");

    xml.append("    <Location>Deutschland</Location>\r\n\r\n");
    xml.append("    <Comment>IDEA-Datenabzug aus JVerein</Comment>\r\n");
    xml.append("  </DataSupplier>\r\n");
    xml.append("  <Media>\r\n");
    xml.append("    <Name>JVerein IDEA Export</Name>\r\n");
    for (IDEATable<?> table : tables)
    {
      writeTableXml(xml, table, registry);
    }
    xml.append("  </Media>\r\n");
    xml.append("</DataSet>\r\n");

    os.putNextEntry(new ZipEntry("index.xml"));
    try
    {
      os.write(xml.toString().getBytes("UTF-8"));
    }
    finally
    {
      os.closeEntry();
    }
  }

  private void writeTableXml(StringBuilder xml, IDEATable<?> table,
      Map<Class<?>, IDEATable<?>> registry)
  {
    xml.append("    <Table>\r\n");
    xml.append("      <URL>").append(xml(table.getFileName()))
        .append("</URL>\r\n");
    xml.append("      <Name>").append(xml(table.getName()))
        .append("</Name>\r\n");

    xml.append("      <UTF8/>\r\n");
    xml.append("      <DecimalSymbol>,</DecimalSymbol>\r\n");
    xml.append("      <DigitGroupingSymbol>.</DigitGroupingSymbol>\r\n");
    xml.append("      <VariableLength>\r\n");

    List<? extends IDEAColumn<?>> columns = table.getColumns();

    for (IDEAColumn<?> column : columns)
    {
      writeColumnXml(xml, column);
    }

    for (IDEAReference reference : table.getReferences())
    {
      writeReferenceXml(xml, table, reference, registry);
    }

    xml.append("      </VariableLength>\r\n");
    xml.append("    </Table>\r\n");
  }

  private void writeColumnXml(StringBuilder xml, IDEAColumn<?> column)
  {
    String element = column.isPrimaryKey() ? "VariablePrimaryKey"
        : "VariableColumn";

    xml.append("        <").append(element).append(">\r\n");
    xml.append("          <Name>").append(xml(column.getName()))
        .append("</Name>\r\n");

    switch (column.getType())
    {
      case ALPHANUMERIC:
        xml.append("          <AlphaNumeric/>\r\n");
        if (column.getLength() != null)
        {
          xml.append("          <MaxLength>").append(column.getLength())
              .append("</MaxLength>\r\n");
        }
        break;

      case NUMERIC:
        xml.append("          <Numeric>\r\n");

        if (column.getAccuracy() != null)
        {
          xml.append("            <Accuracy>").append(column.getAccuracy())
              .append("</Accuracy>\r\n");
        }
        xml.append("          </Numeric>\r\n");
        break;

      case DATE:
        xml.append("          <Date>\r\n");

        if (column.getFormat() != null)
        {
          xml.append("            <Format>").append(xml(column.getFormat()))
              .append("</Format>\r\n");
        }
        xml.append("          </Date>\r\n");

        break;

      default:
        throw new IllegalStateException(
            "Unbekannter IDEA-Datentyp: " + column.getType());
    }

    xml.append("        </").append(element).append(">\r\n");
  }

  private void writeReferenceXml(StringBuilder xml, IDEATable<?> sourceTable,
      IDEAReference reference, Map<Class<?>, IDEATable<?>> registry)
  {
    IDEATable<?> targetTable = registry.get(reference.getTargetTable());

    if (targetTable == null)
    {
      throw new IllegalStateException(
          "Zieltabelle fuer Referenz nicht registriert: "
              + reference.getTargetTable().getName());
    }

    IDEAColumn<?> sourceColumn = findColumn(sourceTable,
        reference.getFromColumn());

    if (sourceColumn == null)
    {
      throw new IllegalStateException(
          "Referenzquelle '" + reference.getFromColumn()
              + "' existiert nicht in Tabelle '" + sourceTable.getName() + "'");
    }

    IDEAColumn<?> targetColumn = findColumn(targetTable,
        reference.getTargetColumn());

    if (targetColumn == null)
    {
      throw new IllegalStateException(
          "Referenzziel '" + reference.getTargetColumn()
              + "' existiert nicht in Tabelle '" + targetTable.getName() + "'");
    }

    if (sourceColumn.getType() != targetColumn.getType())
    {
      throw new IllegalStateException(
          "Datentypen der Referenz stimmen nicht ueberein: "
              + sourceTable.getName() + "." + sourceColumn.getName() + " -> "
              + targetTable.getName() + "." + targetColumn.getName());
    }

    xml.append("        <ForeignKey>\r\n");
    xml.append("          <Name>").append(xml(reference.getFromColumn()))
        .append("</Name>\r\n");
    xml.append("          <References>").append(xml(targetTable.getName()))
        .append("</References>\r\n");
    xml.append("          <Alias>\r\n");
    xml.append("            <From>").append(xml(reference.getFromColumn()))
        .append("</From>\r\n");
    xml.append("            <To>").append(xml(reference.getTargetColumn()))
        .append("</To>\r\n");
    xml.append("          </Alias>\r\n");
    xml.append("        </ForeignKey>\r\n");
  }

  private IDEAColumn<?> findColumn(IDEATable<?> table, String name)
  {
    for (IDEAColumn<?> column : table.getColumns())
    {
      if (name.equals(column.getName()))
        return column;
    }

    return null;
  }

  private String xml(String value)
  {
    if (value == null)
      return "";

    return value.replace("&", "&amp;").replace("\"", "&quot;")
        .replace("<", "&lt;").replace(">", "&gt;");
  }

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.IDEA_DATEINAME, object) + ".zip";
  }

  @Override
  public String getName()
  {
    return "IDEA-Export";
  }

  @Override
  public Filter[] getAusgabeParameter(Object object)
  {
    return new Filter[] { Filter.GESCHAEFTSJAHR };
  }

}
