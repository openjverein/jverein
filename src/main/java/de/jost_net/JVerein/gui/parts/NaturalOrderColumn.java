package de.jost_net.JVerein.gui.parts;

import de.willuhn.jameica.gui.parts.AbstractTablePart.AbstractTableItem;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;

public class NaturalOrderColumn extends Column
{
  private static final long serialVersionUID = -7441928271141767865L;

  public NaturalOrderColumn(String id, String name)
  {
    super(id, name);
  }

  public NaturalOrderColumn(String id, String name, Formatter f,
      boolean changeable, int align)
  {
    super(id, name, f, changeable, align);
  }

  @Override
  public int compare(AbstractTableItem a, AbstractTableItem b)
  {
    String left = getFormattedValue(a.value, a.data);
    String right = getFormattedValue(b.value, b.data);

    int l = 0;
    int r = 0;

    while (l < left.length() && r < right.length())
    {
      char lc = left.charAt(l);
      char rc = right.charAt(r);

      if (Character.isDigit(lc) && Character.isDigit(rc))
      {
        int lend = numberEnd(left, l);
        int rend = numberEnd(right, r);

        int result = Integer.compare(Integer.parseInt(left.substring(l, lend)),
            Integer.parseInt(right.substring(r, rend)));

        if (result != 0)
          return result;

        l = lend;
        r = rend;
      }
      else
      {
        int result = Character.compare(lc, rc);

        if (result != 0)
          return result;

        l++;
        r++;
      }
    }

    if (l == left.length() && r == right.length())
      return 0;

    return l == left.length() ? -1 : 1;
  }

  private static int numberEnd(String value, int start)
  {
    int end = start;

    while (end < value.length() && Character.isDigit(value.charAt(end)))
      end++;

    return end;
  }
}
