package de.jost_net.JVerein.io.idea;

import java.util.List;

import de.jost_net.JVerein.util.Geschaeftsjahr;

public interface IDEATable<T>
{
  String getName();

  String getFileName();

  List<IDEAColumn<T>> getColumns();

  List<IDEAReference> getReferences();

  List<T> getLines(Geschaeftsjahr jahr) throws Exception;
}
