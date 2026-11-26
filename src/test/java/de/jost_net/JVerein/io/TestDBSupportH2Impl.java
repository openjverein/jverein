package de.jost_net.JVerein.io;

import de.jost_net.JVerein.server.DBSupportH2v2Impl;

public class TestDBSupportH2Impl extends DBSupportH2v2Impl
{
  private static final long serialVersionUID = 1L;

  @Override
  public String getJdbcUrl()
  {
    return "jdbc:h2v2:mem:jverein_test;DB_CLOSE_DELAY=-1";
  }
}
