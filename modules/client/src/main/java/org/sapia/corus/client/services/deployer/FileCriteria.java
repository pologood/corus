package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * Holds criteria to select files.
 * 
 * @author yduchesne
 * 
 */
public class FileCriteria implements Externalizable {

  static final long serialVersionUID = 1L;

  private ArgMatcher name = ArgMatchers.any();

  /**
   * @param alias
   *          the {@link ArgMatcher} instance corresponding to a file name.
   * @return this instance.
   */
  public FileCriteria setName(ArgMatcher alias) {
    this.name = alias;
    return this;
  }

  /**
   * @return this instance's {@link ArgMatcher} instance corresponding to a file name.
   */
  public ArgMatcher getName() {
    return name;
  }

  /**
   * @return a new {@link FileCriteria}.
   */
  public static FileCriteria newInstance() {
    return new FileCriteria();
  }

  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.name = (ArgMatcher) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
  }

}
