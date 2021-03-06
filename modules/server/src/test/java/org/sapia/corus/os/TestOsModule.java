package org.sapia.corus.os;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.os.OsModule;

public class TestOsModule implements OsModule{

  private int pidCounter;

  @Override
  public synchronized String executeProcess(LogCallback log, File rootDirectory,
      CmdLine commandLine, Map<String, String> processOptions) throws IOException {
    return Integer.toString(++pidCounter);
  }

  @Override
  public void executeScript(LogCallback log, File rootDirectory,
      CmdLine commandLine) throws IOException {
  }

  @Override
  public void killProcess(LogCallback log, KillSignal sig, String pid) throws IOException {
  }

}