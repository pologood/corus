package org.sapia.corus.processor;

import junit.framework.TestCase;

import org.sapia.corus.client.services.processor.ExecConfig;

public class ExecConfigTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNewInstance() throws Exception{
    ExecConfig.newInstance(getClass().getResourceAsStream("testConf.xml"));
  }

}
