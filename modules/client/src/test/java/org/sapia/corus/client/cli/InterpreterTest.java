package org.sapia.corus.client.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.text.StrLookup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.console.AbortException;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcessCriteria;

public class InterpreterTest {

  private CorusConnectorImpl connector;
  private Interpreter console;

  @Before
  public void setUp() {
    this.connector = mock(CorusConnectorImpl.class);
    this.console = new Interpreter(DefaultConsoleOutput.newInstance(), connector);
  }

  @Test
  public void testInterpret() throws Throwable {
    ProcessorFacade processor = mock(ProcessorFacade.class);
    when(connector.getProcessorFacade()).thenReturn(processor);

    ProcessCriteria expectedCriteria = ProcessCriteria.builder()
        .distribution(ArgMatchers.exact("test"))
        .version(ArgMatchers.exact("1.0"))
        .name(ArgMatchers.exact("proc"))
        .profile("dev").build();

    final AtomicReference<ClusterInfo> inputClusterInfo = new AtomicReference<ClusterInfo>();
    final AtomicReference<ProcessCriteria> inputCriteria = new AtomicReference<ProcessCriteria>();

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        inputCriteria.set((ProcessCriteria) invocation.getArguments()[0]);
        inputClusterInfo.set((ClusterInfo) invocation.getArguments()[2]);
        return null;
      }
    }).when(processor).kill(any(ProcessCriteria.class), any(KillPreferences.class), any(ClusterInfo.class));

    console.eval("kill -d test -v 1.0 -n proc -p dev -cluster", StrLookup.systemPropertiesLookup());

    assertEquals(expectedCriteria.getDistribution(), inputCriteria.get().getDistribution());
    assertEquals(expectedCriteria.getVersion(), inputCriteria.get().getVersion());
    assertEquals(expectedCriteria.getName(), inputCriteria.get().getName());
    assertEquals(expectedCriteria.getProfile(), inputCriteria.get().getProfile());
    assertTrue("Expected clustered", inputClusterInfo.get().isClustered());
  }

  @Test
  public void testInterpret_confCommand() throws Throwable {
    ConfiguratorFacade config = mock(ConfiguratorFacade.class);
    when(connector.getConfigFacade()).thenReturn(config);

    console.eval("conf add -p name=value\\=123", StrLookup.systemPropertiesLookup());

    ArgumentCaptor<Properties> propsArg = ArgumentCaptor.forClass(Properties.class);
    
    verify(config).addProperties(eq(PropertyScope.PROCESS), propsArg.capture(), anySetOf(String.class), eq(false), any(ClusterInfo.class));
    Properties props = propsArg.getValue();
    assertEquals(props.getProperty("name"), "value=123");
  }

  @Test(expected = AbortException.class)
  public void testAbort() throws Throwable {
    console.eval("quit", StrLookup.systemPropertiesLookup());
  }

  @Test
  public void testInterpretCommandReader() throws Throwable {
    ProcessorFacade processor = mock(ProcessorFacade.class);
    when(connector.getProcessorFacade()).thenReturn(processor);

    ProcessCriteria expectedCriteria = ProcessCriteria.builder()
        .distribution(ArgMatchers.exact("test"))
        .version(ArgMatchers.exact("1.0"))
        .name(ArgMatchers.exact("proc"))
        .profile("dev").build();

    final AtomicReference<ClusterInfo> inputClusterInfo = new AtomicReference<ClusterInfo>();
    final AtomicReference<ProcessCriteria> inputCriteria = new AtomicReference<ProcessCriteria>();

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        inputCriteria.set((ProcessCriteria) invocation.getArguments()[0]);
        inputClusterInfo.set((ClusterInfo) invocation.getArguments()[2]);
        return null;
      }
    }).when(processor).kill(any(ProcessCriteria.class), any(KillPreferences.class), any(ClusterInfo.class));

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    pw.println("kill -d test -v 1.0 -n ${proc.name} -p dev -cluster");
    pw.println("# this is a comment");
    pw.println("echo \"process killed\"");

    StringReader reader = new StringReader(writer.toString());
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("proc.name", "proc");
    console.interpret(reader, StrLookup.mapLookup(vars));

    assertEquals(expectedCriteria.getDistribution(), inputCriteria.get().getDistribution());
    assertEquals(expectedCriteria.getVersion(), inputCriteria.get().getVersion());
    assertEquals(expectedCriteria.getName(), inputCriteria.get().getName());
    assertEquals(expectedCriteria.getProfile().get(), inputCriteria.get().getProfile().get());
    assertTrue("Expected clustered", inputClusterInfo.get().isClustered());
  }
  
  @Test
  public void testComment() throws Throwable {
    Object value = console.eval("# this is a comment", StrLookup.noneLookup());
    assertNull("Expected <null> as return value from eval() when comment is passed in", value);
  }

}