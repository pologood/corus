package org.sapia.corus.taskmanager.core;

import java.util.UUID;

/**
 * A default {@link ThrottleKey} implementation.
 * 
 * @author yduchesne
 * 
 */
public class DefaultThrottleKey implements ThrottleKey {

  private String name;

  public DefaultThrottleKey() {
    this(UUID.randomUUID().toString());
  }

  public DefaultThrottleKey(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ThrottleKey) {
      return name.equals(((DefaultThrottleKey) obj).getName());
    } else {
      return false;
    }
  }

}
