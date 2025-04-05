package de.jost_net.JVerein;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.willuhn.jameica.system.Application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class Settings
{
  /**
   * In den Methoden test01 und test02 wird geprüft, ob die Settings-Klasse sowohl mit den Zeilenende \n als auch \r\n sauber umgeht.
   * @throws IOException
   */
  @Test
  void test01() throws IOException
  {
    File f1 = getFile();
    FileWriter wrt = new FileWriter(f1);
    wrt.write("wert=x\r\n");
    wrt.close();
    de.willuhn.jameica.system.Settings setting = new de.willuhn.jameica.system.Settings(
        Settings.class);
    assertEquals("x", setting.getString("wert", null));
  }

  @Test
  void test02() throws IOException
  {
    File f1 = getFile();
    FileWriter wrt = new FileWriter(f1);
    wrt.write("wert=x\n");
    wrt.close();
    de.willuhn.jameica.system.Settings setting = new de.willuhn.jameica.system.Settings(
        Settings.class);
    assertEquals("x", setting.getString("wert", null));
  }

  private File getFile()
  {
    return new File(Application.getConfig().getConfigDir() + "/"
        + this.getClass().getCanonicalName() + ".properties");
  }
}
