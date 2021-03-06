package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;

/**
 * Sent to a Corus repository node so that deployment of the shell scripts that
 * are specified by an instance of this class is triggered.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptDeploymentRequest extends ArtifactDeploymentRequest {

  static final long serialVersionID = 1L;

  public static final String EVENT_TYPE = "corus.event.repository.request.scripts";

  private List<ShellScript> scripts = new ArrayList<ShellScript>();

  /**
   * Do not use: meant for externalization.
   */
  public ShellScriptDeploymentRequest() {
  }

  /**
   * @param endpoint
   *          the {@link Endpoint} of the requester from which this instance
   *          originates.
   * @param files
   *          the {@link ShellScript} instances corresponding to the scripts
   *          that are requested.
   */
  public ShellScriptDeploymentRequest(Endpoint endpoint, Collection<ShellScript> scripts) {
    super(endpoint);
    this.scripts.addAll(scripts);
  }

  /**
   * @return this instance's unmodifiable {@link List} of {@link ShellScript}
   *         instances.
   */
  public List<ShellScript> getScripts() {
    return Collections.unmodifiableList(scripts);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    this.scripts = (List<ShellScript>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(scripts);
  }

}
