package org.sapia.corus.util;

import org.apache.log.Logger;
import org.sapia.corus.client.common.log.ExtendedLogCallback;
import org.sapia.corus.client.common.log.LogCallback;

/**
 * Implements the {@link LogCallback} interface over the {@link Logger} class.
 * 
 * @author yduchesne
 *
 */
public class LoggerLogCallback implements ExtendedLogCallback {
  
  private Logger log;
  
  public LoggerLogCallback(Logger log) {
    this.log = log;
  }
  
  @Override
  public void info(String msg) {
    log.info(msg);
  }
  
  @Override
  public void error(String msg) {
    log.error(msg);
  }
  
  @Override
  public void debug(String msg) {
    log.debug(msg);
  }
  
  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }
  
  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

}
