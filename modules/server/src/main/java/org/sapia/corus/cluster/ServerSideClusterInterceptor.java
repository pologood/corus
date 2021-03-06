package org.sapia.corus.cluster;

import java.rmi.RemoteException;
import java.util.Set;

import org.apache.log.Logger;
import org.sapia.corus.client.common.encryption.DecryptionContext;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.CorusCallback;
import org.sapia.corus.client.services.cluster.CorusCallbackCapable;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;

/**
 * Handles clustered commands on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class ServerSideClusterInterceptor implements Interceptor, CorusCallback {

  private Logger                  log;
  private ServerContext           context;
  private ClusterManager          cluster;
  private Func<Connections, ServerAddress> connectionPoolSupplier;
  
  /**
   * @param log the {@link Logger} to use.
   * @param context the {@link ServerContext}.
   * @param a function used to obtain a {@link Connections} instance.
   */
  ServerSideClusterInterceptor(Logger log, ServerContext context, Func<Connections, ServerAddress> connectionPoolSupplier) {
    this.log                    = log;
    this.context                = context;
    this.cluster                = context.getServices().lookup(ClusterManager.class);
    this.connectionPoolSupplier = connectionPoolSupplier;
  }

  /**
   * @param log the {@link Logger} to use.
   * @param context the {@link ServerContext}.
   */
  ServerSideClusterInterceptor(Logger log, ServerContext context) {
    this(log, context, new Func<Connections, ServerAddress>() {
      @Override
      public Connections call(ServerAddress nextTarget) {
        try {
          return Hub.getModules().getTransportManager().getConnectionsFor(nextTarget);
        } catch (RemoteException e)  {
          throw new IllegalStateException("Network error occurred while performing operation", e);
        }
      }
    });
  }

  /**
   * @param evt an {@link IncomingCommandEvent} instance.
   */
  public void onIncomingCommandEvent(IncomingCommandEvent evt) {
    if (evt.getCommand() instanceof CorusCallbackCapable) {
      CorusCallbackCapable capable = (CorusCallbackCapable) evt.getCommand();
      capable.setCorusCallback(this);
    }
  }

  // --------------------------------------------------------------------------
  // CorusCallback interface

  public org.sapia.corus.client.Corus getCorus() {
    return context.getCorus();
  }

  @Override
  public void debug(String message) {
    log.debug(message);
  }

  @Override
  public boolean isDebug() {
    return log.isDebugEnabled();
  }

  @Override
  public void error(String message, Throwable err) {
    log.error(message, err);
  }

  @Override
  public Set<ServerAddress> getSiblings() {
    return cluster.getHostAddresses();
  }
  
  @Override
  public DecryptionContext getDecryptionContext() {
    return Encryption.getDefaultDecryptionContext(context.getKeyPair().getPrivate());
  }

  @Override
  public Object send(ClusteredCommand cmd, ServerAddress nextTarget) throws Exception {
    Connections pool = connectionPoolSupplier.call(nextTarget);
    if (cmd.getAuditInfo().isSet()) {
      Assertions.illegalState(cmd.getAuditInfo().get().isEncrypted(), "Expected AuditInfo to have been decrypted at this point");
      log.debug("AuditInfo is set: encrypting with public key of next targeted host");
      CorusHost nextHost = cluster.resolveHost(nextTarget);
      cmd.setAuditInfo(cmd.getAuditInfo().get().encryptWith(Encryption.getDefaultEncryptionContext(nextHost.getPublicKey())));
    }
    
    RmiConnection conn = null;
    try {
      conn = pool.acquire();
      conn.send(cmd);
      Object returnValue = conn.receive();
      pool.release(conn);
      return returnValue;
    } catch (RemoteException re) {
      if (conn != null) {
        pool.invalidate(conn);
      }
      pool.clear();
      throw re;
    } catch (Exception e) {
      log.error("Problem sending clustered command to " + nextTarget, e);
      if (conn != null) {
        conn.close();
      }
      throw e;
    }

  }

}
