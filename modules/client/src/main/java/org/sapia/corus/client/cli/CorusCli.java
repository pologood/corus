package org.sapia.corus.client.cli;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.log4j.Level;
import org.sapia.console.CmdElement;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandConsole;
import org.sapia.console.Console;
import org.sapia.console.ConsoleInput;
import org.sapia.console.ConsoleInputFactory;
import org.sapia.console.ConsoleListener;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.CliPropertyKeys;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.cli.command.Sort;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnectionContextImpl;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.sort.SortSwitchInfo;
import org.sapia.ubik.util.Localhost;

/**
 * This class is the entry point into the Corus command-line interface.
 * 
 * @author Yanick Duchesne
 */
public class CorusCli extends CommandConsole {

  public static final int    DEFAULT_PORT      = 33000;
  public static final String HOST_OPT          = "h";
  public static final String PORT_OPT          = "p";
  public static final String SCRIPT_OPT        = "s";
  public static final String COMMAND_OPT       = "c";
  public static final int    MAX_ERROR_HISTORY = 20;

  private static ClientFileSystem FILE_SYSTEM = new DefaultClientFileSystem();

  protected CorusConnector corus;
  private List<CliError>   errors;
  private boolean          abortOnError;
  private StrLookup        vars = StrLookup.systemPropertiesLookup();
  private AtomicReference<SortSwitchInfo[]> sortSwitches;
 
  public CorusCli(CorusConnector corus) throws IOException {
    this(corus, getConsoleIO(ConsoleOutput.DefaultConsoleOutput.newInstance()));
  }
  
  private CorusCli(CorusConnector corus, ConsoleIO io) throws IOException {
    super(io, new CorusCommandFactory());
    this.corus = corus;
    this.sortSwitches = new AtomicReference<SortSwitchInfo[]>();
    this.sortSwitches.set(new SortSwitchInfo[]{});
    super.setCommandListener(new CliConsoleListener());
    errors = new AutoFlushedBoundedList<CliError>(MAX_ERROR_HISTORY);

    // Change the prompt
    setPrompt(CliUtils.getPromptFor(corus.getContext()));
  }
 
  private static ConsoleIO getConsoleIO(ConsoleOutput toWrap) {
    String osName = System.getProperty("os.name").toLowerCase();

    final AtomicReference<ConsoleInput> input = new AtomicReference<ConsoleInput>();
    // note: a case of the os.name being set to Vista has been reported.
    if (SystemUtils.IS_OS_WINDOWS || osName.contains("vista")) {
      input.set(ConsoleInputFactory.createJdk6ConsoleInput());
    } else {
      try {
        input.set(ConsoleInputFactory.createJLineConsoleInput());
      } catch (IOException e) {
        input.set(ConsoleInputFactory.createJdk6ConsoleInput());
      }
    }
    
    final ConsoleOutput output = CorusConsoleOutput.DefaultCorusConsoleOutput.wrap(toWrap);
    
    return new ConsoleIO() {
     
      @Override
      public ConsoleInput getInput() {
        return input.get();
      }
      
      @Override
      public ConsoleOutput getOutput() {
        return output;
      }
      
    };
  }

  public static void main(String[] args) {

    // disabling log4j output
    org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);

    String host = null;
    int port = DEFAULT_PORT;

    try {
      CmdLine cmd = CmdLine.parse(args);

      if (cmd.containsOption("ver", false)) {
        System.out.println("Corus client version: " + CorusVersion.create());
        System.out.println("Java version: " + System.getProperty("java.version"));
      } else if (CliUtils.isHelp(cmd)) {
        help();
      } else {
        if (cmd.containsOption(HOST_OPT, true)) {
          host = cmd.assertOption(HOST_OPT, true).getValue();
          if (host.equalsIgnoreCase("localhost")) {
            host = Localhost.getPreferredLocalAddress().getHostAddress();
          }
        } else {
          host = Localhost.getPreferredLocalAddress().getHostAddress();
        }

        if (cmd.containsOption(PORT_OPT, true)) {
          port = cmd.assertOption(PORT_OPT, true).asInt();
        }
        CorusConnectionContext connection = new CorusConnectionContextImpl(host, port, FILE_SYSTEM);
        CorusConnector connector = new CorusConnectorImpl(connection);

        // --------------------------------------------------------------------
        // -s option

        if (cmd.containsOption(SCRIPT_OPT, false)) {
          String path = cmd.assertOption(SCRIPT_OPT, true).getValue();

          Reader input = null;
          try {
            input = new FileReader(new File(path));
          } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
          }
          Map<String, String> vars = CliUtils.getOptionsMap(cmd);
          try {
            Interpreter console = new Interpreter(DefaultConsoleOutput.newInstance(), connector);
            console.interpret(input, StrLookup.mapLookup(vars));
            System.exit(0);
          } catch (Throwable err) {
            err.printStackTrace();
            System.exit(1);
          }

          // --------------------------------------------------------------------
          // -c option

        } else if (cmd.containsOption(COMMAND_OPT, false)) {
          Option opt = cmd.assertOption(COMMAND_OPT, true);
          StringBuilder commandLine = new StringBuilder(opt.getValue());

          for (int i = 0; i < cmd.size(); i++) {
            CmdElement ce = cmd.get(i);
            if (ce.equals(opt)) {
              for (int j = i + 1; j < cmd.size(); j++) {
                ce = cmd.get(j);
                commandLine.append(" ").append(ce.toString());
              }
              break;
            }
          }

          try {
            Interpreter console = new Interpreter(DefaultConsoleOutput.newInstance(), connector);
            console.eval(commandLine.toString(), StrLookup.systemPropertiesLookup());
            System.exit(0);
          } catch (Throwable err) {
            err.printStackTrace();
            System.exit(1);
          }

          // --------------------------------------------------------------------
          // default behavior

        } else {
          try {
            CorusCli cli = new CorusCli(connector);
            List<NameValuePair> props = connector.getConfigFacade().getProperties(PropertyScope.SERVER, new ClusterInfo(false)).next().getData();
            for (NameValuePair p : props) {
              if (p.getName().equals(CliPropertyKeys.SORT_SWITCHES) && p.getValue() != null) {
                List<SortSwitchInfo> s = Sort.getSwitches(p.getValue());
                cli.sortSwitches.set(s.toArray(new SortSwitchInfo[s.size()]));
              } 
            }
            cli.start();
          } catch (NullPointerException e) {
          }
        }
      }
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Exception e) {
      if (e instanceof ConnectionException || e instanceof RemoteException) {
        System.out.println("No server listening at " + host + ":" + port);
        e.printStackTrace();
      } else {
        e.printStackTrace();
      }
    }
  }

  // ==========================================================================
  // Restricted methods

  /**
   * @see org.sapia.console.CommandConsole#newContext()
   */
  protected Context newContext() {
    CliContextImpl context = new CliContextImpl(corus, errors, vars, sortSwitches);
    context.setAbortOnError(abortOnError);
    return context;
  }

  private static void help() {
    System.out.println();
    System.out.println("Corus client command-line syntax:");
    System.out.println();
    System.out.println("coruscli [-help] [-h <host>] [-p <port>] [-s script_path] [-c command_line]");
    System.out.println("or");
    System.out.println("coruscli -ver");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -h    host of the corus server to which to connect");
    System.out.println("        (defaults to local address).");
    System.out.println();
    System.out.println("  -p    specifies the port on which the corus server");
    System.out.println("        listens (defaults to 33000).");
    System.out.println();
    System.out.println("  -s    specifies the path to a script to execute");
    System.out.println();
    System.out.println("  -c    executes the command-line corresponding to the");
    System.out.println("        value of this option and exits immediately.");
    System.out.println();
    System.out.println("  -ver  indicates that the version of this command-line");
    System.out.println("        is to be displayed in the terminal.");
    System.out.println();
    System.out.println("  -help displays this help and exits immediately.");
  }

  // ==========================================================================
  // Inner classes

  public class CliConsoleListener implements ConsoleListener {
    /**
     * @see org.sapia.console.ConsoleListener#onAbort(Console)
     */
    public void onAbort(Console cons) {
      if (!errors.isEmpty()) {
        cons.out().println("Got " + errors.size() + " errors:");
        for (CliError err : errors) {
          cons.out().println("" + err.getId() + ") Error executing command: " + err.getCommandLine().toString());
          String stack = ExceptionUtils.getFullStackTrace(err.getCause());
          cons.out().println(stack);
        }
      } else {
        cons.println("Good bye...");
      }
      System.exit(0);
    }

    /**
     * @see org.sapia.console.ConsoleListener#onCommandNotFound(Console, String)
     */
    public void onCommandNotFound(Console cons, String command) {
      cons.println("Command not recognized: " + command);
    }

    /**
     * @see org.sapia.console.ConsoleListener#onStart(Console)
     */
    public void onStart(Console cons) {
      line(cons);

      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);

      center(cons, "");
      center(cons, "Corus Command Line Interface (" + CorusVersion.create().toString() + ")");
      center(cons, "");
      center(cons, "R E S T R I C T E D   A C C E S S");
      center(cons, "");
      center(cons, "Authorized Users Only");
      center(cons, "");
      center(cons, "(c)2002-" + year + " sapia-oss.org");
      center(cons, "");
      line(cons);
      cons.println();

      cons.println("-> Type man to see the list of available commands.");
      cons.println("-> Type man <command_name> to see help about a specific command.");
      cons.println();
    }
    
    void line(Console cons) {
      for (int i = 0; i < 80; i++) {
        cons.print("*");
      }

      cons.println("");
    }

    void center(Console cons, String text) {
      int margin = (80 - text.length()) / 2;
      cons.print("*");

      for (int i = 0; i < (margin - 1); i++) {
        cons.print(" ");
      }

      cons.print(text);

      if ((text.length() % 2) == 0) {
        for (int i = 0; i < (margin - 1); i++) {
          cons.print(" ");
        }
      } else {
        for (int i = 0; i < margin; i++) {
          cons.print(" ");
        }
      }

      cons.println("*");
    }
  }
}
