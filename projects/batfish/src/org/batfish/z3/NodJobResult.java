package org.batfish.z3;

import java.util.Collections;
import java.util.Set;

import org.batfish.representation.Flow;

public class NodJobResult {

   private Throwable _failureCause;

   private Set<Flow> _flows;

   private boolean _successful;

   public NodJobResult() {
      _successful = true;
      _flows = Collections.<Flow> emptySet();
   }

   public NodJobResult(Set<Flow> flows) {
      _flows = flows;
      _successful = true;
   }

   public NodJobResult(Throwable failureCause) {
      _failureCause = failureCause;
      _successful = false;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

   public boolean terminatedSuccessfully() {
      return _successful;
   }

}
