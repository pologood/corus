package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;

public class ExecConfigDatabaseImpl implements ExecConfigDatabase {

  private DbMap<String, ExecConfig> configs;
  private AtomicInteger lastId = new AtomicInteger(1);
  
  public ExecConfigDatabaseImpl(DbMap<String, ExecConfig> configs) {
    this.configs = configs;
    for (ExecConfig c : configs) {
      if (c.getId() > lastId.get()) {
        lastId.set(c.getId());
      }
    }
  }
  
  public synchronized List<ExecConfig> getConfigs() {
    Iterator<ExecConfig> itr = configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while (itr.hasNext()) {
      toReturn.add(itr.next());
    }
    Collections.sort(toReturn);
    return toReturn;
  }

  public synchronized List<ExecConfig> getBootstrapConfigs() {
    Iterator<ExecConfig> itr = configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while (itr.hasNext()) {
      ExecConfig ec = itr.next();
      if (ec.isStartOnBoot()) {
        toReturn.add(ec);
      }
    }
    Collections.sort(toReturn);
    return toReturn;
  }

  public synchronized List<ExecConfig> getConfigsFor(ExecConfigCriteria criteria) {
    Iterator<ExecConfig> itr      = configs.values();
    List<ExecConfig>     toReturn = new ArrayList<ExecConfig>();
    while (itr.hasNext()) {
      ExecConfig c = itr.next();
      if (criteria.getName().matches(c.getName())) {
        toReturn.add(c);
      }
    }
    Collections.sort(toReturn);
    return toReturn;
  }

  public synchronized void removeConfigsFor(ExecConfigCriteria criteria) {
    List<ExecConfig> toRemove = getConfigsFor(criteria);
 
    if (criteria.getBackup() > 0) {
      // sorting in reverse order
      Collections.sort(toRemove, new Comparator<ExecConfig>() {
        @Override
        public int compare(ExecConfig e1, ExecConfig e2) {
          return - (e1.getId() - e2.getId());
        }
      });
      
      toRemove = toRemove.subList(criteria.getBackup(), toRemove.size());
    }
    for (ExecConfig c : toRemove) {
      configs.remove(c.getName());
    }
  }

  public synchronized ExecConfig getConfigFor(String name) {
    return configs.get(name);
  }

  public synchronized void removeConfig(String name) {
    configs.remove(name);
  }

  public synchronized void addConfig(ExecConfig c) {
    if (c.getId() == 0) {
      c.setId(lastId.getAndIncrement());
    }
    configs.put(c.getName(), c);
  }

  public synchronized void removeProcessesForDistribution(Distribution d) {
    Iterator<ExecConfig> itr = configs.values();
    while (itr.hasNext()) {
      ExecConfig c = itr.next();
      c.removeAll(d);
      if (c.getProcesses().size() == 0) {
        configs.remove(c.getName());
      } else {
        c.save();
      }
    }
  }
  
}
