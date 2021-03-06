package org.sapia.corus.client.cli.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sapia.console.AbortException;
import org.sapia.console.ExecHandle;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.CliUtil;

/**
 * Invokes an OS command.
 * 
 * @author yduchesne
 * 
 */
public class Cmd extends NoOptionCommand {

  private static int BUFSZ = 1024;
  private static int COMMAND_TIME_OUT = 5000;

  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    if (!ctx.getCommandLine().hasNext()) {
      throw new InputException("Command has no arguments, cannot be executed (specify an OS command to execute)");
    }

    try {
      ExecHandle handle = ctx.getCommandLine().exec();

      // Extract the output stream of the process
      ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
      CliUtil.extractUntilAvailable(handle.getInputStream(), anOutput, COMMAND_TIME_OUT);

      ctx.getConsole().println(anOutput.toString("UTF-8").trim());

      // Extract the error stream of the process
      anOutput.reset();
      CliUtil.extractAvailable(handle.getErrStream(), anOutput);
      if (anOutput.size() > 0) {
        ctx.getConsole().println(anOutput.toString("UTF-8").trim());
        throw new AbortException("Aborting on process error");
      }
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, e);
    }
  }
}
