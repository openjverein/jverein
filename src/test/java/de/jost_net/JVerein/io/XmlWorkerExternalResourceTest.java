package de.jost_net.JVerein.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlWorkerExternalResourceTest
{
  private static HttpServer server;

  private static int port;

  private static volatile boolean hasRequest = false;

  // HTTP-Testserver
  @BeforeAll
  static void startServer() throws IOException
  {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

    port = server.getAddress().getPort();

    server.createContext("/", exchange -> {
      hasRequest = true;
      byte[] response = ("TEST RESOURCE").getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      try
      {
        exchange.getResponseBody().write(response);
      }
      finally
      {
        exchange.close();
      }
    });

    server.start();
  }

  @AfterAll
  static void stopServer()
  {
    if (server != null)
    {
      server.stop(0);
    }
  }

  void renderHtml(String html) throws DocumentException, IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter writer = PdfWriter.getInstance(doc, bos);
    doc.open();
    doc.newPage();
    doc.add(new Paragraph("Formulartest"));

    ColumnText ct = new ColumnText(writer.getDirectContent());
    ct.setSimpleColumn(0, 0, doc.getPageSize().getWidth(),
        doc.getPageSize().getHeight());

    ElementList elemente = new FormularAufbereitung(null, false, false)
        .parseHtml(html, "");
    for (Element e : elemente)
    {
      ct.addElement(e);
    }
    ct.go();

    doc.close();
    writer.close();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(delimiter = '|', textBlock = """
      IMG src HTTP | <img src="__URL__" alt="external image" />
      IMG protocol-relative | <img src="//__HOST__/xmlworker-img-protocol-relative.png" alt="external image" />
      Inline background-image | <div style="background-image:url('__URL__')">test</div>
      Inline background | <div style="background:url('__URL__')">test</div>
      CSS background-image | <style type="text/css">.test { background-image:url('__URL__'); }</style><div class="test">test</div>
      CSS background | <style type="text/css">.test { background:url('__URL__') no-repeat; }</style><div class="test">test</div>
      CSS list-style-image | <style type="text/css">.test { list-style-image:url('__URL__'); }</style><ul class="test"><li>test</li></ul>
      CSS border-image | <style type="text/css">.test { border-image:url('__URL__') 30 round; }</style><div class="test">test</div>
      CSS cursor | <style type="text/css">.test { cursor:url('__URL__'), auto; }</style><div class="test">test</div>
      CSS @import url | <style type="text/css">@import url('__URL__');</style>
      CSS @import direct | <style type="text/css">@import '__URL__';</style>
      CSS @font-face | <style type="text/css">@font-face { font-family:ExternalTestFont; src:url('__URL__'); }</style><div>font test</div>
      LINK stylesheet | <link rel="stylesheet" type="text/css" href="__URL__" />
      LINK protocol-relative | <link rel="stylesheet" type="text/css" href="//__HOST__/xmlworker-link-protocol-relative.css" />
      LINK favicon | <link rel="icon" type="image/x-icon" href="__URL__" />
      OBJECT data | <object data="__URL__" type="application/pdf">test</object>
      EMBED src | <embed src="__URL__" type="application/pdf" />
      IFRAME src | <iframe src="__URL__">test</iframe>
      VIDEO poster | <video poster="__URL__"></video>
      VIDEO source  | <video><source src="__URL__" type="video/mp4" /></video>
      VIDEO track  | <video><track src="__URL__" kind="subtitles" srclang="en" /></video>
      AUDIO source | <audio controls="controls"><source src="__URL__" type="audio/mpeg" /></audio>
      SCRIPT src | <script type="text/javascript" src="__URL__"></script>
      FORM action | <form action="__URL__" method="post"><input type="text" name="test" /></form>
      BASE href | <base href="__URL__" />
      META refresh | <meta http-equiv="refresh" content="1;url=__URL__" />
      SVG SVG | <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><image xlink:href="__URL__" width="200" height="100" /></svg>
      SVG use | <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><use xlink:href="__URL__#symbol" /></svg>
      Anchor href | <a href="__URL__">external link</a>
      Final inline background | <div style="background-image:url('__URL__')">final test</div>
      """)
  void externalResourceMustNotBeLoaded(String name, String html)
      throws Exception
  {
    html = html.replace("__URL__", "http://127.0.0.1:" + port + "/test");
    html = html.replace("__HOST__", "127.0.0.1:" + port);

    hasRequest = false;
    try
    {
      renderHtml(html);
    }
    catch (SecurityException ignore)
    {
      // Darf geworfen werden, wenn geblockt wird
    }
    assertFalse(hasRequest, () -> "Externe Resource geladen!");
  }

  @Test
  void testServerMustBeReachable() throws Exception
  {
    hasRequest = false;

    HttpURLConnection connection = (HttpURLConnection) new URI(
        "http://127.0.0.1:" + port + "/test-server-health").toURL()
            .openConnection();

    connection.setConnectTimeout(2000);
    connection.setReadTimeout(2000);
    connection.setRequestMethod("GET");

    try
    {
      assertEquals(200, connection.getResponseCode(),
          "Test server muss mit HTTP 200 antworten");

      assertTrue(connection.getInputStream().readAllBytes().length > 0,
          "Test server muss eine Antwort liefern");
    }
    finally
    {
      connection.disconnect();
    }

    assertTrue(hasRequest, "Test server muss erreichbar sein");
  }

  @Test
  void normalCssMustStillWork() throws Exception
  {
    String html = "<div style=\"color:red;font-size:12px;margin:10px;"
        + "padding:5px;text-align:center;font-weight:bold;"
        + "border:1px solid black;\">Normal CSS</div>";

    renderHtml(html);
  }

  @Test
  void dataUriMustNotCreateHttpRequest() throws Exception
  {
    hasRequest = false;

    String html = "<div style=\"background-image:"
        + "url('data:image/png;base64,iVBORw0KGgo=');\">data URI</div>";

    renderHtml(html);

    assertFalse(hasRequest, "data: URI must not create an HTTP request");
  }

}
