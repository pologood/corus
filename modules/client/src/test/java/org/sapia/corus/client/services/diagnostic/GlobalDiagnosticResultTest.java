package org.sapia.corus.client.services.diagnostic;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.json.JSONObject;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;

public class GlobalDiagnosticResultTest extends GlobalDiagnosticResult {
  
  private GlobalDiagnosticResult emptyResult, progressErrorResult, processErrorResult, processWithoutErrorResult, busyResult;
  
  
  @Before
  public void setUp() throws Exception {
    emptyResult = GlobalDiagnosticResult.Builder.newInstance()
        .progressDiagnostics(new ProgressDiagnosticResult(new ArrayList<String>()))
        .processDiagnostics(new ArrayList<ProcessConfigDiagnosticResult>())
        .build();
    

    progressErrorResult = GlobalDiagnosticResult.Builder.newInstance()
        .progressDiagnostics(new ProgressDiagnosticResult(Arrays.asList("error")))
        .processDiagnostics(new ArrayList<ProcessConfigDiagnosticResult>())
        .build();
    
    processErrorResult = GlobalDiagnosticResult.Builder.newInstance()
        .progressDiagnostics(new ProgressDiagnosticResult(new ArrayList<String>()))
        .processDiagnostics(new ArrayList<ProcessConfigDiagnosticResult>(Arrays.asList(createProcessConfigResult(true))))
        .build();
    
    
    processWithoutErrorResult = GlobalDiagnosticResult.Builder.newInstance()
        .progressDiagnostics(new ProgressDiagnosticResult(new ArrayList<String>()))
        .processDiagnostics(new ArrayList<ProcessConfigDiagnosticResult>(Arrays.asList(createProcessConfigResult(false))))
        .build();
    
    busyResult = GlobalDiagnosticResult.Builder.newInstance().busy().build();

  }
  
  @Test
  public void testGetStatus_empty_result() {
    assertEquals(GlobalDiagnosticStatus.SUCCESS, emptyResult.getStatus());
  }
  
  @Test
  public void testGetStatus_progress_error_result() {
    assertEquals(GlobalDiagnosticStatus.FAILURE, progressErrorResult.getStatus());
  }
  
  @Test
  public void testGetStatus_process_error_result() {
    assertEquals(GlobalDiagnosticStatus.FAILURE, processErrorResult.getStatus());
  }
  
  @Test
  public void testGetStatus_process_without_error_result() {
    assertEquals(GlobalDiagnosticStatus.SUCCESS, processWithoutErrorResult.getStatus());
  }
  
  @Test
  public void testGetStatus_busy_result() {
    assertEquals(GlobalDiagnosticStatus.INCOMPLETE, busyResult.getStatus());
  }

  @Test
  public void testToJson() {
    StringWriter writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    processErrorResult.toJson(stream, ContentLevel.DETAIL);
    JSONObject.fromObject(writer.getBuffer().toString());
  }
  
  @Test
  public void testToSummaryJson() {
    StringWriter writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    processErrorResult.toJson(stream, ContentLevel.DETAIL);
    JSONObject.fromObject(writer.getBuffer().toString());
  }

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(processErrorResult);
    SerializationUtils.deserialize(payload);
  }

  
  private ProcessConfigDiagnosticResult createProcessConfigResult(boolean failure) {
    if (failure) {
      return new ProcessConfigDiagnosticResult(
          SuggestionDiagnosticAction.REMEDIATE, 
          ProcessConfigDiagnosticStatus.FAILURE,
          new Distribution("test", "1.0"),
          new ProcessConfig("test"),
          new ArrayList<ProcessDiagnosticResult>()
      );
    } else {
      return new ProcessConfigDiagnosticResult(
          SuggestionDiagnosticAction.REMEDIATE, 
          ProcessConfigDiagnosticStatus.SUCCESS,
          new Distribution("test", "1.0"),
          new ProcessConfig("test"),
          new ArrayList<ProcessDiagnosticResult>()
      );      
    }
  }
}
