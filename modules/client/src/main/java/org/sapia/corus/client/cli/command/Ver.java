package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Displays the Corus version.
 * 
 * @author yduchesne
 * 
 */
public class Ver extends NoOptionCommand {

  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    ctx.getConsole().println("Server version: " + ctx.getCorus().getContext().getVersion());
  }

}
