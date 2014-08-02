package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.ubik.util.Collects;

/**
 * Forces a repo pull.
 * 
 * @author yduchesne
 */
public class Pull extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    ctx.getCorus().getRepoFacade().pull(getClusterInfo(ctx));
  }
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_CLUSTER);
  }

}
