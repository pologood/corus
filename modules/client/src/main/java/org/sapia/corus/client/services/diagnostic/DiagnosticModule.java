package org.sapia.corus.client.services.diagnostic;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Allows acquiring diagnostic information from a Corus instance.
 * 
 * @author yduchesne
 *
 */
public interface DiagnosticModule extends java.rmi.Remote, Module {

  String ROLE = DiagnosticModule.class.getName();
  
  /**
   * Performs global diagnostics acquisition (includes acquiring Corus-specific diagnostic).
   * 
   * @param requestingOwner the optional {@link LockOwner} of the party requesting the diagnostic.
   * @return a {@link GlobalDiagnosticResult}.
   */
  public GlobalDiagnosticResult acquireGlobalDiagnostics(OptionalValue<LockOwner> requestingOwner);

  /**
   * Performs global diagnostics acquisition (includes acquiring Corus-specific diagnostic).
   * 
   * @param criteria a {@link ProcessCriteria}, used to filter which process to acquire diagnostics for.
   * @param requestingOwner the optional {@link LockOwner} of the party requesting the diagnostic.
   * @return a {@link GlobalDiagnosticResult}.
   */
  public GlobalDiagnosticResult acquireGlobalDiagnostics(ProcessCriteria criteria, OptionalValue<LockOwner> requestingOwner);
  
  /**
   * Performs process diagnostic acquisition (excludes acquiring Corus-specific diagnostic).
   * 
   * @param requestingOwner the optional {@link LockOwner} of the party requesting the diagnostic.
   * @return the {@link List} of {@link ProcessConfigDiagnosticResult}s resulting from this operation.
   */
  public List<ProcessConfigDiagnosticResult> acquireProcessDiagnostics(OptionalValue<LockOwner> requestingOwner);
  
  /**
   * This method acquires the diagnostic for the given process. The returned result will correspond
   * to "incomplete" if the current lock owner of the process does not correspond to the one passed in.
   * 
   * @param process a {@link Process} whose diagnostic to acquire.
   * @param requestingOwner the optional {@link LockOwner} of the party requesting the diagnostic.
   * @return the {@link ProcessDiagnosticResult} corresponding to the given process.
   */
  public ProcessDiagnosticResult acquireProcessDiagnostics(Process process, OptionalValue<LockOwner> requestingOwner);

  
  /**
   * This method acquires the diagnostic for the given process. The returned result will correspond
   * to "incomplete" if the current lock owner of the process does not correspond to the one passed in.
   * 
   * @param criteria a {@link ProcessCriteria}, used to filter which process to acquire diagnostics for.
   * @param process a {@link Process} whose diagnostic to acquire.
   * @param requestingOwner the optional {@link LockOwner} of the party requesting the diagnostic.
   * @return the {@link List} of {@link ProcessConfigDiagnosticResult}s resulting from this operation.
   */
  public List<ProcessConfigDiagnosticResult> acquireProcessDiagnostics(ProcessCriteria criteria, OptionalValue<LockOwner> requestingOwner);
}
