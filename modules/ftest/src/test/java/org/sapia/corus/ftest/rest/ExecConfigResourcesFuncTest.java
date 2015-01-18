package org.sapia.corus.ftest.rest;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.ubik.util.Assertions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ExecConfigResourcesFuncTest {

  private static final int MAX_ATTEMPTS      = 10;
  private static final int INTERVAL_SECONDS  = 3;
  
  private FtestClient client;  
  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
  }
  
  @AfterSuite
  public void afterSuite() throws Exception {
    tearDown();
    client.close();
  }
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
    tearDown();
  }
  
  private void tearDown() throws Exception {
    Interpreter interp = new Interpreter(client.getConnector());
    try {
      interp.eval("kill -d demo -v * -n noopApp -w 30 -cluster", StrLookup.noneLookup());
    } catch (Throwable err) {
      throw new IllegalStateException("Could not kill processes", err);
    }
    
    client.getConnector().getProcessorFacade().undeployExecConfig("ftest", ClusterInfo.clustered());
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testDeployExecConfig_clustered() throws Exception {
    
    File toDeploy = new File("etc/exec.xml");

    try(FileInputStream fis = new FileInputStream(toDeploy)) {
      JSONValue response = client.resource("/clusters/ftest/exec-configs")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
        
    JSONArray configs = client.resource("/clusters/ftest/exec-configs")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(configs.size(), client.getHostCount());
  }
  
  @Test
  public void testUndeployExecConfig_clustered() throws Exception {
    
    File toDeploy = new File("etc/exec.xml");

    client.getConnector().getProcessorFacade().deployExecConfig(toDeploy, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/exec-configs/ftest")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray configs = client.resource("/clusters/ftest/exec-configs")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(configs.size(), 0);
    
  }
  
  @Test
  public void testExecConfig_clustered() throws Exception {
    
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), ClusterInfo.clustered());
    
    File toDeploy = new File("etc/exec.xml");

    client.getConnector().getProcessorFacade().deployExecConfig(toDeploy, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/exec-configs/ftest/exec")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON),JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
  }
 
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testDeployExecConfig_specific_host() throws Exception {
    
    File toDeploy = new File("etc/exec.xml");

    Assertions.illegalState(!toDeploy.exists(), "Exec config does not exist");
    
    try(FileInputStream fis = new FileInputStream(toDeploy)) {
      JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/exec-configs")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
        
    JSONArray configs = client.resource("/clusters/ftest/exec-configs")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(configs.size(), 1);
  }
  
  @Test
  public void testUndeployExecConfig_specific_host() throws Exception {
    
    File toDeploy = new File("etc/exec.xml");
    
    Assertions.illegalState(!toDeploy.exists(), "Exec config does not exist");

    client.getConnector().getProcessorFacade().deployExecConfig(toDeploy, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/exec-configs/ftest")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray configs = client.resource("/clusters/ftest/exec-configs")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(configs.size(), client.getHostCount() - 1);
    
  }
  
  @Test
  public void testExecConfig_specific_host() throws Exception {
    
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), ClusterInfo.clustered());
    
    File toDeploy = new File("etc/exec.xml");

    client.getConnector().getProcessorFacade().deployExecConfig(toDeploy, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" +  client.getHostLiteral() + "/exec-configs/ftest/exec")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON),JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, 1);
    
  }

  // --------------------------------------------------------------------------
  // helpers
  
  private void waitUntilRunning(int maxAttempts, int intervalSecs, int expectedProcessCount) throws Exception {
    int attempts = 0;
    int actualProcessCount = 0;
    while (attempts < maxAttempts && actualProcessCount < expectedProcessCount) {
      JSONArray procs = client.resource("/clusters/ftest/processes")
          .queryParam("d", "demo")
          .queryParam("v", "*")
          .queryParam("n", "noopApp")
          .queryParam("p", "test")
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class).asArray();
      
      actualProcessCount = procs.size();
      Thread.sleep(intervalSecs * 1000);
      attempts++;
    }
    if (actualProcessCount < expectedProcessCount) {
      throw new IllegalStateException("Got " + actualProcessCount + " processes running, expected: " + expectedProcessCount);
    }
  }
}