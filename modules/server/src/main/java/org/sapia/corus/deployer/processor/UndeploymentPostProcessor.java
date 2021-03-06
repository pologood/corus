package org.sapia.corus.deployer.processor;

import org.sapia.corus.client.common.log.LogCallback;

/**
 * Instances of this interface are invoked synchronously right after undeployment of a given 
 * distribution.
 * 
 * @author yduchesne
 *
 */
public interface UndeploymentPostProcessor {

  /**
   * @param context a {@link DeploymentContext}.
   * @param callback a {@link LogCallback} to log to for informing about progress.
   * @throws Exception if an error occurs while performing this operation.
   */
  public void onPostUndeploy(DeploymentContext context, LogCallback callback) throws Exception;
  
}
