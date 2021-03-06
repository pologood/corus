package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.ubik.util.Collects;

public class SendSecurityConfigNotificationTaskTest extends AbstractRepoTaskTest {

  private SendSecurityConfigNotificationTask task;
  
  @Before
  public void setUp() {
    super.doSetUp();
    Set<Endpoint> endpoints = new HashSet<Endpoint>();

    task = new SendSecurityConfigNotificationTask(repoConfig, endpoints);
    
    when(security.getRoleConfig(any(ArgMatcher.class))).thenReturn(
        Collects.arrayToList(
            new SecurityModule.RoleConfig("admin", Collects.arrayToSet(Permission.values()))
        )
    );
    
    when(appkeys.getAppKeyConfig(any(ArgMatcher.class))).thenReturn(
        Collects.arrayToList(
            new ApplicationKeyManager.AppKeyConfig("test-app", "test-role", "test-key")
        )
    );
    
  }  
  
  @Test
  public void test_normal_case() throws Throwable {
    task.execute(taskContext, null);
    
    verify(cluster).send(any(ClusterNotification.class));
  }
  
  @Test
  public void test_no_security_config() throws Throwable {
    when(security.getRoleConfig(any(ArgMatcher.class))).thenReturn(new ArrayList<SecurityModule.RoleConfig>());
    when(appkeys.getAppKeyConfig(any(ArgMatcher.class))).thenReturn(new ArrayList<ApplicationKeyManager.AppKeyConfig>());
    
    task.execute(taskContext, null);
    
    verify(cluster, never()).send(any(ClusterNotification.class));
  }

}
