package org.sapia.corus.client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.AnyPattern;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class holds the return value of a clustered method
 * invocation.
 * 
 * @author yduchesne
 * 
 * @param <T> the concrete type of the data returned as part of an instance of this class.
 */
public class Result<T> {
  
  /**
   * Indicates what type of data a {@link Result} holds.
   */
  public enum Type {
    ELEMENT,
    COLLECTION;
    
    /**
     * @param clazz the {@link Class} for which to return the appropriate type.
     * @return the {@link Type} corresponding to the given class.
     */
    public static Type forClass(Class<?> clazz) {
      if (clazz.isArray() 
        || Collection.class.isAssignableFrom(clazz)
        || Map.class.isAssignableFrom(clazz)) {
        return Type.COLLECTION;
      } 
      return Type.ELEMENT;
    }
  }

  private CorusHost origin;
  private T data;
  private Type type;

  public Result(CorusHost origin, T data, Type type) {
    Assertions.notNull(origin, "Origin must be specified");
    Assertions.notNull(type, "Type must be specified");
    this.origin = origin;
    this.data = data;
    this.type = type;
  }

  /**
   * @return the return data of the remote invocation, or <code>null</code> if
   *         the invocation returned nothing.
   */
  public T getData() {
    if (isError()) {
      Throwable err = getError();
      err.fillInStackTrace();
      throw new IllegalStateException("Error occurred while performing operation on host: " + origin.getEndpoint().getServerAddress(), err);
    } else if (data == null) {
      throw new IllegalStateException("Null resulted from operation on host: " + origin.getEndpoint().getServerAddress());
    }
    return data;
  }
  
  /**
   * @return the {@link Type} of object that is instance is expected to hold.
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the {@link CorusHost} corresponding to the address of the Corus
   *         server on which the invocation was performed.
   */
  public CorusHost getOrigin() {
    return origin;
  }
  
  /**
   * @return <code>true</code> if this instance holds a {@link Throwable} instance as data.
   */
  public boolean isError() {
    return data instanceof Throwable;
  }
  
  /**
   * @return <code>true</code> if this instance's data is <code>null</code>.
   */
  public boolean isNull() {
    return data == null;
  }
  
  /**
   * @return the error that resulted from the invocation corresponding to this instance.
   */
  public Throwable getError() {
    Assertions.illegalState(!isError(), "Data not an instance of Throwable");
    return (Throwable) data;
  }
  
  /**
   * Filters this instance's content based on the given pattern.
   * 
   * @param pattern a {@link Pattern} to use for filtering.
   * @return this instance.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Result<T> filter(Pattern pattern) {
    if (data == null || pattern.getClass().equals(AnyPattern.class)) {
      // noop
    } else if (type == Result.Type.COLLECTION) {
      if (data instanceof Iterable) {
        List newData = new ArrayList();
        Iterable<?> elements = (Iterable<?>) data;
        for (Object e : elements) {
          if (e instanceof Matcheable && ((Matcheable) e).matches(pattern)) {
            newData.add(e);
          }
        }
        data = (T) newData;
      } else if (data instanceof Object[]) {
        List newData = new ArrayList();
        for (Object o : (Object[]) data) {
          if (o instanceof Matcheable && ((Matcheable) o).matches(pattern)) {
            newData.add(o);
          }
        }
        Object[] newArray = (Object[]) Array.newInstance(data.getClass().getComponentType(), newData.size());
        for (int i = 0; i < newArray.length; i++) {
          newArray[i]  = newData.get(i);
        }
        data = (T) newArray;
      } else if (data instanceof Map) {
        Map<Object, Object> newData = new HashMap<Object, Object>();
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) data).entrySet()) {
          if (entry.getValue() instanceof Matcheable && ((Matcheable) entry.getValue()).matches(pattern)) {
            newData.put(entry.getKey(), entry.getValue());
          }
        }
        data = (T) newData;
      }
    }
    return this;
  }
  
  /**
   * @return the number of elements that this instance holds.
   */
  public int size() {
    if (type == Type.COLLECTION) {
      if (data instanceof Object[]) {
        return ((Object[]) data).length;
      } else if (data instanceof Collection) {
        return ((Collection<?>) data).size();
      } else if (data instanceof Map) {
        return Map.class.cast(data).size();
      }
    }
    return 1;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "origin", origin, "data", data);
  }
}
