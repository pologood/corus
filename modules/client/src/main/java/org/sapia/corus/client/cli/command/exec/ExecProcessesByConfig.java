package org.sapia.corus.client.cli.command.exec;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.cli.command.Exec;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.ubik.util.Collects;

/**
 * Implements execution of processes by configuration (
 * <code>exec -e myExecConfig</code>).
 * 
 * @author yduchesne
 * 
 */
public class ExecProcessesByConfig extends AbstractExecCommand {
    
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(Exec.OPT_EXEC_CONFIG, Exec.OPT_WAIT, Exec.OPT_CLUSTER);
  }
  
  /**
   * Overriden to allow for unit testing.
   */
  @Override
  protected void validate(CmdLine cmdLine) throws InputException {
    super.validate(cmdLine);
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    ClusterInfo cluster = getClusterInfo(ctx);
    String configName = ctx.getCommandLine().assertOption(Exec.OPT_EXEC_CONFIG.getName(), true).getValue();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(configName)).build();
    displayProgress(ctx.getCorus().getProcessorFacade().execConfig(crit, cluster), ctx);

    // determining which hosts may have the running processes: they must have
    // the
    // distribution, and the tags corresponding to these processes
    Results<List<ExecConfig>> execResults = ctx.getCorus().getProcessorFacade().getExecConfigs(crit, cluster);

    while (execResults.hasNext()) {
      Result<List<ExecConfig>> execResult = execResults.next();
      for (ExecConfig e : execResult.getData()) {
        if (e.getName().equals(configName)) {
          Option waitOpt = getOpt(ctx, OPT_WAIT.getName());
          if (waitOpt != null) {
            for (ProcessDef pd : e.getProcesses()) {
              ProcessCriteria criteria = ProcessCriteria.builder().distribution(pd.getDist()).name(pd.getName()).profile(e.getProfile())
                  .version(pd.getVersion()).build();
              try {  
                waitForProcessStartup(ctx, criteria, pd.getInstances(), waitOpt.getValue() == null ? Exec.DEFAULT_EXEC_WAIT_TIME_SECONDS : waitOpt.asInt(),
                  cluster);
              } catch (NullPointerException npe) {
                // HACK: handle NPE that could not be reproduced
                ctx.getConsole().println("Unexpected runtime error occurred on the client side: NOT aborting (check server state)");
              }
            }
            ctx.getConsole().println("Process startup completed on all nodes");
          }
        }
      }
    }

  }

}
