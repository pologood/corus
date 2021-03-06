package org.sapia.corus.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.mcast.EventChannel;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;

/**
 * An instance of this class supplements the Spring bean lifecyle by supporting
 * additional constructs:
 * 
 * <ul>
 * <li>It recognizes the {@link Service} interface.
 * <li>It recognizes the {@link Bind} annotation.
 * </ul>
 * 
 * <p>
 * Instances that are annotated with {@link Bind} are automatically bound to
 * this instance's {@link InternalServiceContext}.
 * 
 * @author yduchesne
 * 
 */
class ModuleLifeCycleManager implements ServerContext, PropertyProvider {
  
  public static final String CORUS_DUMP_FILE_NAME = "corus-dump.json";

  private Logger                   logger   = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private List<ApplicationContext> contexts = new ArrayList<ApplicationContext>();
  private ServerContextImpl        delegate;
  private PropertyContainer        properties;

  ModuleLifeCycleManager(ServerContextImpl serverContext, PropertyContainer properties) {
    this.delegate = serverContext;
    this.properties = properties;
  }

  // /////// ServerContext interface

  @Override
  public Corus getCorus() {
    return delegate.getCorus();
  }
  
  @Override
  public KeyPair getKeyPair() {
    return delegate.getKeyPair();
  }

  @Override
  public String getDomain() {
    return delegate.getDomain();
  }

  @Override
  public String getHomeDir() {
    return delegate.getHomeDir();
  }
  
  @Override
  public String getNodeSubdirName() {
    return delegate.getNodeSubdirName();
  }

  @Override
  public EventChannel getEventChannel() {
    return delegate.getEventChannel();
  }

  @Override
  public CorusHost getCorusHost() {
    return delegate.getCorusHost();
  }

  @Override
  public CorusTransport getTransport() {
    return delegate.getTransport();
  }

  @Override
  public Properties getCorusProperties() {
    return delegate.getCorusProperties();
  }

  @Override
  public Properties getProcessProperties(List<String> categories) throws IOException {
    return delegate.getProcessProperties(categories);
  }

  @Override
  public String getServerName() {
    return delegate.getServerName();
  }

  @Override
  public void overrideServerName(String serverName) {
    delegate.overrideServerName(serverName);
  }

  @Override
  public InternalServiceContext getServices() {
    return delegate.getServices();
  }

  @Override
  public <S> S lookup(Class<S> serviceInterface) {
    return delegate.lookup(serviceInterface);
  }

  @Override
  public Object lookup(String name) {
    return delegate.lookup(name);
  }

  // /////// PropertyProvider interface

  @Override
  public void overrideInitProperties(PropertyContainer properties) {
    this.properties = properties;
  }

  @Override
  public PropertyContainer getInitProperties() {
    return properties;
  }

  // /////// Instance methods

  ServerContext getServerContext() {
    return delegate;
  }

  void addApplicationContext(ApplicationContext ctx) {
    contexts.add(ctx);
  }

  void startServices() throws Exception {
    File dumpFile    = new File(new File(delegate.getHomeDir()), CORUS_DUMP_FILE_NAME);
    try {
      doStartServices(dumpFile);
    } finally {
      if (dumpFile.exists()) {
        logger.warn(String.format("Deleting dump file: %s", dumpFile.getAbsolutePath()));
        dumpFile.delete();
      }
    }
  }
  
  void dump(JsonStream stream) {
    stream.beginObject();
    for (ApplicationContext context : contexts) {
      Map<String, Dumpable> dumpable = context.getBeansOfType(Dumpable.class);
      for (Dumpable d : dumpable.values()) {
        d.dump(stream);
      }
    }
    stream.endObject();
  }
  
  private void doStartServices(File dumpFile) throws Exception {
    JsonInput dumpContent = null;
    if (dumpFile.exists()) {
      logger.warn(String.format("Found dump file: %s. Will load content", dumpFile.getAbsolutePath()));
      dumpContent = JsonObjectInput.newInstance(IOUtil.textStreamToString(new FileInputStream(dumpFile)));
    }
    
    for (ApplicationContext context : contexts) {
      for (String name : context.getBeanDefinitionNames()) {
        Object bean = context.getBean(name);
        if (bean instanceof Dumpable && dumpContent != null) {
          logger.warn(String.format("Component %s will process dump file: %s", bean.getClass().getName(), dumpFile.getAbsolutePath()));
          Dumpable dumpable = (Dumpable) bean;
          dumpable.load(dumpContent);
        }
        
        if (bean instanceof Service) {
          try {
            Service service = (Service) bean;
            service.start();
          } catch (Exception e) {
            throw new FatalBeanException("Error performing service initialization for " + name, e);
          }
        }
      }
    }
  }
}
