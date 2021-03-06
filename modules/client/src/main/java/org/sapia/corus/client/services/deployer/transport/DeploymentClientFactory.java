package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;

import org.sapia.corus.client.services.deployer.transport.http.HttpDeploymentClient;
import org.sapia.ubik.net.ServerAddress;

/**
 * A factory class that is used to obtain <code>DeploymentClient</code>
 * instance.
 * 
 * @author Yanick Duchesne
 */
public class DeploymentClientFactory {

  /**
   * Returns a {@link DeploymentClient} instance, given the target address
   * passed in.
   * <p>
   * IMPORTANT: this method internally calls the {@link DeploymentClient}'s
   * {@link DeploymentClient#connect(ServerAddress)} method before returning it.
   * Thus, the caller MUST NOT itself call that method.
   * 
   * @see DeploymentClient#connect(ServerAddress)
   * 
   * @param addr
   *          a {@link ServerAddress}.
   * @return a {@link DeploymentClient} instance.
   * @throws IOException
   *           if a problem occurs returning the client instance.
   */
  public static DeploymentClient newDeploymentClientFor(ServerAddress addr) throws IOException {
    DeploymentClient client = new HttpDeploymentClient();
    client.connect(addr);
    return client;
  }

}
